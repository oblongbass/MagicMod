package com.magic.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class TutorialEndScreen extends Screen {
    private int fadeStep = 0;
    private static final int FADE_IN_STEPS = 20;
    private static final int HOLD_STEPS = 100;
    private static final int FADE_OUT_STEPS = 20;
    private static final int TOTAL_STEPS = FADE_IN_STEPS + HOLD_STEPS + FADE_OUT_STEPS;

    private float alpha = 0.0f;
    private boolean soundPlayed = false;
    private final List<ParticleData> particles = new ArrayList<>();
    private long worldAge = 0;
    private Random random = new Random();
    private int mouseX = 0, mouseY = 0;

    public TutorialEndScreen() {
        super(Text.literal(""));
    }

    @Override
    public void tick() {
        fadeStep++;
        worldAge++;

        if (fadeStep <= FADE_IN_STEPS) {
            alpha = (float) fadeStep / FADE_IN_STEPS;
        } else if (fadeStep <= FADE_IN_STEPS + HOLD_STEPS) {
            alpha = 1.0f;
            if (!soundPlayed && fadeStep == FADE_IN_STEPS + 10) {
                playUnlockSound();
                soundPlayed = true;
            }
            spawnMagicParticles();
        } else if (fadeStep <= TOTAL_STEPS) {
            alpha = 1.0f - (float) (fadeStep - FADE_IN_STEPS - HOLD_STEPS) / FADE_OUT_STEPS;
        }

        if (fadeStep >= TOTAL_STEPS) {
            closeScreen();
        }
    }

    private void spawnMagicParticles() {
        int sw = this.width;
        int sh = this.height;
        if (sw <= 0 || sh <= 0) return;

        if (worldAge % 3 == 0) {
            for (int i = 0; i < 3; i++) {
                // 粒子在屏幕范围内随机生成
                float sx = random.nextFloat() * sw;
                float sy = random.nextFloat() * sh;
                // 鼠标吸引力偏移：粒子生成时偏向鼠标方向
                float dx = mouseX - sx;
                float dy = mouseY - sy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float pull = 0.15f;
                float vx = (random.nextFloat() - 0.5f) * 2f + (dx / Math.max(dist, 1f)) * pull;
                float vy = (random.nextFloat() - 0.5f) * 2f + (dy / Math.max(dist, 1f)) * pull;

                ParticleData p = new ParticleData(sx, sy, vx, vy, random.nextInt(50) + 20, random.nextInt(3));
                particles.add(p);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        int screenWidth = this.width;
        int screenHeight = this.height;

        int bgAlpha = (int) (alpha * 255);
        if (bgAlpha > 255) bgAlpha = 255;
        if (bgAlpha < 0) bgAlpha = 0;
        int bgColor = (bgAlpha << 24);

        context.fill(0, 0, screenWidth, screenHeight, bgColor);

        // 渲染粒子（屏幕坐标系）
        renderParticles(context);

        if (alpha > 0.3f) {
            float textAlpha = Math.min(1.0f, (alpha - 0.3f) / 0.7f);
            int textAlphaInt = (int) (textAlpha * 255);
            if (textAlphaInt > 255) textAlphaInt = 255;

            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            Text titleText = Text.literal("✧ 魔法技能已解锁！ ✧").formatted(net.minecraft.util.Formatting.GOLD, net.minecraft.util.Formatting.BOLD);
            Text subtitleText = Text.literal("你已获得魔法能力，开启你的魔法之旅吧！").formatted(net.minecraft.util.Formatting.YELLOW);

            context.drawCenteredTextWithShadow(this.textRenderer, titleText, centerX, centerY - 80, 0xFFFFD700);
            context.drawCenteredTextWithShadow(this.textRenderer, subtitleText, centerX, centerY - 20, 0xFFFFFF00);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7按任意键关闭...").formatted(net.minecraft.util.Formatting.GRAY), centerX, centerY + 100, 0xFF888888);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderParticles(DrawContext context) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            ParticleData p = particles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            // 缓慢向鼠标位置吸引
            float dx = mouseX - p.x;
            float dy = mouseY - p.y;
            p.vx += dx * 0.001f;
            p.vy += dy * 0.001f;
            // 阻尼
            p.vx *= 0.98f;
            p.vy *= 0.98f;

            p.life--;
            if (p.life <= 0) {
                particles.remove(i);
                continue;
            }

            int color;
            switch (p.type) {
                case 0: color = 0xFF00BFFF; break;
                case 1: color = 0xFF9400D3; break;
                default: color = 0xFF00FF7F; break;
            }
            int alpha2 = Math.min(255, p.life * 5);
            int particleColor = (alpha2 << 24) | (color & 0x00FFFFFF);
            context.fill((int) p.x - 2, (int) p.y - 2, (int) p.x + 2, (int) p.y + 2, particleColor);
        }
    }

    private void closeScreen() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (fadeStep > FADE_IN_STEPS) {
            closeScreen();
        }
        return true;
    }

    private void playUnlockSound() {
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public static void open() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) {
            client.setScreen(new TutorialEndScreen());
        }
    }

    private static class ParticleData {
        float x, y;
        float vx, vy;
        int life;
        int type;

        ParticleData(float x, float y, float vx, float vy, int life, int type) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.type = type;
        }
    }
}
