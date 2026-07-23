package com.magic.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (teleportToMagicCircleKey.wasPressed()) {
                onTeleportKeyPressed(client);
            }
            while (openMagicScreenKey.wasPressed()) {
                onOpenMagicScreenKeyPressed(client);
            }
        });
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