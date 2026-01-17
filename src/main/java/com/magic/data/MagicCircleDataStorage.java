package com.magic.data;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import com.magic.MagicMod;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.UUID;

/**
 * 魔法阵数据持久化存储
 * 使用简单的文本文件格式存储魔法阵数据，确保游戏完全退出后数据不丢失
 * 文件格式：每行一个魔法阵记录
 * UUID,维度,X,Y,Z,物品实体UUID,激活状态
 */
public class MagicCircleDataStorage {
    private static final String FILE_NAME = "magic_circles.txt";
    private static final String CONFIG_DIR = "config/magic-mod/";
    private static Path configFilePath;
    
    static {
        // 初始化配置文件路径
        try {
            // 获取Minecraft运行目录
            String gameDir = System.getProperty("user.dir");
            Path configDir = Paths.get(gameDir, CONFIG_DIR);
            Files.createDirectories(configDir);
            configFilePath = configDir.resolve(FILE_NAME);
            MagicMod.LOGGER.info("[Magic] 配置文件路径: {}", configFilePath);
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 初始化配置文件路径失败", e);
            configFilePath = null;
        }
    }
    
    /**
     * 保存所有魔法阵数据到文件
     */
    public static void saveAllData(Map<UUID, PlayerData> playerDataMap) {
        if (configFilePath == null) {
            MagicMod.LOGGER.error("[Magic] 配置文件路径未初始化，无法保存数据");
            return;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(configFilePath, java.nio.charset.StandardCharsets.UTF_8)) {
            int savedCount = 0;
            
            for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                UUID playerUuid = entry.getKey();
                PlayerData data = entry.getValue();
                
                // 只保存有激活魔法阵的玩家数据
                if (data.hasActiveMagicCircle() && data.magicCirclePos != null && data.magicCircleDimension != null) {
                    // 格式：玩家UUID,维度,X,Y,Z,物品实体UUID,激活状态
                    String line = String.format("%s,%s,%d,%d,%d,%s,%b",
                        playerUuid.toString(),
                        data.magicCircleDimension,
                        data.magicCirclePos.getX(),
                        data.magicCirclePos.getY(),
                        data.magicCirclePos.getZ(),
                        data.magicCircleItemUuid != null ? data.magicCircleItemUuid.toString() : "null",
                        data.magicCircleActive
                    );
                    writer.write(line);
                    writer.newLine();
                    savedCount++;
                }
            }
            
            MagicMod.LOGGER.info("[Magic] 保存了 {} 个魔法阵数据到文件", savedCount);
            
        } catch (IOException e) {
            MagicMod.LOGGER.error("[Magic] 保存魔法阵数据到文件失败", e);
        }
    }
    
    /**
     * 从文件加载所有魔法阵数据
     */
    public static Map<UUID, PlayerData> loadAllData() {
        Map<UUID, PlayerData> playerDataMap = new HashMap<>();
        
        if (configFilePath == null || !Files.exists(configFilePath)) {
            MagicMod.LOGGER.info("[Magic] 配置文件不存在，使用空数据");
            return playerDataMap;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(configFilePath, java.nio.charset.StandardCharsets.UTF_8)) {
            String line;
            int loadedCount = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // 跳过空行和注释
                }
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        UUID playerUuid = UUID.fromString(parts[0]);
                        String dimension = parts[1];
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        int z = Integer.parseInt(parts[4]);
                        String itemUuidStr = parts[5];
                        boolean active = Boolean.parseBoolean(parts[6]);
                        
                        // 创建PlayerData
                        PlayerData data = new PlayerData();
                        BlockPos pos = new BlockPos(x, y, z);
                        
                        UUID itemUuid = null;
                        if (!"null".equals(itemUuidStr)) {
                            itemUuid = UUID.fromString(itemUuidStr);
                        }
                        
                        data.setMagicCircle(pos, dimension, itemUuid);
                        if (!active) {
                            data.magicCircleActive = false;
                        }
                        
                        playerDataMap.put(playerUuid, data);
                        loadedCount++;
                    } else {
                        MagicMod.LOGGER.warn("[Magic] 配置文件行格式错误: {}", line);
                    }
                } catch (Exception e) {
                    MagicMod.LOGGER.warn("[Magic] 解析配置文件行失败: {} - {}", line, e.getMessage());
                }
            }
            
            MagicMod.LOGGER.info("[Magic] 从文件加载了 {} 个魔法阵数据", loadedCount);
            
        } catch (IOException e) {
            MagicMod.LOGGER.error("[Magic] 加载魔法阵数据文件失败", e);
        }
        
        return playerDataMap;
    }
    
    /**
     * 保存单个玩家的魔法阵数据
     */
    public static void savePlayerData(UUID playerUuid, PlayerData data) {
        if (configFilePath == null) {
            MagicMod.LOGGER.error("[Magic] 配置文件路径未初始化，无法保存数据");
            return;
        }
        
        // 先加载现有数据
        Map<UUID, PlayerData> allData = loadAllData();
        
        // 更新或添加当前玩家数据
        allData.put(playerUuid, data);
        
        // 保存所有数据
        saveAllData(allData);
    }
    
    /**
     * 删除单个玩家的魔法阵数据
     */
    public static void deletePlayerData(UUID playerUuid) {
        if (configFilePath == null) {
            MagicMod.LOGGER.error("[Magic] 配置文件路径未初始化，无法删除数据");
            return;
        }
        
        // 先加载现有数据
        Map<UUID, PlayerData> allData = loadAllData();
        
        // 删除玩家数据
        allData.remove(playerUuid);
        
        // 保存更新后的数据
        saveAllData(allData);
    }
}