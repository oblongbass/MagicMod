package com.magic.client;

import net.minecraft.client.MinecraftClient;

public class MagicCircleRenderHandler {
    // WorldRenderEvents在1.21.9中已被移除
    // 魔法阵渲染现在通过RenderTickCounterMixin实现
    
    public static void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }
        
        // 更新所有法阵粒子
        MagicCircleRenderer.updateCircles(client);
    }
}