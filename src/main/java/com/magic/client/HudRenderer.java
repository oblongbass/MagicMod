package com.magic.client;

import com.magic.item.MagicWandItem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class HudRenderer {
    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            ItemStack stack = client.player.getMainHandStack();
            if (stack.getItem() instanceof MagicWandItem) {
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                // 在左下角显示简单文本
                int x = 10;
                int y = screenHeight - 30;
                drawContext.drawText(
                        client.textRenderer,
                        Text.literal("魔法棒已装备"),
                        x, y,
                        0xFFFFFF,
                        true
                );
            }
        });
    }
}