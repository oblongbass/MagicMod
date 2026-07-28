package com.magic.client;

import com.magic.item.ElementWandItem;
import com.magic.networking.ElementSwitchC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * 客户端按键处理：Shift+数字键 1-4 一键切换元素
 * 使用 START_CLIENT_TICK 在热键栏处理前拦截按键
 */
public class ElementKeyHandler {
    private static final int[] KEY_TO_ELEMENT = {3, 1, 0, 2};

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;
            // 直接检测潜行键的物理按键状态
            if (client.options.sneakKey == null || !client.options.sneakKey.isPressed()) return;
            if (!(client.player.getMainHandStack().getItem() instanceof ElementWandItem)) return;

            // 冷却检查（与快捷键共用，基于系统时间）
            if (System.currentTimeMillis() - KeyBindings.lastSwitchTime < KeyBindings.SWITCH_COOLDOWN_MS) return;

            // 保存当前快捷栏位置，防止 Minecraft 原版键盘处理切换了快捷栏
            int prevSlot = client.player.getInventory().getSelectedSlot();

            for (int i = 0; i < 4; i++) {
                var hotbarKey = client.options.hotbarKeys[i];
                if (hotbarKey != null && hotbarKey.wasPressed()) {
                    // 消耗按键事件后，Minecraft 的原版键盘处理仍可能切换快捷栏
                    // 因此强制恢复原来的选择
                    client.player.getInventory().setSelectedSlot(prevSlot);
                    ClientPlayNetworking.send(new ElementSwitchC2SPayload(KEY_TO_ELEMENT[i]));
                    ManaManager.setCurrentElement(KEY_TO_ELEMENT[i]); // 立即本地更新
                    KeyBindings.lastSwitchTime = System.currentTimeMillis();
                    return;
                }
            }
        });
    }
}
