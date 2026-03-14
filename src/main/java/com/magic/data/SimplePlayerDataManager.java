package com.magic.data;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.MagicMod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据管理器 - 使用存档数据持久化存储
 */
public class SimplePlayerDataManager {
    
    /**
     * 获取存档数据
     */
    private static MagicCircleSavedData getSavedData(World world) {
        return MagicCircleSavedData.getOrCreate(world);
    }
    
    /**
     * 获取玩家数据
     */
    public static PlayerData getPlayerData(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.warn("[Magic] 无法获取存档数据，使用临时数据");
            return new PlayerData();
        }
        return savedData.getPlayerData(player.getUuid());
    }
    
    /**
     * 设置魔法阵
     */
    public static void setMagicCircle(ServerPlayerEntity player, BlockPos pos, String dimension, UUID itemEntityUuid) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.error("[Magic] 无法获取存档数据，无法保存魔法阵");
            return;
        }
        
        PlayerData data = savedData.getPlayerData(player.getUuid());
        data.setMagicCircle(pos, dimension, itemEntityUuid);
        savedData.setMagicCircle(player.getUuid(), data);
        savedData.markDirty(); // 确保标记为脏数据
        
        MagicMod.LOGGER.info("[Magic] 保存玩家 {} 的魔法阵数据到存档，位置: {}, 维度: {}", 
            player.getName().getString(), pos, dimension);
    }
    
    /**
     * 清除魔法阵
     */
    public static void clearMagicCircle(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.error("[Magic] 无法获取存档数据，无法清除魔法阵");
            return;
        }
        
        savedData.clearMagicCircle(player.getUuid());
        
        MagicMod.LOGGER.info("[Magic] 清除玩家 {} 的魔法阵数据", 
            player.getName().getString());
    }
    
    /**
     * 检查是否有激活的魔法阵
     */
    public static boolean hasActiveMagicCircle(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            return false;
        }
        return savedData.hasActiveMagicCircle(player.getUuid());
    }
    
    /**
     * 获取魔法阵位置
     */
    public static BlockPos getMagicCirclePos(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCirclePos;
    }
    
    /**
     * 获取魔法阵维度
     */
    public static String getMagicCircleDimension(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCircleDimension;
    }
    
    /**
     * 获取魔法阵物品实体UUID
     */
    public static UUID getMagicCircleItemEntityUuid(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCircleItemUuid;
    }
    
    /**
     * 设置魔法阵物品实体UUID
     */
    public static void setMagicCircleItemEntityUuid(ServerPlayerEntity player, UUID itemEntityUuid) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.error("[Magic] 无法获取存档数据，无法更新物品实体UUID");
            return;
        }
        
        PlayerData data = savedData.getPlayerData(player.getUuid());
        data.magicCircleItemUuid = itemEntityUuid;
        savedData.setMagicCircle(player.getUuid(), data);
    }
    
    /**
     * 验证并恢复所有玩家的魔法阵数据
     * 当玩家加入游戏时调用
     * 检查存档中记录的魔法阵是否真实存在（花岗岩和物品实体）
     * 并激活所有有效魔法阵的粒子特效（只对在线的所有者）
     */
    public static void restoreMagicCircleFromWorld(ServerPlayerEntity player) {
        try {
            World world = player.getEntityWorld();
            UUID playerUuid = player.getUuid();
            net.minecraft.server.MinecraftServer server = world.getServer();

            if (server == null) {
                MagicMod.LOGGER.warn("[Magic] 无法获取服务器实例");
                return;
            }

            MagicMod.LOGGER.info("[Magic] 开始恢复所有玩家的魔法阵数据");

            MagicCircleSavedData savedData = getSavedData(world);
            if (savedData == null) {
                MagicMod.LOGGER.warn("[Magic] 无法获取存档数据，无法恢复魔法阵");
                return;
            }

            // 获取所有玩家的魔法阵数据
            Map<UUID, PlayerData> allPlayerData = savedData.getAllPlayerData();
            MagicMod.LOGGER.info("[Magic] 存档中有 {} 个玩家的数据", allPlayerData.size());

            String currentDimension = world.getRegistryKey().getValue().toString();
            int restoredCount = 0;

            // 遍历所有玩家的魔法阵数据
            for (Map.Entry<UUID, PlayerData> entry : allPlayerData.entrySet()) {
                UUID ownerUuid = entry.getKey();
                PlayerData data = entry.getValue();

                MagicMod.LOGGER.info("[Magic] 检查玩家 {} 的魔法阵数据: 有激活魔法阵={}, 位置={}, 维度={}",
                    ownerUuid, data.hasActiveMagicCircle(), data.magicCirclePos, data.magicCircleDimension);

                if (!data.hasActiveMagicCircle() || data.magicCirclePos == null || data.magicCircleDimension == null) {
                    MagicMod.LOGGER.info("[Magic] 玩家 {} 没有激活的魔法阵数据，跳过", ownerUuid);
                    continue;
                }

                // 检查维度是否匹配
                if (!currentDimension.equals(data.magicCircleDimension)) {
                    MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵在维度 {}，当前维度 {}，跳过",
                        ownerUuid, data.magicCircleDimension, currentDimension);
                    continue;
                }

                // 验证花岗岩是否存在
                BlockPos granitePos = data.magicCirclePos;
                net.minecraft.block.BlockState blockState = world.getBlockState(granitePos);
                MagicMod.LOGGER.info("[Magic] 检查位置 {} 的方块: {}", granitePos, blockState.getBlock());

                if (blockState.getBlock() != net.minecraft.block.Blocks.POLISHED_GRANITE) {
                    MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵花岗岩缺失，位置: {}，当前方块: {}，清理数据",
                        ownerUuid, granitePos, blockState.getBlock());
                    savedData.clearMagicCircle(ownerUuid);
                    continue;
                }

                // 查找魔法阵物品实体
                MagicCircleItemEntity foundEntity = null;
                BlockPos itemPos = granitePos.up();
                // 扩大搜索范围到32x32x32的区域，提高找到实体的概率
                Box searchBox = new Box(
                    itemPos.getX() - 16, itemPos.getY() - 16, itemPos.getZ() - 16,
                    itemPos.getX() + 16, itemPos.getY() + 16, itemPos.getZ() + 16
                );

                // 先搜索所有ItemEntity，看看有哪些
                var allItemEntities = world.getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    searchBox,
                    entity -> true
                );

                MagicMod.LOGGER.info("[Magic] 在位置 {} 附近找到 {} 个ItemEntity", granitePos, allItemEntities.size());
                for (net.minecraft.entity.ItemEntity itemEntity : allItemEntities) {
                    MagicMod.LOGGER.info("[Magic] ItemEntity: 类型={}, 位置={}, 物品={}",
                        itemEntity.getClass().getSimpleName(),
                        itemEntity.getBlockPos(),
                        itemEntity.getStack().getItem());
                }

                var entities = world.getEntitiesByClass(
                    MagicCircleItemEntity.class,
                    searchBox,
                    entity -> true
                );
                MagicMod.LOGGER.info("[Magic] 在位置 {} 附近找到 {} 个魔法阵物品实体", granitePos, entities.size());

                for (MagicCircleItemEntity entity : entities) {
                    MagicMod.LOGGER.info("[Magic] 魔法阵物品实体，所有者: {}, 位置: {}",
                        entity.getOwnerUuid(), entity.getBlockPos());
                    // 简化逻辑：只要找到魔法阵物品实体就认为有效
                    // 位置已经匹配（花岗岩位置），不需要再检查ownerUuid
                    foundEntity = entity;
                    break;
                }

                if (foundEntity != null) {
                    // 更新物品实体UUID（如果变化了）
                    if (!foundEntity.getUuid().equals(data.magicCircleItemUuid)) {
                        data.magicCircleItemUuid = foundEntity.getUuid();
                        savedData.setMagicCircle(ownerUuid, data);
                    }

                    MagicMod.LOGGER.info("[Magic] 恢复玩家 {} 的魔法阵，位置: {}",
                        ownerUuid, granitePos);

                    // 无论所有者是否在线，都向当前加入的玩家发送特效数据包
                    // 这样当前玩家能看到世界上所有的魔法阵特效
                    double centerX = granitePos.getX() + 0.5;
                    double centerY = granitePos.getY() + 0.5;
                    double centerZ = granitePos.getZ() + 0.5;

                    com.magic.networking.MagicNetworking.sendMagicCirclePlaced(
                        player,  // 发送给当前加入的玩家，而不是所有者
                        new net.minecraft.util.math.Vec3d(centerX, centerY, centerZ),
                        ownerUuid  // 保留所有者UUID，用于客户端识别
                    );

                    restoredCount++;
                    MagicMod.LOGGER.info("[Magic] 已向玩家 {} 发送魔法阵特效激活数据包，所有者: {}", player.getName().getString(), ownerUuid);
                } else {
                    MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵花岗岩存在但物品实体缺失，位置: {}",
                        ownerUuid, granitePos);
                    savedData.clearMagicCircle(ownerUuid);
                }
            }

            MagicMod.LOGGER.info("[Magic] 成功恢复 {} 个魔法阵的粒子特效", restoredCount);

            // 检查当前玩家是否有魔法阵数据，如果没有则扫描世界
            PlayerData currentPlayerData = savedData.getPlayerData(playerUuid);
            if (!currentPlayerData.hasActiveMagicCircle()) {
                MagicMod.LOGGER.info("[Magic] 当前玩家 {} 没有魔法阵数据，开始扫描世界",
                    player.getName().getString());
                scanWorldForMagicCircle(player, world, playerUuid, savedData);
            }

        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 恢复魔法阵数据失败", e);
        }
    }
    
    /**
     * 扫描世界寻找玩家的魔法阵（当存档数据丢失时使用）
     */
    private static void scanWorldForMagicCircle(ServerPlayerEntity player, World world, UUID playerUuid, MagicCircleSavedData savedData) {
        try {
            MagicMod.LOGGER.info("[Magic] 存档中没有玩家 {} 的魔法阵数据，开始扫描世界", player.getName().getString());

            MagicCircleItemEntity foundEntity = null;

            // 扫描世界中的所有MagicCircleItemEntity
            Box worldBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);
            var entities = world.getEntitiesByClass(
                MagicCircleItemEntity.class,
                worldBox,
                entity -> true
            );

            MagicMod.LOGGER.info("[Magic] 扫描到 {} 个魔法阵物品实体", entities.size());

            for (MagicCircleItemEntity magicCircleEntity : entities) {
                UUID ownerUuid = magicCircleEntity.getOwnerUuid();
                MagicMod.LOGGER.info("[Magic] 魔法阵物品实体，所有者: {}, 位置: {}",
                    ownerUuid, magicCircleEntity.getBlockPos());

                // 检查所有者UUID是否匹配
                if (ownerUuid != null && ownerUuid.equals(playerUuid)) {
                    foundEntity = magicCircleEntity;
                    MagicMod.LOGGER.info("[Magic] 找到属于玩家 {} 的魔法阵物品实体", player.getName().getString());
                    break;
                }
            }

            if (foundEntity != null) {
                // 获取魔法阵位置
                BlockPos entityPos = foundEntity.getBlockPos();
                BlockPos granitePos = entityPos.down(); // 花岗岩在物品实体下方

                // 验证花岗岩是否存在
                if (world.getBlockState(granitePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
                    // 恢复魔法阵数据到存档
                    PlayerData data = savedData.getPlayerData(playerUuid);
                    data.setMagicCircle(
                        granitePos,
                        world.getRegistryKey().getValue().toString(),
                        foundEntity.getUuid()
                    );
                    savedData.setMagicCircle(playerUuid, data);

                    MagicMod.LOGGER.info("[Magic] 从世界扫描恢复玩家 {} 的魔法阵数据到存档，位置: {}",
                        player.getName().getString(), granitePos);

                    // 激活特效
                    double centerX = granitePos.getX() + 0.5;
                    double centerY = granitePos.getY() + 0.5;
                    double centerZ = granitePos.getZ() + 0.5;

                    com.magic.networking.MagicNetworking.sendMagicCirclePlaced(
                        player,
                        new net.minecraft.util.math.Vec3d(centerX, centerY, centerZ),
                        playerUuid
                    );
                } else {
                    MagicMod.LOGGER.warn("[Magic] 找到魔法阵物品实体，但花岗岩缺失，位置: {}", granitePos);
                }
            } else {
                MagicMod.LOGGER.info("[Magic] 扫描完成，未找到玩家 {} 的魔法阵物品实体", player.getName().getString());
            }
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 扫描世界寻找魔法阵失败", e);
        }
    }
    
    /**
     * 检查魔法阵传送是否在冷却中
     */
    public static boolean isMagicCircleOnCooldown(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.isMagicCircleOnCooldown(player.getEntityWorld());
    }
    
    /**
     * 获取魔法阵传送剩余冷却时间（秒）
     */
    public static int getMagicCircleCooldownRemainingSeconds(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.getMagicCircleCooldownRemainingSeconds(player.getEntityWorld());
    }
    
    /**
     * 开始魔法阵传送冷却
     */
    public static void startMagicCircleCooldown(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.error("[Magic] 无法获取存档数据，无法设置冷却时间");
            return;
        }
        
        PlayerData data = savedData.getPlayerData(player.getUuid());
        data.startMagicCircleCooldown(world);
        savedData.setMagicCircle(player.getUuid(), data);
        savedData.markDirty();
        
        MagicMod.LOGGER.info("[Magic] 玩家 {} 的魔法阵传送开始冷却", player.getName().getString());
    }
    
    // ==================== 魔法值系统 ====================
    
    /**
     * 检查玩家是否有足够的魔法值
     */
    public static boolean hasMana(ServerPlayerEntity player, int amount) {
        PlayerData data = getPlayerData(player);
        boolean hasEnough = data.hasMana(amount);
        MagicMod.LOGGER.info("[Magic] 玩家 {} 检查魔法值: 需要 {} 点，当前 {} 点，结果: {}", 
            player.getName().getString(), amount, data.getCurrentMana(), hasEnough);
        return hasEnough;
    }
    
    /**
     * 消耗魔法值
     * @return 是否成功消耗（魔法值不足时返回false）
     */
    public static boolean consumeMana(ServerPlayerEntity player, int amount) {
        MagicMod.LOGGER.info("[Magic] 玩家 {} 尝试消耗 {} 点魔法值", player.getName().getString(), amount);
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            MagicMod.LOGGER.error("[Magic] 保存数据为null，无法消耗魔法值");
            return false;
        }
        
        PlayerData data = savedData.getPlayerData(player.getUuid());
        boolean success = data.consumeMana(amount);
        MagicMod.LOGGER.info("[Magic] 消耗魔法值结果: {}", success);
        if (success) {
            savedData.setMagicCircle(player.getUuid(), data);
            savedData.markDirty();
        }
        return success;
    }
    
    /**
     * 获取玩家当前魔法值
     */
    public static int getCurrentMana(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.getCurrentMana();
    }
    
    /**
     * 获取玩家最大魔法值
     */
    public static int getMaxMana(ServerPlayerEntity player) {
        return PlayerData.MAX_MANA;
    }
    
    /**
     * 更新玩家魔法值恢复（每tick调用）
     */
    public static void tickManaRegen(ServerPlayerEntity player) {
        World world = player.getEntityWorld();
        MagicCircleSavedData savedData = getSavedData(world);
        if (savedData == null) {
            return;
        }
        
        PlayerData data = savedData.getPlayerData(player.getUuid());
        data.tickManaRegen();
        savedData.setMagicCircle(player.getUuid(), data);
        savedData.markDirty();
        
        // 同步魔法值到客户端（每20tick同步一次，避免过于频繁）
        if (world.getTime() % 20 == 0) {
            com.magic.networking.MagicNetworking.sendManaSync(
                player,
                data.getCurrentMana(),
                data.getMaxMana()
            );
        }
    }
}