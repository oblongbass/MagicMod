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
 * 玩家数据管理器 - 使用文件持久化存储
 */
public class SimplePlayerDataManager {
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    static {
        // 在类加载时从文件加载数据
        loadFromFile();
    }
    
    /**
     * 从文件加载所有玩家数据
     */
    public static void loadFromFile() {
        try {
            Map<UUID, PlayerData> loadedData = MagicCircleDataStorage.loadAllData();
            playerDataMap.clear();
            playerDataMap.putAll(loadedData);
            MagicMod.LOGGER.info("[Magic] 从文件加载了 {} 个玩家的魔法阵数据", loadedData.size());
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 从文件加载数据失败", e);
        }
    }
    
    /**
     * 保存所有玩家数据到文件
     */
    public static void saveToFile() {
        try {
            MagicCircleDataStorage.saveAllData(playerDataMap);
            MagicMod.LOGGER.info("[Magic] 保存了 {} 个玩家的魔法阵数据到文件", playerDataMap.size());
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 保存数据到文件失败", e);
        }
    }
    
    /**
     * 保存单个玩家的数据到文件
     */
    private static void savePlayerDataToFile(UUID playerUuid) {
        try {
            PlayerData data = playerDataMap.get(playerUuid);
            if (data != null) {
                MagicCircleDataStorage.savePlayerData(playerUuid, data);
            }
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 保存玩家数据到文件失败: {}", playerUuid, e);
        }
    }
    
    public static PlayerData getPlayerData(ServerPlayerEntity player) {
        return playerDataMap.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }
    
    public static void setMagicCircle(ServerPlayerEntity player, BlockPos pos, String dimension, UUID itemEntityUuid) {
        PlayerData data = getPlayerData(player);
        data.setMagicCircle(pos, dimension, itemEntityUuid);
        // 保存到文件
        savePlayerDataToFile(player.getUuid());
    }
    
    public static void clearMagicCircle(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        data.clearMagicCircle();
        // 保存到文件
        savePlayerDataToFile(player.getUuid());
    }
    
    public static boolean hasActiveMagicCircle(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.hasActiveMagicCircle();
    }
    
    public static BlockPos getMagicCirclePos(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCirclePos;
    }
    
    public static String getMagicCircleDimension(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCircleDimension;
    }
    
    public static UUID getMagicCircleItemEntityUuid(ServerPlayerEntity player) {
        PlayerData data = getPlayerData(player);
        return data.magicCircleItemUuid;
    }
    
    public static void setMagicCircleItemEntityUuid(ServerPlayerEntity player, UUID itemEntityUuid) {
        PlayerData data = getPlayerData(player);
        data.magicCircleItemUuid = itemEntityUuid;
        // 保存到文件
        savePlayerDataToFile(player.getUuid());
    }
    
    /**
     * 验证并恢复玩家的魔法阵数据
     * 当玩家重新加入游戏时调用，验证文件数据与世界的匹配情况
     */
    public static void restoreMagicCircleFromWorld(ServerPlayerEntity player) {
        try {
            World world = player.getWorld();
            UUID playerUuid = player.getUuid();
            
            PlayerData data = getPlayerData(player);
            
            // 检查文件中是否有魔法阵数据
            if (data.hasActiveMagicCircle() && data.magicCirclePos != null && data.magicCircleDimension != null) {
                // 验证当前维度是否匹配
                String currentDimension = world.getRegistryKey().getValue().toString();
                if (!currentDimension.equals(data.magicCircleDimension)) {
                    MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵在维度 {}，但当前维度是 {}",
                        player.getName().getString(), data.magicCircleDimension, currentDimension);
                    // 维度不匹配，可能在不同维度，不激活特效
                    return;
                }
                
                // 验证花岗岩是否存在
                BlockPos granitePos = data.magicCirclePos;
                if (world.getBlockState(granitePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
                    // 查找魔法阵物品实体
                    MagicCircleItemEntity foundEntity = null;
                    BlockPos itemPos = granitePos.up(); // 物品实体在花岗岩上方
                    
                    // 在花岗岩上方小范围内搜索物品实体
                    Box searchBox = new Box(
                        itemPos.getX() - 1, itemPos.getY() - 1, itemPos.getZ() - 1,
                        itemPos.getX() + 2, itemPos.getY() + 2, itemPos.getZ() + 2
                    );
                    
                    var entities = world.getEntitiesByClass(
                        MagicCircleItemEntity.class,
                        searchBox,
                        entity -> true
                    );
                    
                    for (MagicCircleItemEntity entity : entities) {
                        // 验证所有者
                        if (playerUuid.equals(entity.getOwnerUuid())) {
                            foundEntity = entity;
                            break;
                        }
                    }
                    
                    if (foundEntity != null) {
                        // 更新物品实体UUID（如果变化了）
                        if (!foundEntity.getUuid().equals(data.magicCircleItemUuid)) {
                            data.magicCircleItemUuid = foundEntity.getUuid();
                            savePlayerDataToFile(playerUuid);
                        }
                        
                        MagicMod.LOGGER.info("[Magic] 验证玩家 {} 的魔法阵成功，位置: {}", 
                            player.getName().getString(), granitePos);
                        
                        // 发送网络数据包激活客户端特效
                        double centerX = granitePos.getX() + 0.5;
                        double centerY = granitePos.getY() + 0.5;
                        double centerZ = granitePos.getZ() + 0.5;
                        
                        com.magic.networking.MagicNetworking.sendMagicCirclePlaced(
                            player, 
                            new net.minecraft.util.math.Vec3d(centerX, centerY, centerZ)
                        );
                        
                    } else {
                        MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵花岗岩存在但物品实体缺失",
                            player.getName().getString());
                        // 花岗岩存在但物品实体丢失，清理无效数据
                        data.clearMagicCircle();
                        savePlayerDataToFile(playerUuid);
                    }
                } else {
                    MagicMod.LOGGER.warn("[Magic] 玩家 {} 的魔法阵花岗岩缺失，清理数据",
                        player.getName().getString());
                    // 花岗岩不存在，清理无效数据
                    data.clearMagicCircle();
                    savePlayerDataToFile(playerUuid);
                }
            } else {
                // 文件中没有魔法阵数据，但可以扫描世界寻找（可能配置文件丢失）
                scanWorldForMagicCircle(player, world, playerUuid);
            }
            
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 恢复魔法阵数据失败", e);
        }
    }
    
    /**
     * 扫描世界寻找玩家的魔法阵（当配置文件数据丢失时使用）
     */
    private static void scanWorldForMagicCircle(ServerPlayerEntity player, World world, UUID playerUuid) {
        try {
            MagicCircleItemEntity foundEntity = null;
            
            // 扫描世界中的所有MagicCircleItemEntity
            Box worldBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);
            var entities = world.getEntitiesByClass(
                MagicCircleItemEntity.class,
                worldBox,
                entity -> true
            );
            
            for (MagicCircleItemEntity magicCircleEntity : entities) {
                // 检查所有者UUID是否匹配
                UUID ownerUuid = magicCircleEntity.getOwnerUuid();
                if (ownerUuid != null && ownerUuid.equals(playerUuid)) {
                    foundEntity = magicCircleEntity;
                    break;
                }
            }
            
            if (foundEntity != null) {
                // 获取魔法阵位置
                BlockPos entityPos = foundEntity.getBlockPos();
                BlockPos granitePos = entityPos.down(); // 花岗岩在物品实体下方
                
                // 验证花岗岩是否存在
                if (world.getBlockState(granitePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
                    // 恢复魔法阵数据到文件
                    PlayerData data = getPlayerData(player);
                    data.setMagicCircle(
                        granitePos,
                        world.getRegistryKey().getValue().toString(),
                        foundEntity.getUuid()
                    );
                    savePlayerDataToFile(playerUuid);
                    
                    MagicMod.LOGGER.info("[Magic] 从世界扫描恢复玩家 {} 的魔法阵数据，位置: {}", 
                        player.getName().getString(), granitePos);
                    
                    // 激活特效
                    double centerX = granitePos.getX() + 0.5;
                    double centerY = granitePos.getY() + 0.5;
                    double centerZ = granitePos.getZ() + 0.5;
                    
                    com.magic.networking.MagicNetworking.sendMagicCirclePlaced(
                        player, 
                        new net.minecraft.util.math.Vec3d(centerX, centerY, centerZ)
                    );
                }
            }
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 扫描世界寻找魔法阵失败", e);
        }
    }
}