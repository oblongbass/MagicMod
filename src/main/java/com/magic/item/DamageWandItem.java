package com.magic.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DamageWandItem extends Item {
    private static final int MAX_CHARGE_TIME = 100;
    private static final int AUTO_RELEASE_TIME = 120;
    private static final int MIN_CHARGE_TIME = 10;
    private static final int COOLDOWN_TICKS = 40;
    private static final float MAX_DAMAGE = 10.0f;

    private static final int[] BASE_CHARGE_THRESHOLDS = {20, 40, 60, 80, 100};
    private static final float[] BASE_DAMAGE_VALUES = {2.0f, 4.0f, 6.0f, 8.0f, 10.0f};

    private static final Map<UUID, Long> chargeStartTimes = new HashMap<>();

    private static final String CHARGE_POWER_ID = "magic-mod:charge_power";
    private static final String DAMAGE_POWER_ID = "magic-mod:damage_power";

    public DamageWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            user.setCurrentHand(hand);
            return ActionResult.SUCCESS;
        }

        user.setCurrentHand(hand);
        chargeStartTimes.put(user.getUuid(), world.getTime());

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5f, 1.0f);

        user.sendMessage(Text.literal("§a开始蓄力... (5秒达到最大伤害)"), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient() || !(user instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) user;
        Long startTime = chargeStartTimes.get(player.getUuid());

        if (startTime != null) {
            int useTime = (int) (world.getTime() - startTime);

            if (useTime % 20 == 0 && useTime <= MAX_CHARGE_TIME) {
                float damage = calculateDamageWithEnchantments(stack, useTime);
                int progressPercent = (int) ((float) useTime / MAX_CHARGE_TIME * 100);
                player.sendMessage(Text.literal("§e蓄力进度: " + progressPercent + "% - 伤害: " + String.format("%.1f", damage)), true);
            }

            if (useTime >= AUTO_RELEASE_TIME) {
                player.stopUsingItem();
                return;
            }
        }
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient() || !(user instanceof PlayerEntity)) return false;

        PlayerEntity player = (PlayerEntity) user;
        Long startTime = chargeStartTimes.get(player.getUuid());
        int useTime = 0;

        if (startTime != null) {
            useTime = (int) (world.getTime() - startTime);
            chargeStartTimes.remove(player.getUuid());
        } else {
            useTime = Math.min(MAX_CHARGE_TIME, this.getMaxUseTime(stack, user) - remainingUseTicks);
        }

        if (useTime < MIN_CHARGE_TIME) {
            player.sendMessage(Text.literal("§c蓄力时间太短! 至少需要0.5秒"), true);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.0f);
            return false;
        }

        float damage = calculateDamageWithEnchantments(stack, useTime);
        boolean hit = fireDamageBeam(player, world, damage, stack);

        if (hit) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 0.8f);
            player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

            if (world instanceof ServerWorld) {
                stack.damage(1, player, player.getActiveHand());
            }
        } else {
            player.sendMessage(Text.literal("§c未命中目标!"), true);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.0f);
            player.getItemCooldownManager().set(stack, COOLDOWN_TICKS / 2);
        }

        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, net.minecraft.entity.LivingEntity user) {
        return AUTO_RELEASE_TIME;
    }

    // 关键修复：使用新的组件系统来定义物品属性
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public int getEnchantability() {
        return 15;
    }

    private float calculateDamageWithEnchantments(ItemStack stack, int useTime) {
        int chargePowerLevel = getEnchantmentLevel(stack, CHARGE_POWER_ID);
        int damagePowerLevel = getEnchantmentLevel(stack, DAMAGE_POWER_ID);

        int[] chargeThresholds = applyChargePower(chargePowerLevel);
        useTime = Math.min(useTime, MAX_CHARGE_TIME);

        // 修复：正确确定伤害阶段 - 从最高阈值开始向下检查
        int damageStage = 0;
        for (int i = chargeThresholds.length - 1; i >= 0; i--) {
            if (useTime >= chargeThresholds[i]) {
                damageStage = i;
                break;
            }
        }

        float baseDamage = BASE_DAMAGE_VALUES[damageStage];

        if (damagePowerLevel > 0) {
            float damageBonus = damagePowerLevel * 2.0f;
            float progress = (float) useTime / MAX_CHARGE_TIME;
            baseDamage += damageBonus * progress;
        }

        return baseDamage;
    }

    private int getEnchantmentLevel(ItemStack stack, String enchantmentId) {
        try {
            var enchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (enchantmentsComponent == null) {
                return 0;
            }

            for (var entry : enchantmentsComponent.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> enchantment = entry.getKey();
                int level = entry.getIntValue();

                String id = enchantment.getIdAsString();
                if (id.equals(enchantmentId)) {
                    return level;
                }
            }
            return 0;
        } catch (Exception e) {
            System.err.println("获取附魔等级时出错: " + e.getMessage());
            return 0;
        }
    }

    private int[] applyChargePower(int level) {
        if (level <= 0) {
            return BASE_CHARGE_THRESHOLDS;
        }

        float reductionFactor = 1.0f - (level * 0.2f);
        reductionFactor = Math.max(reductionFactor, 0.4f);

        int[] modifiedThresholds = new int[BASE_CHARGE_THRESHOLDS.length];
        for (int i = 0; i < BASE_CHARGE_THRESHOLDS.length; i++) {
            modifiedThresholds[i] = (int) (BASE_CHARGE_THRESHOLDS[i] * reductionFactor);
        }

        return modifiedThresholds;
    }

    private boolean fireDamageBeam(PlayerEntity player, World world, float damage, ItemStack stack) {
        Vec3d start = player.getEyePos();
        Vec3d rotation = player.getRotationVec(1.0F);
        double maxDistance = 20.0;
        Vec3d end = start.add(rotation.multiply(maxDistance));
        Box box = player.getBoundingBox().stretch(rotation.multiply(maxDistance)).expand(1.0);

        EntityHitResult entityHitResult = findEntityInDirection(world, player, start, end, box);

        if (entityHitResult != null) {
            Entity target = entityHitResult.getEntity();
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;

                if (world instanceof ServerWorld serverWorld) {
                    livingTarget.damage(serverWorld, player.getDamageSources().magic(), damage);
                }

                sendDamageMessage(player, target, damage, stack);
                return true;
            }
        }

        return false;
    }

    private void sendDamageMessage(PlayerEntity player, Entity target, float damage, ItemStack stack) {
        int chargePowerLevel = getEnchantmentLevel(stack, CHARGE_POWER_ID);
        int damagePowerLevel = getEnchantmentLevel(stack, DAMAGE_POWER_ID);

        StringBuilder message = new StringBuilder("§6对 " + target.getName().getString() + " 造成 " + String.format("%.1f", damage) + " 点魔法伤害!");

        if (chargePowerLevel > 0) {
            message.append(" §a[蓄能").append(chargePowerLevel).append("]");
        }
        if (damagePowerLevel > 0) {
            message.append(" §c[伤能").append(damagePowerLevel).append("]");
        }

        player.sendMessage(Text.literal(message.toString()), true);
    }

    private EntityHitResult findEntityInDirection(World world, PlayerEntity user, Vec3d start, Vec3d end, Box box) {
        EntityHitResult result = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : world.getOtherEntities(user, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator())) {

            Box entityBox = entity.getBoundingBox();
            Optional<Vec3d> hit = entityBox.raycast(start, end);

            if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    result = new EntityHitResult(entity, hit.get());
                }
            }
        }

        return result;
    }
}