package com.magic.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class CrystalBallItem extends Item {
    private static final int SCAN_RANGE = 15; // 扫描范围
    private static final int COOLDOWN_TICKS = 40; // 2秒冷却

    public CrystalBallItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;

        if (user.isSneaking()) {
            // 潜行右键：占卜功能
            performDivination(serverWorld, user);
        } else {
            // 普通右键：扫描周围实体
            scanNearbyEntities(serverWorld, user);
        }

        // 播放使用音效
        world.playSound(
                null,
                user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS,
                1.0f,
                0.8f + world.random.nextFloat() * 0.4f
        );

        // 修复：在 1.21.5 中，使用新的冷却系统
        // 直接使用 ItemCooldownManager 的正确方法
        user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

        return ActionResult.SUCCESS;
    }

    private void scanNearbyEntities(ServerWorld world, PlayerEntity user) {
        Vec3d userPos = user.getPos();
        Box scanBox = new Box(
                userPos.add(-SCAN_RANGE, -SCAN_RANGE, -SCAN_RANGE),
                userPos.add(SCAN_RANGE, SCAN_RANGE, SCAN_RANGE)
        );

        List<LivingEntity> entities = world.getEntitiesByClass(
                LivingEntity.class,
                scanBox,
                entity -> entity != user && entity.isAlive()
        );

        // 发送扫描结果
        if (entities.isEmpty()) {
            user.sendMessage(Text.literal("§b水晶球显示：附近没有检测到生物"), true);
        } else {
            user.sendMessage(Text.literal("§b水晶球检测到 " + entities.size() + " 个生物："), true);

            for (int i = 0; i < Math.min(entities.size(), 5); i++) { // 最多显示5个
                LivingEntity entity = entities.get(i);
                String entityName = entity.getDisplayName().getString();
                double distance = Math.round(entity.distanceTo(user) * 10) / 10.0;

                user.sendMessage(Text.literal("§7- " + entityName + " §8(距离: " + distance + "米)"), false);
            }

            if (entities.size() > 5) {
                user.sendMessage(Text.literal("§7... 还有 " + (entities.size() - 5) + " 个生物"), false);
            }
        }

        // 在实体周围生成粒子效果
        for (LivingEntity entity : entities) {
            spawnEntityParticles(world, entity);
        }
    }

    private void performDivination(ServerWorld world, PlayerEntity user) {
        String[] fortunes = {
                "今日运势：大吉 - 你会找到稀有宝物",
                "今日运势：中吉 - 冒险中将有奇遇",
                "今日运势：小吉 - 平静但幸运的一天",
                "今日运势：末吉 - 小心脚下的陷阱",
                "今日运势：凶 - 最好待在安全的地方",
                "水晶球一片迷雾... 命运尚未确定",
                "星星指引你向东方前进",
                "地下深处有宝藏等待发现",
                "注意天空中的异常现象",
                "古老的魔法在附近涌动"
        };

        String fortune = fortunes[world.random.nextInt(fortunes.length)];
        user.sendMessage(Text.literal("§d" + fortune), true);

        // 在玩家周围生成华丽的粒子效果
        spawnDivinationParticles(world, user);
    }

    private void spawnEntityParticles(ServerWorld world, LivingEntity entity) {
        Vec3d pos = entity.getPos();

        // 在实体周围生成环绕粒子
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double x = pos.x + Math.cos(angle) * 1.2;
            double z = pos.z + Math.sin(angle) * 1.2;

            world.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x, pos.y + entity.getHeight() / 2, z,
                    2, // 数量
                    0.1, 0.1, 0.1, // 随机偏移
                    0.02 // 速度
            );
        }

        // 在头顶生成标识粒子
        world.spawnParticles(
                ParticleTypes.GLOW,
                pos.x, pos.y + entity.getHeight() + 0.5, pos.z,
                3,
                0.3, 0.1, 0.3,
                0.05
        );
    }

    private void spawnDivinationParticles(ServerWorld world, PlayerEntity player) {
        Vec3d pos = player.getPos();

        // 在玩家周围生成魔法阵粒子
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            double radius = 2.5;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;

            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    x, pos.y + 0.1, z,
                    1,
                    0, 0, 0,
                    0.1
            );
        }

        // 在玩家头顶生成上升粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2;

            world.spawnParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.x + offsetX, pos.y + 0.5, pos.z + offsetZ,
                    1,
                    0, 0.1, 0,
                    0.05
            );
        }

        // 播放占卜音效
        world.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                0.8f,
                1.2f
        );
    }
}