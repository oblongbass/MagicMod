package com.magic.client;

import com.magic.networking.ElementSwitchC2SPayload;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class KeyBindings {
    private static KeyBinding teleportToMagicCircleKey;
    private static KeyBinding openMagicScreenKey;
    // 元素快捷键
    public static KeyBinding elementWaterKey;   // 水 Z
    public static KeyBinding elementFireKey;    // 火 X
    public static KeyBinding elementEarthKey;   // 土 V
    public static KeyBinding elementWindKey;    // 风 R
    public static long lastSwitchTime = 0;
    public static final long SWITCH_COOLDOWN_MS = 500; // 0.5秒

    private static final KeyBinding.Category MAGIC_CATEGORY = KeyBinding.Category.create(
        Identifier.of("magic-mod", "magic")
    );

    public static void register() {
        teleportToMagicCircleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.teleport_to_magic_circle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            MAGIC_CATEGORY
        ));

        openMagicScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.open_magic_screen",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            MAGIC_CATEGORY
        ));

        // 元素快捷键
        elementWaterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.element_water",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            MAGIC_CATEGORY
        ));
        elementFireKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.element_fire",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            MAGIC_CATEGORY
        ));
        elementEarthKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.element_earth",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            MAGIC_CATEGORY
        ));
        elementWindKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.element_wind",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            MAGIC_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (teleportToMagicCircleKey.wasPressed()) {
                onTeleportKeyPressed(client);
            }
            while (openMagicScreenKey.wasPressed()) {
                onOpenMagicScreenKeyPressed(client);
            }

            // 处理元素快捷键
            handleElementKeys(client);
        });
    }

    private static void handleElementKeys(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;

        if (System.currentTimeMillis() - lastSwitchTime < SWITCH_COOLDOWN_MS) return;

        KeyBinding[] elementKeys = {elementWaterKey, elementFireKey, elementEarthKey, elementWindKey};
        int[] elements = {1, 0, 3, 2}; // 水=1, 火=0, 土=3, 风=2

        for (int i = 0; i < 4; i++) {
            if (elementKeys[i].wasPressed()) {
                ClientPlayNetworking.send(new ElementSwitchC2SPayload(elements[i]));
                ManaManager.setCurrentElement(elements[i]); // 立即本地更新，不等待网络
                lastSwitchTime = System.currentTimeMillis();
            }
        }
    }

    private static void onTeleportKeyPressed(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        client.getNetworkHandler().sendChatCommand("magic_teleport_to_circle");
    }

    private static void onOpenMagicScreenKeyPressed(MinecraftClient client) {
        if (client.player == null) return;
        client.setScreen(new com.magic.client.screen.MagicScreen());
    }
}