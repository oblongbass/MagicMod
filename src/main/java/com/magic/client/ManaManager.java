package com.magic.client;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

/**
 * 客户端魔法值管理器
 * 用于在客户端显示魔法值
 */
@Environment(EnvType.CLIENT)
public class ManaManager {
    private static int currentMana = 100;
    private static int maxMana = 100;
    private static boolean manaInitialized = false;
    
    /**
     * 更新客户端魔法值（从服务器接收）
     */
    public static void setMana(int current, int max) {
        currentMana = current;
        maxMana = max;
        manaInitialized = true;
    }
    
    /**
     * 获取当前魔法值
     */
    public static int getCurrentMana() {
        return currentMana;
    }
    
    /**
     * 获取最大魔法值
     */
    public static int getMaxMana() {
        return maxMana;
    }
    
    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return manaInitialized;
    }
    
    /**
     * 重置（玩家退出时调用）
     */
    public static void reset() {
        currentMana = 100;
        maxMana = 100;
        manaInitialized = false;
    }
}
