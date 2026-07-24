package com.magic.event;

import com.magic.data.SimplePlayerDataManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家攻击生物时，将当前元素标记到目标身上（持续15秒）。
 * 火元素令目标着火，火+水触发蒸发反应造成额外伤害。
 */
public class ElementAttackHandler {

    public record ElementMark(int element, long expireTick) {}

    private static final Map<UUID, ElementMark> entityElementMarks = new ConcurrentHashMap<>();
    private static final int MARK_DURATION_TICKS = 15 * 20;

    /** 活跃的伤害数字实体 */
    static final Map<UUID, DamageNumber> activeDamageNumbers = new ConcurrentHashMap<>();

    static class DamageNumber {
        TextDisplayEntity entity;
        int age;
        static final int MAX_AGE = 20;
        final double startY;

        DamageNumber(TextDisplayEntity entity, double startY) {
            this.entity = entity;
            this.startY = startY;
            this.age = 0;
        }
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;
            if (!SimplePlayerDataManager.isMagicUnlocked(serverPlayer)) return ActionResult.PASS;

            int attackerElement = SimplePlayerDataManager.getCurrentElement(serverPlayer);
            int targetElement = getEntityElement(target);
            boolean isVaporize = isVaporizeReaction(attackerElement, targetElement);

            // 检查元素反应：火+水 = 蒸发
            if (isVaporize) {
                float reactionDamage = 4.0f;
                ServerWorld sw = (ServerWorld) world;
                target.damage(sw, player.getDamageSources().magic(), reactionDamage);
                spawnDamageNumber(sw, target.getLerpedPos(1.0F).add(0, target.getHeight() + 0.3, 0), reactionDamage);
            }

            // 火元素效果：着火（蒸发反应时水灭火，不点燃）
            if (attackerElement == 0 && !isVaporize) {
                target.setOnFireFor(3);
            }

            // 打上新标记
            long expireTick = world.getTime() + MARK_DURATION_TICKS;
            entityElementMarks.put(target.getUuid(), new ElementMark(attackerElement, expireTick));

            return ActionResult.PASS;
        });
    }

    /** 火+水 / 水+火 = 蒸发反应 */
    private static boolean isVaporizeReaction(int attacker, int target) {
        return (attacker == 0 && target == 1) || (attacker == 1 && target == 0);
    }

    /** 生成浮动伤害数字 */
    public static void spawnDamageNumber(ServerWorld world, Vec3d pos, float damage) {
        TextDisplayEntity text = new TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        text.setPosition(pos);
        text.setText(Text.literal(String.format("%.1f", damage)).formatted(Formatting.GOLD));
        text.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        world.spawnEntity(text);
        activeDamageNumbers.put(text.getUuid(), new DamageNumber(text, pos.y));
    }

    /** 每 tick 更新伤害数字动画 */
    public static void tickDamageNumbers() {
        var it = activeDamageNumbers.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            DamageNumber dn = entry.getValue();
            dn.age++;
            if (dn.age >= DamageNumber.MAX_AGE || dn.entity.isRemoved()) {
                dn.entity.remove(Entity.RemovalReason.DISCARDED);
                it.remove();
                continue;
            }
            float progress = (float) dn.age / DamageNumber.MAX_AGE;
            // 上跳
            dn.entity.setPosition(dn.entity.getX(), dn.startY + progress * 1.5, dn.entity.getZ());
            // 后半段淡出：opacity 从 127 降到 0
            int opacity = progress < 0.5f ? 127 : (int) (127 * (1.0f - (progress - 0.5f) * 2.0f));
            dn.entity.setTextOpacity((byte) opacity);
        }
    }

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
    }
}
