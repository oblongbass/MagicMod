package com.magic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import com.magic.MagicMod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 魔法阵存档数据
 * 使用Minecraft的SavedData系统，每个存档有自己的魔法阵数据
 */
public class MagicCircleSavedData extends PersistentState {
    private static final String DATA_NAME = "magic_circles";
    
    // 存储所有玩家的魔法阵数据
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    public MagicCircleSavedData() {
        super();
    }
    
    /**
     * 创建Codec
     */
    public static final Codec<MagicCircleSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, PlayerData.CODEC).fieldOf("players").forGetter(data -> {
            MagicMod.LOGGER.info("[Magic] 序列化MagicCircleSavedData，玩家数量: {}", data.playerDataMap.size());
            // 将UUID转换为字符串
            Map<String, PlayerData> stringMap = new HashMap<>();
            data.playerDataMap.forEach((uuid, playerData) -> {
                MagicMod.LOGGER.info("[Magic] 序列化玩家 {} 的魔法阵数据: 有激活魔法阵={}, 位置={}, 维度={}, magicCircleActive={}",
                    uuid, playerData.hasActiveMagicCircle(), playerData.magicCirclePos, playerData.magicCircleDimension, playerData.magicCircleActive);
                stringMap.put(uuid.toString(), playerData);
            });
            return stringMap;
        })
    ).apply(instance, MagicCircleSavedData::createFromMap));
    
    /**
     * 从Map创建MagicCircleSavedData
     */
    private static MagicCircleSavedData createFromMap(Map<String, PlayerData> stringMap) {
        MagicMod.LOGGER.info("[Magic] 从Map创建MagicCircleSavedData，玩家数量: {}", stringMap.size());
        MagicCircleSavedData data = new MagicCircleSavedData();
        // 将字符串转换回UUID
        stringMap.forEach((uuidStr, playerData) -> {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                data.playerDataMap.put(uuid, playerData);
                MagicMod.LOGGER.info("[Magic] 加载玩家 {} 的魔法阵数据: 有激活魔法阵={}, 位置={}, 维度={}",
                    uuidStr, playerData.hasActiveMagicCircle(), playerData.magicCirclePos, playerData.magicCircleDimension);
            } catch (IllegalArgumentException e) {
                MagicMod.LOGGER.warn("[Magic] 无效的玩家UUID: {}", uuidStr);
            }
        });
        return data;
    }
    
    /**
     * 创建PersistentStateType
     */
    private static final PersistentStateType<MagicCircleSavedData> TYPE = new PersistentStateType<>(
        (String) MagicMod.MOD_ID + "_" + DATA_NAME,
        (Supplier<MagicCircleSavedData>) MagicCircleSavedData::new,
        CODEC,
        null
    );
    
    /**
     * 获取或创建存档数据
     */
    public static MagicCircleSavedData getOrCreate(World world) {
        MinecraftServer server = world.getServer();
        if (server == null) {
            MagicMod.LOGGER.warn("[Magic] 无法获取服务器实例");
            return null;
        }
        
        // 从服务器的数据存储中获取
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(UUID playerUuid) {
        return playerDataMap.computeIfAbsent(playerUuid, uuid -> new PlayerData());
    }
    
    /**
     * 设置魔法阵
     */
    public void setMagicCircle(UUID playerUuid, PlayerData data) {
        playerDataMap.put(playerUuid, data);
        markDirty();
    }
    
    /**
     * 清除魔法阵
     */
    public void clearMagicCircle(UUID playerUuid) {
        PlayerData data = playerDataMap.get(playerUuid);
        if (data != null) {
            data.clearMagicCircle();
            markDirty();
        }
    }
    
    /**
     * 获取所有玩家数据
     */
    public Map<UUID, PlayerData> getAllPlayerData() {
        return new HashMap<>(playerDataMap);
    }
    
    /**
     * 检查是否有激活的魔法阵
     */
    public boolean hasActiveMagicCircle(UUID playerUuid) {
        PlayerData data = playerDataMap.get(playerUuid);
        return data != null && data.hasActiveMagicCircle();
    }
}