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
    public String magicCircleDimension = "";
    public boolean magicCircleActive = false;
    public UUID magicCircleItemUuid = null;

    // 魔法阵传送冷却
    private static final int MAGIC_CIRCLE_COOLDOWN_SECONDS = 30;
    private long magicCircleCooldownEndTime = 0;

    // 魔法值系统
    public static final int BASE_MAX_MANA = 100;
    public static final int MANA_PER_LEVEL = 50;
    public int currentMana = BASE_MAX_MANA;
    private int manaRegenTickCounter = 0;

    // 魔法级系统
    public static final int MAX_MAGIC_LEVEL = 3;
    public int magicLevel = 0;
    public boolean magicUnlocked = false;

    // 当前元素（0=火 1=水 2=风 3=土）
    public static final int DEFAULT_ELEMENT = 3; // 默认土元素
    public int currentElement = DEFAULT_ELEMENT;

    /** 升级消耗(魔法水晶数量) */
    public static int getUpgradeCost(int targetLevel) {
        return targetLevel * 2;
    }

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
        Codec.INT.optionalFieldOf("currentMana").forGetter(data -> Optional.of(data.currentMana)),
        Codec.INT.optionalFieldOf("magicLevel").forGetter(data -> Optional.of(data.magicLevel)),
        Codec.BOOL.optionalFieldOf("magicUnlocked").forGetter(data -> Optional.of(data.magicUnlocked)),
        Codec.INT.optionalFieldOf("currentElement").forGetter(data -> Optional.of(data.currentElement))
    ).apply(instance, (hasReceivedGuideBook, magicCirclePos, magicCircleDimension, magicCircleActive, magicCircleItemUuid, magicCircleCooldownEndTime, currentMana, magicLevel, magicUnlocked, currentElement) -> {
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
        data.currentMana = currentMana.orElse(BASE_MAX_MANA);
        data.magicLevel = magicLevel.orElse(0);
        data.magicUnlocked = magicUnlocked.orElse(false);
        data.currentElement = currentElement.orElse(DEFAULT_ELEMENT);
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

        nbt.putInt("currentMana", currentMana);
        nbt.putInt("magicLevel", magicLevel);
        nbt.putBoolean("magicUnlocked", magicUnlocked);
        nbt.putInt("currentElement", currentElement);
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

        magicCircleCooldownEndTime = nbt.getLong("magicCircleCooldownEndTime").orElse(0L);

        currentMana = nbt.getInt("currentMana").orElse(BASE_MAX_MANA);
        magicLevel = nbt.getInt("magicLevel").orElse(0);
        magicUnlocked = nbt.getBoolean("magicUnlocked").orElse(false);
        currentElement = nbt.getInt("currentElement").orElse(DEFAULT_ELEMENT);
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

    // 获取魔法阵传送剩余冷却时间(秒)
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
     * @return 是否成功消耗(魔法值不足时返回false)
     */
    public boolean consumeMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            MagicMod.LOGGER.info("[Magic] 消耗魔法值 {} 点,剩余 {} 点", amount, currentMana);
            return true;
        }
        MagicMod.LOGGER.warn("[Magic] 魔法值不足!需要 {} 点,当前 {} 点", amount, currentMana);
        return false;
    }

    /**
     * 恢复魔法值
     */
    public void restoreMana(int amount) {
        currentMana = Math.min(getMaxMana(), currentMana + amount);
    }

    /**
     * 恢复魔法值（每tick调用）
     * 每秒恢复1点，即每20tick恢复1点
     */
    public void tickManaRegen() {
        int max = getMaxMana();
        if (currentMana < max) {
            manaRegenTickCounter++;
            if (manaRegenTickCounter >= 20) {
                currentMana = Math.min(max, currentMana + 1);
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
     * 获取最大魔法值（根据魔法等级动态计算：100 + 等级×50）
     */
    public int getMaxMana() {
        return BASE_MAX_MANA + magicLevel * MANA_PER_LEVEL;
    }

    /**
     * 尝试升级魔法等级
     * @return 是否升级成功（已达到最高等级返回false）
     */
    public boolean tryUpgrade() {
        if (magicLevel >= MAX_MAGIC_LEVEL) {
            return false;
        }
        magicLevel++;
        currentMana = getMaxMana();
        return true;
    }

    /**
     * 重置魔法值恢复计时器(用于新加入游戏时)
     */
    public void resetManaRegenTimer() {
        manaRegenTickCounter = 0;
    }

    public boolean isMagicUnlocked() {
        return magicUnlocked;
    }

    public void unlockMagic() {
        this.magicUnlocked = true;
        MagicMod.LOGGER.info("[Magic] 玩家魔法技能已激活!");
    }

    public int getMagicLevel() {
        return magicLevel;
    }

    public int getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(int element) {
        this.currentElement = element;
    }

    public void setMagicLevel(int level) {
        this.magicLevel = Math.min(MAX_MAGIC_LEVEL, Math.max(0, level));
    }

    public void incrementMagicLevel() {
        if (magicLevel < MAX_MAGIC_LEVEL) {
            magicLevel++;
            MagicMod.LOGGER.info("[Magic] 魔法级提升到 {} 级!", magicLevel);
        }
    }
}