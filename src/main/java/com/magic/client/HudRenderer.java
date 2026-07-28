package com.magic.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HudRenderer {

    // 独立于 ManaManager 的元素显示状态，快捷键直接更新，确保 HUD 即时刷新
    private static int displayElement = 3;

    public static void setDisplayElement(int element) {
        displayElement = element;
    }

    public static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int screenWidth = client.getWindow().getScaledWidth();

        if (ManaManager.isMagicUnlocked()) {
            int currentMana = ManaManager.getCurrentMana();
            int maxMana = ManaManager.getMaxMana();
            int magicLevel = ManaManager.getMagicLevel();

            // 魔法等级
            if (magicLevel > 0) {
                String levelText = "§6✦ 魔法等级: " + magicLevel + " ✦";
                int levelWidth = client.textRenderer.getWidth(Text.literal(levelText));
                drawContext.drawText(client.textRenderer, Text.literal(levelText),
                        screenWidth - 120 + (100 - levelWidth) / 2, 16, 0xFFFFD700, false);
            }

            // 当前元素（使用 displayElement，快捷键触发即时刷新）
            int element = displayElement;
            String elemName = com.magic.item.ElementWandItem.ELEMENT_NAMES[element];
            drawContext.drawText(client.textRenderer, Text.literal("§l" + elemName),
                    screenWidth - 120, 40, 0xFFFFFFFF, false);

            // Shift+数字提示：手持元素法杖且潜行时显示
            if (client.player.isSneaking() && client.player.getMainHandStack().getItem() instanceof com.magic.item.ElementWandItem) {
                String[] numColors = {"§6", "§b", "§c", "§7"};
                String[] numNames = {"土", "水", "火", "风"};
                int[] numElems = {3, 1, 0, 2};
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    String color = numColors[i];
                    String marker = (numElems[i] == element) ? "§f▸" : " ";
                    sb.append(marker).append("§e[").append(i + 1).append("]").append(color).append(numNames[i]).append(" ");
                }
                drawContext.drawText(client.textRenderer, Text.literal(sb.toString()),
                        screenWidth - 120, 60, 0xFFFFFFFF, false);
            }

            renderManaBar(drawContext, client, screenWidth, currentMana, maxMana);
        }
    }

    public static void renderManaBar(DrawContext drawContext, MinecraftClient client,
            int screenWidth, int currentMana, int maxMana) {

        int barX = screenWidth - 120;
        int barY = 30;
        int barWidth = 100;
        int barHeight = 10;

        drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

        int manaWidth = (int) ((float) currentMana / maxMana * barWidth);
        if (manaWidth > 0) {
            int color;
            if (currentMana > maxMana * 0.6) {
                color = 0xFF00BFFF;
            } else if (currentMana > maxMana * 0.3) {
                color = 0xFF00CED1;
            } else {
                color = 0xFFFF4500;
            }
            drawContext.fill(barX, barY, barX + manaWidth, barY + barHeight, color);
        }

        drawContext.fill(barX - 1, barY - 1, barX + barWidth + 1, barY, 0xFF555555);
        drawContext.fill(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, 0xFF555555);
        drawContext.fill(barX - 1, barY, barX, barY + barHeight, 0xFF555555);
        drawContext.fill(barX + barWidth, barY, barX + barWidth + 1, barY + barHeight, 0xFF555555);

        String manaText = currentMana + "/" + maxMana;
        int textWidth = client.textRenderer.getWidth(manaText);
        int textX = barX + (barWidth - textWidth) / 2;
        int textY = barY + 1;

        drawContext.drawText(client.textRenderer, manaText, textX, textY, 0xFFFFFFFF, false);

        drawContext.drawText(client.textRenderer, Text.literal("§b魔法值"), barX, barY - 12, 0xFFFFFFFF, false);
    }
}
