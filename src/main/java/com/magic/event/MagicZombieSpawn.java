package com.magic.event;

import com.magic.Entity.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.Random;

public class MagicZombieSpawn {
    private static final Random random = new Random();
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            // 每 5 秒尝试生成一次（100 ticks = 5 秒）
            if (tickCounter >= 100) {
                tickCounter = 0;

                // 对所有世界进行处理
                for (ServerWorld world : server.getWorlds()) {
                    // 只在主世界生成
                    if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
                        trySpawnMagicZombies(world);
                    }
                }
            }
        });
    }

    private static void trySpawnMagicZombies(ServerWorld world) {
        // 获取所有在线玩家
        for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
            // 每个玩家有 25% 的几率触发生成检查
            if (random.nextFloat() < 0.25f) {
                attemptSpawnNearPlayer(world, player);
            }
        }
    }

    private static void attemptSpawnNearPlayer(ServerWorld world, net.minecraft.server.network.ServerPlayerEntity player) {
        // 在玩家周围 24-48 格范围内随机选择位置
        int distance = 24 + random.nextInt(25); // 24-48 格
        double angle = random.nextDouble() * Math.PI * 2;

        int spawnX = (int) (player.getX() + Math.cos(angle) * distance);
        int spawnZ = (int) (player.getZ() + Math.sin(angle) * distance);

        // 获取生成位置的地面高度
        int spawnY = world.getTopY(Heightmap.Type.WORLD_SURFACE, spawnX, spawnZ);
        BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);

        // 检查生成条件
        if (canSpawnAtLocation(world, spawnPos)) {
            // 尝试生成魔法僵尸
            spawnMagicZombie(world, spawnPos);
        }
    }

    private static boolean canSpawnAtLocation(ServerWorld world, BlockPos pos) {
        // 检查亮度等级（小于5）
        if (world.getLightLevel(pos) >= 5) {
            return false;
        }

        // 检查是否为有效生物群系
        net.minecraft.world.biome.Biome biome = world.getBiome(pos).value();
        if (!biome.getSpawnSettings().getSpawnEntries(net.minecraft.entity.SpawnGroup.MONSTER).isEmpty()) {
            // 修复：使用正确的生成限制检查方法
            return SpawnRestriction.canSpawn(ModEntities.MAGIC_ZOMBIE, world, SpawnReason.NATURAL, pos, world.getRandom());
        }

        return false;
    }

    private static void spawnMagicZombie(ServerWorld world, BlockPos pos) {
        // 修复：使用正确的实体创建方法
        com.magic.Entity.MagicZombieEntity magicZombie = ModEntities.MAGIC_ZOMBIE.create(world, SpawnReason.NATURAL);

        if (magicZombie != null) {
            // 设置位置和朝向
            magicZombie.refreshPositionAndAngles(
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    random.nextFloat() * 360.0f, 0.0f
            );

            // 尝试在世界上生成
            if (magicZombie.canSpawn(world, SpawnReason.NATURAL)) {
                world.spawnEntity(magicZombie);

                // 生成粒子效果（可选）
                if (world.random.nextFloat() < 0.3f) {
                    spawnSpawnParticles(world, pos);
                }

                System.out.println("[MagicZombieSpawn] 在位置 " + pos + " 生成了魔法僵尸");
            }
        }
    }

    private static void spawnSpawnParticles(ServerWorld world, BlockPos pos) {
        // 在生成位置周围生成魔法粒子
        for (int i = 0; i < 8; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = world.random.nextDouble() * 2.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;

            world.spawnParticles(
                    net.minecraft.particle.ParticleTypes.ENCHANT,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1,
                    0, 0, 0,
                    0.02
            );
        }
    }
}