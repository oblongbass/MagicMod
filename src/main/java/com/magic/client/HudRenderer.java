package com.magic.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HudRenderer {
    
    // 使用静态方法供 Mixin 调用
    public static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 获取屏幕尺寸
        int screenWidth = client.getWindow().getScaledWidth();
        
        // 获取魔法值
        int currentMana = ManaManager.getCurrentMana();
        int maxMana = ManaManager.getMaxMana();
        
        // 渲染魔法值条
        renderManaBar(drawContext, client, screenWidth, currentMana, maxMana);
    }
    
    public static void renderManaBar(DrawContext drawContext, MinecraftClient client, 
            int screenWidth, int currentMana, int maxMana) {
        
        // 魔法值条的位置（右上角）
        int barX = screenWidth - 120;
        int barY = 30;
        int barWidth = 100;
        int barHeight = 10;
        
        // 背景条（深灰色）
        drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);
        
        // 魔法值条（蓝色）
        int manaWidth = (int) ((float) currentMana / maxMana * barWidth);
        if (manaWidth > 0) {
            // 根据魔法值多少改变颜色
            int color;
            if (currentMana > maxMana * 0.6) {
                color = 0xFF00BFFF; // 蓝色 - 充足
            } else if (currentMana > maxMana * 0.3) {
                color = 0xFF00CED1; // 深青色 - 中等
            } else {
                color = 0xFFFF4500; // 红色 - 不足
            }
            drawContext.fill(barX, barY, barX + manaWidth, barY + barHeight, color);
        }
        
        // 边框
        drawContext.fill(barX - 1, barY - 1, barX + barWidth + 1, barY, 0xFF555555);
        drawContext.fill(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, 0xFF555555);
        drawContext.fill(barX - 1, barY, barX, barY + barHeight, 0xFF555555);
        drawContext.fill(barX + barWidth, barY, barX + barWidth + 1, barY + barHeight, 0xFF555555);
        
        // 显示魔法值数值
        String manaText = currentMana + "/" + maxMana;
        int textWidth = client.textRenderer.getWidth(manaText);
        int textX = barX + (barWidth - textWidth) / 2;
        int textY = barY + 1;
        
        drawContext.drawText(client.textRenderer, manaText, textX, textY, 0xFFFFFFFF, false);
        
        // 显示"魔法值"标签
        drawContext.drawText(client.textRenderer, Text.literal("§b魔法值"), barX, barY - 12, 0xFFFFFFFF, false);
    }
}
