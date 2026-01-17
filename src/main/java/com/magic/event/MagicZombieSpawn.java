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
        // 暂时禁用魔法僵尸生成
        return false;
    }

    private static void spawnMagicZombie(ServerWorld world, BlockPos pos) {
        // 暂时禁用魔法僵尸生成
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