package com.magic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Optional;
import java.util.UUID;

public class PlayerData {
    public boolean hasReceivedGuideBook = false;
    
    // 魔法阵相关数据
    public BlockPos magicCirclePos = null;
    public String magicCircleDimension = ""; // 使用空字符串代替null，避免CODEC序列化问题
    public boolean magicCircleActive = false;
    public UUID magicCircleItemUuid = null; // 魔法阵物品的UUID，用于识别所有者
    
    /**
     * 创建Codec
     */
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("hasReceivedGuideBook").forGetter(data -> data.hasReceivedGuideBook),
        BlockPos.CODEC.optionalFieldOf("magicCirclePos").forGetter(data -> Optional.ofNullable(data.magicCirclePos)),
        Codec.STRING.optionalFieldOf("magicCircleDimension").forGetter(data -> Optional.ofNullable(data.magicCircleDimension)),
        Codec.BOOL.fieldOf("magicCircleActive").forGetter(data -> data.magicCircleActive),
        Codec.STRING.optionalFieldOf("magicCircleItemUuid").forGetter(data -> data.magicCircleItemUuid != null ? Optional.of(data.magicCircleItemUuid.toString()) : Optional.empty())
    ).apply(instance, (hasReceivedGuideBook, magicCirclePos, magicCircleDimension, magicCircleActive, magicCircleItemUuid) -> {
        PlayerData data = new PlayerData();
        data.hasReceivedGuideBook = hasReceivedGuideBook;
        data.magicCirclePos = magicCirclePos.orElse(null);
        data.magicCircleDimension = magicCircleDimension.orElse(null);
        data.magicCircleActive = magicCircleActive;
        if (magicCircleItemUuid.isPresent() && !magicCircleItemUuid.get().isEmpty()) {
            try {
                data.magicCircleItemUuid = UUID.fromString(magicCircleItemUuid.get());
            } catch (IllegalArgumentException e) {
                data.magicCircleItemUuid = null;
            }
        }
        return data;
    }));
    
    public void writeNbt(NbtCompound nbt) {
        nbt.putBoolean("hasReceivedGuideBook", hasReceivedGuideBook);
        
        // 写入魔法阵数据
        if (magicCirclePos != null) {
            nbt.putInt("magicCircleX", magicCirclePos.getX());
            nbt.putInt("magicCircleY", magicCirclePos.getY());
            nbt.putInt("magicCircleZ", magicCirclePos.getZ());
        }
        if (magicCircleDimension != null) {
            nbt.putString("magicCircleDimension", magicCircleDimension);
        }
        nbt.putBoolean("magicCircleActive", magicCircleActive);
        if (magicCircleItemUuid != null) {
            nbt.putString("magicCircleItemUuid", magicCircleItemUuid.toString());
        }
    }
    
    public void readNbt(NbtCompound nbt) {
        hasReceivedGuideBook = nbt.getBoolean("hasReceivedGuideBook").orElse(false);
        
        // 读取魔法阵数据
        if (nbt.contains("magicCircleX") && nbt.contains("magicCircleY") && nbt.contains("magicCircleZ")) {
            int x = nbt.getInt("magicCircleX").orElse(0);
            int y = nbt.getInt("magicCircleY").orElse(0);
            int z = nbt.getInt("magicCircleZ").orElse(0);
            magicCirclePos = new BlockPos(x, y, z);
        }
        magicCircleDimension = nbt.getString("magicCircleDimension").orElse("");
        magicCircleActive = nbt.getBoolean("magicCircleActive").orElse(false);
        if (nbt.contains("magicCircleItemUuid")) {
            try {
                magicCircleItemUuid = UUID.fromString(nbt.getString("magicCircleItemUuid").orElse(""));
            } catch (IllegalArgumentException e) {
                magicCircleItemUuid = null;
            }
        }
    }
    
    // 设置魔法阵位置
    public void setMagicCircle(BlockPos pos, String dimension, UUID itemUuid) {
        this.magicCirclePos = pos;
        this.magicCircleDimension = dimension;
        this.magicCircleActive = true;
        this.magicCircleItemUuid = itemUuid;
    }
    
    // 清除魔法阵
    public void clearMagicCircle() {
        this.magicCirclePos = null;
        this.magicCircleDimension = "";
        this.magicCircleActive = false;
        this.magicCircleItemUuid = null;
    }
    
    // 检查是否有激活的魔法阵
    public boolean hasActiveMagicCircle() {
        return magicCircleActive && magicCirclePos != null && magicCircleDimension != null && !magicCircleDimension.isEmpty();
    }
} 