package com.magic.client;

import com.magic.item.ElementWandItem;
import com.magic.networking.ElementSwitchC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键处理：Shift+数字键 1-4 一键切换元素
 * 使用 START_CLIENT_TICK 在热键栏处理前拦截按键，切换后再重置热键栏状态
 */
public class ElementKeyHandler {
    private static final int COOLDOWN_TICKS = 10; // 0.5秒
    private static final int[] KEY_TO_ELEMENT = {3, 1, 0, 2};
    private static int lastSwitchTick = 0;

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;
            if (!client.player.isSneaking()) return;
            if (!(client.player.getMainHandStack().getItem() instanceof ElementWandItem)) return;

            int currentTick = client.player.age;

            // 冷却检查
            if (currentTick - lastSwitchTick < COOLDOWN_TICKS) return;

            for (int i = 0; i < 4; i++) {
                var hotbarKey = client.options.hotbarKeys[i];
                if (hotbarKey != null && hotbarKey.wasPressed()) {
                    ClientPlayNetworking.send(new ElementSwitchC2SPayload(KEY_TO_ELEMENT[i]));
                    lastSwitchTick = currentTick;
                    return;
                }
            }
        });
    }
}
