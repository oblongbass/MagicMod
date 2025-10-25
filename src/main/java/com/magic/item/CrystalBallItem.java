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

import java.util.*;

public class CrystalBallItem extends Item {
    private static final int SCAN_RANGE = 15; // 扫描范围
    private static final int COOLDOWN_TICKS = 40; // 2秒冷却
    private static final int PARTICLE_DURATION = 100; // 粒子效果持续5秒 (100 ticks)

    // 存储正在显示的粒子效果
    private static final Map<UUID, ParticleDisplayTask> activeParticleTasks = new HashMap<>();

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

        // 设置冷却时间
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

            // 为所有检测到的实体启动持续粒子效果
            for (LivingEntity entity : entities) {
                startEntityParticleEffect(entity, world);
            }
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
        startDivinationParticleEffect(user, world);
    }

    // 启动实体粒子效果（持续显示）
    private void startEntityParticleEffect(LivingEntity entity, ServerWorld world) {
        UUID entityId = entity.getUuid();

        // 如果已有该实体的粒子效果，先移除
        if (activeParticleTasks.containsKey(entityId)) {
            activeParticleTasks.remove(entityId);
        }

        // 创建新的粒子显示任务
        ParticleDisplayTask task = new ParticleDisplayTask(entity, world, System.currentTimeMillis());
        activeParticleTasks.put(entityId, task);
    }

    // 启动占卜粒子效果（持续显示）
    private void startDivinationParticleEffect(PlayerEntity player, ServerWorld world) {
        UUID playerId = player.getUuid();

        // 如果已有该玩家的占卜粒子效果，先移除
        if (activeParticleTasks.containsKey(playerId)) {
            activeParticleTasks.remove(playerId);
        }

        // 创建新的占卜粒子显示任务
        ParticleDisplayTask task = new ParticleDisplayTask(player, world, System.currentTimeMillis());
        activeParticleTasks.put(playerId, task);

        // 播放占卜音效
        world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                0.8f,
                1.2f
        );
    }

    // 在服务器刻事件中更新所有粒子效果
    public static void tickParticleEffects() {
        // 使用迭代器安全地移除已完成的任务
        Iterator<Map.Entry<UUID, ParticleDisplayTask>> iterator = activeParticleTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ParticleDisplayTask> entry = iterator.next();
            ParticleDisplayTask task = entry.getValue();

            if (task == null || task.isExpired() || task.getEntity().isRemoved()) {
                iterator.remove();
                continue;
            }

            // 更新粒子效果
            task.update();
        }
    }

    // 粒子显示任务类
    private static class ParticleDisplayTask {
        private final LivingEntity entity;
        private final ServerWorld world;
        private final long startTime;
        private int tickCounter = 0;
        private boolean isDivinationEffect;

        public ParticleDisplayTask(LivingEntity entity, ServerWorld world, long startTime) {
            this.entity = entity;
            this.world = world;
            this.startTime = startTime;
            this.isDivinationEffect = entity instanceof PlayerEntity;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > PARTICLE_DURATION * 50; // 转换为毫秒
        }

        public void update() {
            tickCounter++;

            if (isDivinationEffect) {
                updateDivinationParticles();
            } else {
                updateEntityParticles();
            }
        }

        private void updateEntityParticles() {
            Vec3d pos = entity.getPos();

            // 每5ticks生成一次环绕粒子（避免过于密集）
            if (tickCounter % 5 == 0) {
                // 在实体周围生成环绕粒子
                for (int i = 0; i < 4; i++) {
                    double angle = (i / 4.0) * Math.PI * 2;
                    double x = pos.x + Math.cos(angle) * 1.2;
                    double z = pos.z + Math.sin(angle) * 1.2;

                    world.spawnParticles(
                            ParticleTypes.ELECTRIC_SPARK,
                            x, pos.y + entity.getHeight() / 2, z,
                            1, // 数量
                            0.1, 0.1, 0.1, // 随机偏移
                            0.02 // 速度
                    );
                }
            }

            // 每10ticks生成一次头顶标识粒子
            if (tickCounter % 10 == 0) {
                world.spawnParticles(
                        ParticleTypes.GLOW,
                        pos.x, pos.y + entity.getHeight() + 0.5, pos.z,
                        2,
                        0.2, 0.1, 0.2,
                        0.03
                );
            }

            // 每20ticks生成一次额外的魔法粒子
            if (tickCounter % 20 == 0) {
                world.spawnParticles(
                        ParticleTypes.ENCHANT,
                        pos.x, pos.y + entity.getHeight() / 2, pos.z,
                        3,
                        0.5, 0.3, 0.5,
                        0.05
                );
            }
        }

        private void updateDivinationParticles() {
            Vec3d pos = entity.getPos();

            // 每ticks都生成一些基础粒子
            if (tickCounter % 2 == 0) {
                // 在玩家周围随机位置生成粒子
                double offsetX = (world.random.nextDouble() - 0.5) * 3;
                double offsetZ = (world.random.nextDouble() - 0.5) * 3;

                world.spawnParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        pos.x + offsetX, pos.y + 0.5, pos.z + offsetZ,
                        1,
                        0, 0.05, 0,
                        0.02
                );
            }

            // 每10ticks生成一次魔法阵粒子
            if (tickCounter % 10 == 0) {
                for (int i = 0; i < 8; i++) {
                    double angle = (i / 8.0) * Math.PI * 2;
                    double radius = 2.0 + Math.sin(tickCounter * 0.1) * 0.5; // 脉动效果
                    double x = pos.x + Math.cos(angle) * radius;
                    double z = pos.z + Math.sin(angle) * radius;

                    world.spawnParticles(
                            ParticleTypes.END_ROD,
                            x, pos.y + 0.1, z,
                            1,
                            0, 0, 0,
                            0.05
                    );
                }
            }

            // 每15ticks生成一次上升粒子
            if (tickCounter % 15 == 0) {
                for (int i = 0; i < 5; i++) {
                    double offsetX = (world.random.nextDouble() - 0.5) * 2;
                    double offsetZ = (world.random.nextDouble() - 0.5) * 2;

                    world.spawnParticles(
                            ParticleTypes.GLOW,
                            pos.x + offsetX, pos.y + 0.2, pos.z + offsetZ,
                            1,
                            0, 0.1, 0,
                            0.03
                    );
                }
            }
        }
    }
}