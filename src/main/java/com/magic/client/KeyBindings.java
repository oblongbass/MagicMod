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
    private static final KeyBinding.Category MAGIC_CATEGORY = KeyBinding.Category.create(
        Identifier.of("magic-mod", "magic")
    );
    
    public static void register() {
        // 注册F7键绑定
        teleportToMagicCircleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magic-mod.teleport_to_magic_circle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            MAGIC_CATEGORY
        ));
        
        // 注册客户端tick事件监听器
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (teleportToMagicCircleKey.wasPressed()) {
                onTeleportKeyPressed(client);
            }
        });
    }
    
    private static void onTeleportKeyPressed(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        
        // 在客户端，我们发送一个聊天命令到服务器端处理
        // 服务器端会处理实际的传送逻辑
        client.getNetworkHandler().sendChatCommand("magic_teleport_to_circle");
    }
}