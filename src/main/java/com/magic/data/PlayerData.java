package com.magic.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Optional;
import java.util.UUID;

public class PlayerData {
    public boolean hasReceivedGuideBook = false;
    
    // 魔法阵相关数据
    public BlockPos magicCirclePos = null;
    public String magicCircleDimension = null;
    public boolean magicCircleActive = false;
    public UUID magicCircleItemUuid = null; // 魔法阵物品的UUID，用于识别所有者
    
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
        magicCircleDimension = nbt.getString("magicCircleDimension").orElse(null);
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
        this.magicCircleDimension = null;
        this.magicCircleActive = false;
        this.magicCircleItemUuid = null;
    }
    
    // 检查是否有激活的魔法阵
    public boolean hasActiveMagicCircle() {
        return magicCircleActive && magicCirclePos != null && magicCircleDimension != null;
    }
} 