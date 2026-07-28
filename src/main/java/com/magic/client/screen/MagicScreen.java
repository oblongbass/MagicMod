package com.magic.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class MagicScreen extends Screen {

    public MagicScreen() {
        super(Text.literal("魔法界面"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 80;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int bottomY = this.height - 30;

        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> {
            this.client.setScreen(null);
        })
        .dimensions(centerX - buttonWidth / 2, bottomY, buttonWidth, buttonHeight)
        .build();

        this.addDrawableChild(closeButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xCC202020);

        super.render(context, mouseX, mouseY, delta);

        if (this.textRenderer != null) {
            int centerX = this.width / 2;
            int startY = 50;

            Text title = Text.literal("§5§l魔法界面");
            int titleWidth = this.textRenderer.getWidth(title);
            context.drawTextWithShadow(this.textRenderer, title, centerX - titleWidth / 2, startY, 0xFFFFFFFF);

            Text magicLevelText = Text.literal("§a魔法级: §f" + com.magic.client.ManaManager.getMagicLevel() + " / 10");
            int levelWidth = this.textRenderer.getWidth(magicLevelText);
            context.drawTextWithShadow(this.textRenderer, magicLevelText, centerX - levelWidth / 2, startY + 30, 0xFFFFFFFF);

            boolean unlocked = com.magic.client.ManaManager.isMagicUnlocked();
            Text statusText;
            int statusColor;
            if (unlocked) {
                statusText = Text.literal("§a[已解锁] 魔法技能已激活");
                statusColor = 0xFF00FF00;
            } else {
                statusText = Text.literal("§c[未解锁] 魔法技能未激活");
                statusColor = 0xFFFF0000;
            }
            int statusWidth = this.textRenderer.getWidth(statusText);
            context.drawTextWithShadow(this.textRenderer, statusText, centerX - statusWidth / 2, startY + 55, statusColor);

            if (unlocked) {
                int currentMana = com.magic.client.ManaManager.getCurrentMana();
                int maxMana = com.magic.client.ManaManager.getMaxMana();

                Text manaLabel = Text.literal("§b当前魔法值:");
                int manaLabelWidth = this.textRenderer.getWidth(manaLabel);
                context.drawTextWithShadow(this.textRenderer, manaLabel, centerX - manaLabelWidth / 2, startY + 90, 0xFFFFFFFF);

                int barX = centerX - 100;
                int barY = startY + 105;
                int barWidth = 200;
                int barHeight = 15;

                context.fill(barX - 2, barY - 2, barX + barWidth + 2, barY + barHeight + 2, 0xFF555555);
                context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x88000000);

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
                    context.fill(barX, barY, barX + manaWidth, barY + barHeight, color);
                }

                Text manaText = Text.literal(currentMana + " / " + maxMana);
                int manaTextWidth = this.textRenderer.getWidth(manaText);
                context.drawTextWithShadow(this.textRenderer, manaText, centerX - manaTextWidth / 2, barY + 3, 0xFFFFFFFF);

                Text regenInfo = Text.literal("§7魔法值每秒恢复 1 点");
                int regenWidth = this.textRenderer.getWidth(regenInfo);
                context.drawTextWithShadow(this.textRenderer, regenInfo, centerX - regenWidth / 2, startY + 135, 0xFFAAAAAA);
            } else {
                Text hint = Text.literal("§e解锁魔法技能后可使用魔法值");
                int hintWidth = this.textRenderer.getWidth(hint);
                context.drawTextWithShadow(this.textRenderer, hint, centerX - hintWidth / 2, startY + 90, 0xFFFFFF00);

                Text hint2 = Text.literal("§7完成魔法任务解锁");
                int hint2Width = this.textRenderer.getWidth(hint2);
                context.drawTextWithShadow(this.textRenderer, hint2, centerX - hint2Width / 2, startY + 110, 0xFF888888);
            }

            Text controls = Text.literal("§8按 §eESC §8关闭界面");
            int controlsWidth = this.textRenderer.getWidth(controls);
            context.drawTextWithShadow(this.textRenderer, controls, centerX - controlsWidth / 2, this.height - 50, 0xFF888888);
        }
    }
}