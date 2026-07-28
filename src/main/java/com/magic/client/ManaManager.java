package com.magic.client;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ManaManager {
    private static int currentMana = 100;
    private static int maxMana = 100;
    private static boolean manaInitialized = false;
    private static int magicLevel = 0;
    private static boolean magicUnlocked = false;
    private static int currentElement = 3; // 默认土元素

    public static void setMana(int current, int max) {
        currentMana = current;
        maxMana = max;
        manaInitialized = true;
    }

    public static int getCurrentMana() {
        return currentMana;
    }

    public static int getMaxMana() {
        return maxMana;
    }

    public static boolean isInitialized() {
        return manaInitialized;
    }

    public static void setMagicLevel(int level) {
        magicLevel = level;
    }

    public static int getMagicLevel() {
        return magicLevel;
    }

    public static void setMagicUnlocked(boolean unlocked) {
        magicUnlocked = unlocked;
    }

    public static boolean isMagicUnlocked() {
        return magicUnlocked;
    }

    public static void setCurrentElement(int element) {
        currentElement = element;
        HudRenderer.setDisplayElement(element); // 同步 HUD 显示
    }

    public static int getCurrentElement() {
        return currentElement;
    }

    public static void reset() {
        currentMana = 100;
        maxMana = 100;
        manaInitialized = false;
        magicLevel = 0;
        magicUnlocked = false;
        currentElement = 3;
    }
}
