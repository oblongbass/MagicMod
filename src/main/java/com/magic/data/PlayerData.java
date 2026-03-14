package com.magic.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.magic.MagicMod;
import java.util.Optional;
import java.util.UUID;

public class PlayerData {
    public boolean hasReceivedGuideBook = false;
    
    // 魔法阵相关数据
    public BlockPos magicCirclePos = null;
    public String magicCircleDimension = ""; // 使用空字符串代替null，避免CODEC序列化问题
    public boolean magicCircleActive = false;
    public UUID magicCircleItemUuid = null; // 魔法阵物品的UUID，用于识别所有者
    
    // 魔法阵传送冷却
    private static final int MAGIC_CIRCLE_COOLDOWN_SECONDS = 30; // 30秒冷却时间
    private long magicCircleCooldownEndTime = 0; // 冷却结束时间戳（游戏刻）
    
    // 魔法值系统
    public static final int MAX_MANA = 100; // 最大魔法值
    public int currentMana = MAX_MANA; // 当前魔法值
    private int manaRegenTickCounter = 0; // 魔法值恢复计时器，每20tick恢复1点
    
    /**
     * 创建Codec
     */
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("hasReceivedGuideBook").forGetter(data -> data.hasReceivedGuideBook),
        BlockPos.CODEC.optionalFieldOf("magicCirclePos").forGetter(data -> Optional.ofNullable(data.magicCirclePos)),
        Codec.STRING.optionalFieldOf("magicCircleDimension").forGetter(data -> data.magicCircleDimension != null ? Optional.of(data.magicCircleDimension) : Optional.empty()),
        Codec.BOOL.fieldOf("magicCircleActive").forGetter(data -> data.magicCircleActive),
        Codec.STRING.optionalFieldOf("magicCircleItemUuid").forGetter(data -> data.magicCircleItemUuid != null ? Optional.of(data.magicCircleItemUuid.toString()) : Optional.empty()),
        Codec.LONG.optionalFieldOf("magicCircleCooldownEndTime").forGetter(data -> Optional.of(data.magicCircleCooldownEndTime)),
        Codec.INT.optionalFieldOf("currentMana").forGetter(data -> Optional.of(data.currentMana))
    ).apply(instance, (hasReceivedGuideBook, magicCirclePos, magicCircleDimension, magicCircleActive, magicCircleItemUuid, magicCircleCooldownEndTime, currentMana) -> {
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
        data.magicCircleCooldownEndTime = magicCircleCooldownEndTime.orElse(0L);
        data.currentMana = currentMana.orElse(MAX_MANA);
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
        
        // 写入冷却时间
        nbt.putLong("magicCircleCooldownEndTime", magicCircleCooldownEndTime);
        
        // 写入魔法值
        nbt.putInt("currentMana", currentMana);
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
        
        // 读取冷却时间
        magicCircleCooldownEndTime = nbt.getLong("magicCircleCooldownEndTime").orElse(0L);
        
        // 读取魔法值
        currentMana = nbt.getInt("currentMana").orElse(MAX_MANA);
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
    
    // 检查魔法阵传送是否在冷却中
    public boolean isMagicCircleOnCooldown(World world) {
        if (world == null) {
            return false;
        }
        long currentTime = world.getTime();
        return currentTime < magicCircleCooldownEndTime;
    }
    
    // 获取魔法阵传送剩余冷却时间（秒）
    public int getMagicCircleCooldownRemainingSeconds(World world) {
        if (world == null || !isMagicCircleOnCooldown(world)) {
            return 0;
        }
        long currentTime = world.getTime();
        long remainingTicks = magicCircleCooldownEndTime - currentTime;
        return (int) Math.max(0, remainingTicks / 20); // 转换为秒
    }
    
    // 开始魔法阵传送冷却
    public void startMagicCircleCooldown(World world) {
        if (world == null) {
            return;
        }
        long currentTime = world.getTime();
        magicCircleCooldownEndTime = currentTime + (MAGIC_CIRCLE_COOLDOWN_SECONDS * 20); // 转换为游戏刻
    }
    
    // ==================== 魔法值系统 ====================
    
    /**
     * 检查魔法值是否足够
     */
    public boolean hasMana(int amount) {
        return currentMana >= amount;
    }
    
    /**
     * 消耗魔法值
     * @return 是否成功消耗（魔法值不足时返回false）
     */
    public boolean consumeMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            MagicMod.LOGGER.info("[Magic] 消耗魔法值 {} 点，剩余 {} 点", amount, currentMana);
            return true;
        }
        MagicMod.LOGGER.warn("[Magic] 魔法值不足！需要 {} 点，当前 {} 点", amount, currentMana);
        return false;
    }
    
    /**
     * 恢复魔法值
     */
    public void restoreMana(int amount) {
        currentMana = Math.min(MAX_MANA, currentMana + amount);
    }
    
    /**
     * 恢复魔法值（每tick调用）
     * 每秒恢复1点，即每20tick恢复1点
     */
    public void tickManaRegen() {
        if (currentMana < MAX_MANA) {
            manaRegenTickCounter++;
            if (manaRegenTickCounter >= 20) { // 20 tick = 1 second
                currentMana = Math.min(MAX_MANA, currentMana + 1);
                manaRegenTickCounter = 0;
            }
        }
    }
    
    /**
     * 获取当前魔法值
     */
    public int getCurrentMana() {
        return currentMana;
    }
    
    /**
     * 获取最大魔法值
     */
    public int getMaxMana() {
        return MAX_MANA;
    }
    
    /**
     * 重置魔法值恢复计时器（用于新加入游戏时）
     */
    public void resetManaRegenTimer() {
        manaRegenTickCounter = 0;
    }
} 