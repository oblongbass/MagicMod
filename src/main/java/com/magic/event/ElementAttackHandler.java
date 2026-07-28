package com.magic.event;

import com.magic.data.SimplePlayerDataManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElementAttackHandler {

    // ==================== 元素标记 ====================

    public record ElementMark(int element, long expireTick, int remainingReactions, boolean spread) {}

    private static final Map<UUID, ElementMark> entityElementMarks = new ConcurrentHashMap<>();
    private static final int MARK_DURATION_TICKS = 15 * 20;

    // ==================== 伤害数字 ====================

    static final Map<UUID, DamageNumber> activeDamageNumbers = new ConcurrentHashMap<>();

    static class DamageNumber {
        Entity entity;
        int age;
        static final int MAX_AGE = 20;
        final double startY;

        DamageNumber(Entity entity, double startY) {
            this.entity = entity;
            this.startY = startY;
            this.age = 0;
        }
    }

    // ==================== 土元素禁锢 ====================

    private static final Map<UUID, Integer> immobilizerTicks = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    // ==================== 蒸发状态（蒸发后残留标记，可被风引爆） ====================

    private static final Map<UUID, Long> vaporizeStates = new ConcurrentHashMap<>();
    private static final int VAPORIZE_STATE_DURATION_TICKS = 5 * 20; // 5秒

    // ==================== 元素激活状态 ====================

    private static final Map<UUID, Boolean> elementActivated = new ConcurrentHashMap<>();

    public static boolean isActivated(ServerPlayerEntity player) {
        return elementActivated.getOrDefault(player.getUuid(), false);
    }

    public static void setActivated(ServerPlayerEntity player, boolean activated) {
        if (activated) {
            elementActivated.put(player.getUuid(), true);
        } else {
            elementActivated.remove(player.getUuid());
        }
    }

    // ==================== 注册 ====================

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
            if (!SimplePlayerDataManager.isMagicUnlocked(serverPlayer)) return ActionResult.PASS;

            // 检查元素是否已激活
            if (!isActivated(serverPlayer)) {
                serverPlayer.sendMessage(Text.literal("§c请右键法杖激活元素能力！"), true);
                return ActionResult.PASS;
            }

            int attackerElement = SimplePlayerDataManager.getCurrentElement(serverPlayer);
            ElementMark existingMark = entityElementMarks.get(target.getUuid());

            // 检查过期
            if (existingMark != null && world.getTime() > existingMark.expireTick()) {
                entityElementMarks.remove(target.getUuid());
                existingMark = null;
            }

            int targetElement = (existingMark != null) ? existingMark.element() : -1;
            boolean isVaporize = isVaporizeReaction(attackerElement, targetElement);

            ServerWorld sw = (ServerWorld) world;

            // ———— 蒸发反应 ————
            if (isVaporize) {
                float damage = calcWeightedDamage(target, sw);
                // 若目标是被风扩散的标记，蒸发伤害减半
                if (existingMark != null && existingMark.spread()) {
                    damage *= 0.5f;
                }
                target.damage(sw, player.getDamageSources().magic(), damage);
                spawnDamageNumber(sw, target.getLerpedPos(1.0F).add(0, target.getHeight() + 0.3, 0), damage);

                int remaining = existingMark.remainingReactions();
                long expire = world.getTime() + MARK_DURATION_TICKS;
                boolean wasSpread = existingMark.spread();

                if (remaining > 0) {
                    // 有额外触发次数 → 消耗一次，保留原标记（扩散状态也保留）
                    remaining--;
                    entityElementMarks.put(target.getUuid(), new ElementMark(targetElement, expire, remaining, wasSpread));
                } else if (remaining == -1) {
                    // 首次触发 → 掷额外次数，只掷这一次
                    int extra = rollExtraReactions();
                    if (extra > 0) {
                        entityElementMarks.put(target.getUuid(), new ElementMark(targetElement, expire, extra - 1, wasSpread));
                    } else {
                        entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, expire, -1, false));
                    }
                } else {
                    // remaining == 0 已耗尽，打上新标记
                    entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, expire, -1, false));
                }

                // 蒸发反应后施加蒸发状态（残留5秒，可被风引发扩散蒸发）
                vaporizeStates.put(target.getUuid(), world.getTime() + VAPORIZE_STATE_DURATION_TICKS);

                return ActionResult.PASS;
            }

            // ———— 火元素：着火（非蒸发时） ————
            if (attackerElement == 0) {
                target.setOnFireFor(3);
            }

            // ———— 土元素：禁锢 ————
            if (attackerElement == 3) {
                int seconds = RANDOM.nextInt(5) + 1; // 1-5秒
                int ticks = seconds * 20;
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 9, false, false, true));
                immobilizerTicks.put(target.getUuid(), ticks);
            }

            // ———— 风元素：蒸发扩散 ————
            if (attackerElement == 2) {
                // 优先检查目标是否有蒸发状态 → 引爆弱化蒸发伤害并扩散
                Long vaporizeExpire = vaporizeStates.get(target.getUuid());
                if (vaporizeExpire != null && world.getTime() <= vaporizeExpire) {
                    vaporizeStates.remove(target.getUuid()); // 消耗状态，不可重复触发

                    float damage = calcWeightedDamage(target, sw) * 0.3f; // 弱化蒸发
                    if (damage < 1) damage = 1;
                    target.damage(sw, player.getDamageSources().magic(), damage);
                    spawnDamageNumber(sw, target.getLerpedPos(1.0F).add(0, target.getHeight() + 0.1, 0), damage);

                    // 蒸发伤害扩散到附近生物（减半，不显示数字避免过多）
                    var nearbyEntities = world.getEntitiesByClass(LivingEntity.class,
                            target.getBoundingBox().expand(5.0),
                            e -> e != target && e.isAlive() && !e.isSpectator() && !(e instanceof ArmorStandEntity));
                    for (LivingEntity nearby : nearbyEntities) {
                        float nearbyDmg = damage * 0.5f;
                        if (nearbyDmg < 0.5f) nearbyDmg = 0.5f;
                        nearby.damage(sw, player.getDamageSources().magic(), nearbyDmg);
                        Vec3d npos = nearby.getLerpedPos(1.0f);
                        sw.spawnParticles(ParticleTypes.CLOUD,
                                npos.x, npos.y + nearby.getHeight() * 0.5, npos.z,
                                3, 0.3, 0.3, 0.3, 0.05);
                    }

                    // 风爆粒子
                    Vec3d tpos = target.getLerpedPos(1.0f);
                    sw.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                            tpos.x, tpos.y + target.getHeight() * 0.5, tpos.z,
                            5, 0.5, 0.5, 0.5, 0.1);

                    // 打风标记
                    long expire = world.getTime() + MARK_DURATION_TICKS;
                    entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, expire, -1, false));

                    return ActionResult.PASS;
                }

                // ———— 风元素：扩散火/水 ————
                // 目标有火或水标记 → 扩散到周围生物
                if (existingMark != null && (existingMark.element() == 0 || existingMark.element() == 1)) {
                    int sourceElement = existingMark.element();
                    double spreadRadius = 5.0;
                    var nearbyEntities = world.getEntitiesByClass(LivingEntity.class,
                            target.getBoundingBox().expand(spreadRadius),
                            e -> e != target && e.isAlive() && !e.isSpectator() && !(e instanceof ArmorStandEntity));

                    for (LivingEntity nearby : nearbyEntities) {
                        // 已有非扩散的同元素标记则不覆盖
                        ElementMark nearbyMark = entityElementMarks.get(nearby.getUuid());
                        if (nearbyMark != null && world.getTime() <= nearbyMark.expireTick()
                                && nearbyMark.element() == sourceElement && !nearbyMark.spread()) {
                            continue;
                        }

                        // 扩散标记：持续时间减半，标记为 spread
                        long spreadExpire = world.getTime() + MARK_DURATION_TICKS / 2;
                        entityElementMarks.put(nearby.getUuid(), new ElementMark(sourceElement, spreadExpire, -1, true));

                        // 火扩散到附近→短时间着火
                        if (sourceElement == 0) {
                            nearby.setOnFireFor(1);
                            // 对被扩散的生物造成微量火焰伤害并显示数字
                            float spreadDmg = 1.0f + RANDOM.nextFloat() * 2.0f; // 1~3点
                            nearby.damage(sw, player.getDamageSources().magic(), spreadDmg);
                            spawnDamageNumber(sw, nearby.getLerpedPos(1.0f).add(0, nearby.getHeight() + 0.1, 0), spreadDmg);
                        }

                        // 扩散粒子效果
                        Vec3d npos = nearby.getLerpedPos(1.0f);
                        sw.spawnParticles(ParticleTypes.CLOUD,
                                npos.x, npos.y + nearby.getHeight() * 0.5, npos.z,
                                3, 0.3, 0.3, 0.3, 0.05);
                    }

                    // 原目标失去元素，改为风标记
                    long windExpire = world.getTime() + MARK_DURATION_TICKS;
                    entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, windExpire, -1, false));

                    // 原目标上的扩散风粒子
                    Vec3d tpos = target.getLerpedPos(1.0f);
                    sw.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                            tpos.x, tpos.y + target.getHeight() * 0.5, tpos.z,
                            5, 0.5, 0.5, 0.5, 0.1);

                    return ActionResult.PASS;
                }
                // 无标记/土/风 → 直接穿透到下方通用标记打风标记
            }

            // ———— 打上新标记 ————
            long expire = world.getTime() + MARK_DURATION_TICKS;
            entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, expire, -1, false));

            return ActionResult.PASS;
        });
    }

    // ==================== 加权伤害计算 ====================

    /** HP≤50 → 10-40, HP>50 → 50-90，伤害越高几率越低 */
    private static float calcWeightedDamage(LivingEntity target, ServerWorld world) {
        float hp = target.getHealth();
        int min, max;
        if (hp <= 50) {
            min = 10; max = 40;
        } else {
            min = 50; max = 90;
        }
        // 加权：val = min + i，权重 = 1/(i+1)
        float total = 0;
        int len = max - min + 1;
        float[] weights = new float[len];
        for (int i = 0; i < len; i++) {
            weights[i] = 1.0f / (i + 1);
            total += weights[i];
        }
        float r = RANDOM.nextFloat() * total;
        float cum = 0;
        for (int i = 0; i < len; i++) {
            cum += weights[i];
            if (r <= cum) return min + i;
        }
        return max;
    }

    // ==================== 蒸发额外次数 ====================

    /** 掷额外触发次数：50%→1, 30%→2, 10%→3, 10%→0 */
    private static int rollExtraReactions() {
        float r = RANDOM.nextFloat();
        if (r < 0.10f) return 3;
        if (r < 0.40f) return 2;  // 0.10~0.40 → 30%
        if (r < 0.90f) return 1;  // 0.40~0.90 → 50%
        return 0;                  // 0.90~1.00 → 10%
    }

    // ==================== 反应检测 ====================

    private static boolean isVaporizeReaction(int attacker, int target) {
        return (attacker == 0 && target == 1) || (attacker == 1 && target == 0);
    }

    // ==================== 伤害数字 ====================

    public static void spawnDamageNumber(ServerWorld world, Vec3d pos, float damage) {
        ArmorStandEntity stand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        stand.setPosition(pos);
        stand.setCustomName(Text.literal(String.format("%.1f", damage)).formatted(Formatting.GOLD));
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        world.spawnEntity(stand);
        activeDamageNumbers.put(stand.getUuid(), new DamageNumber(stand, pos.y));
    }

    public static void tickDamageNumbers() {
        for (var it = activeDamageNumbers.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            DamageNumber dn = entry.getValue();
            dn.age++;
            if (dn.age >= DamageNumber.MAX_AGE || dn.entity.isRemoved()) {
                dn.entity.remove(Entity.RemovalReason.DISCARDED);
                it.remove();
                continue;
            }
            float progress = (float) dn.age / DamageNumber.MAX_AGE;
            dn.entity.setPosition(dn.entity.getX(), dn.startY + progress * 1.5, dn.entity.getZ());
        }
    }

    // ==================== 公共查询 ====================

    public static int getEntityElement(LivingEntity entity) {
        ElementMark mark = entityElementMarks.get(entity.getUuid());
        if (mark == null) return -1;
        if (entity.getEntityWorld().getTime() > mark.expireTick()) {
            entityElementMarks.remove(entity.getUuid());
            return -1;
        }
        return mark.element();
    }

    public static int getEntityElement(Entity entity, World world) {
        ElementMark mark = entityElementMarks.get(entity.getUuid());
        if (mark == null) return -1;
        if (world.getTime() > mark.expireTick()) {
            entityElementMarks.remove(entity.getUuid());
            return -1;
        }
        return mark.element();
    }

    public static void cleanExpired(World world) {
        long now = world.getTime();
        entityElementMarks.entrySet().removeIf(entry -> now > entry.getValue().expireTick());
        vaporizeStates.entrySet().removeIf(entry -> now > entry.getValue());
        // ConcurrentHashMap 的 entrySet 不支持 setValue，改用 replaceAll + removeIf
        immobilizerTicks.replaceAll((key, val) -> val - 1);
        immobilizerTicks.values().removeIf(v -> v <= 0);
    }

    // ==================== 土元素粒子（每 tick 由 MagicMod 调用） ====================

    private static int earthParticleTick = 0;

    public static void tickEarthParticles(World world) {
        earthParticleTick++;
        if (earthParticleTick % 10 != 0) return; // 每 10 tick 一次

        long now = world.getTime();
        for (var entry : immobilizerTicks.entrySet()) {
            if (entry.getValue() <= 0) continue;
            Entity entity = world.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living)) continue;

            // 检查 Slowness 效果是否还在（防止粒子在效果过期后还出现）
            if (!living.hasStatusEffect(StatusEffects.SLOWNESS)) continue;

            if (world instanceof ServerWorld sw) {
                // 在目标周围生成少量圆形粒子（每 10 tick 3-5 个）
                int count = RANDOM.nextInt(3) + 3;
                for (int i = 0; i < count; i++) {
                    double angle = RANDOM.nextDouble() * Math.PI * 2;
                    double radius = 0.8 + RANDOM.nextDouble() * 0.4;
                    double px = living.getX() + Math.cos(angle) * radius;
                    double pz = living.getZ() + Math.sin(angle) * radius;
                    double py = living.getY() + RANDOM.nextDouble() * living.getHeight();
                    sw.spawnParticles(ParticleTypes.CRIT, px, py, pz, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
