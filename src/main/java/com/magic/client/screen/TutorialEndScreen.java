package com.magic.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
            assert this.client != null;
            this.client.setScreen(null);
        }
    }

    private void spawnMagicParticles() {
        if (this.client == null || this.client.world == null || this.client.player == null) return;

        double playerX = this.client.player.getX();
        double playerY = this.client.player.getY();
        double playerZ = this.client.player.getZ();

        if (worldAge % 3 == 0) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 3;
                double offsetY = random.nextDouble() * 2;
                double offsetZ = (random.nextDouble() - 0.5) * 3;

                ParticleData particle = new ParticleData(
                    playerX + offsetX,
                    playerY + offsetY,
                    playerZ + offsetZ,
                    (random.nextDouble() - 0.5) * 0.02,
                    random.nextDouble() * 0.03,
                    (random.nextDouble() - 0.5) * 0.02,
                    random.nextInt(3)
                );
                particles.add(particle);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int screenWidth = this.width;
        int screenHeight = this.height;

        int bgAlpha = (int) (alpha * 255);
        if (bgAlpha > 255) bgAlpha = 255;
        if (bgAlpha < 0) bgAlpha = 0;
        int bgColor = (bgAlpha << 24);

        context.fill(0, 0, screenWidth, screenHeight, bgColor);

        renderParticles(context);

        if (alpha > 0.3f) {
            float textAlpha = Math.min(1.0f, (alpha - 0.3f) / 0.7f);
            int textAlphaInt = (int) (textAlpha * 255);
            if (textAlphaInt > 255) textAlphaInt = 255;

            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            int goldColor = 0xFFFFD700;
            int yellowColor = 0xFFFFFF00;
            int grayColor = 0xFF888888;

            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7按任意键继续...").formatted(net.minecraft.util.Formatting.GRAY), centerX, centerY + 100, grayColor);

            Text titleText = Text.literal("✧ 魔法技能已解锁！ ✧").formatted(net.minecraft.util.Formatting.GOLD, net.minecraft.util.Formatting.BOLD);
            Text subtitleText = Text.literal("你已获得魔法能力，开启你的魔法之旅吧！").formatted(net.minecraft.util.Formatting.YELLOW);

            context.drawCenteredTextWithShadow(this.textRenderer, titleText, centerX, centerY - 80, goldColor);
            context.drawCenteredTextWithShadow(this.textRenderer, subtitleText, centerX, centerY - 20, yellowColor);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderParticles(DrawContext context) {
        if (this.client == null || this.client.player == null) return;
        var player = this.client.player;
        double cameraX = player.getX();
        double cameraY = player.getY() + player.getStandingEyeHeight();
        double cameraZ = player.getZ();

        for (int i = particles.size() - 1; i >= 0; i--) {
            ParticleData p = particles.get(i);

            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;
            p.vy += 0.001;

            p.life--;
            if (p.life <= 0) {
                particles.remove(i);
                continue;
            }

            int screenX = (int) ((p.x - cameraX) * 10 + this.width / 2);
            int screenY = (int) ((p.y - cameraY) * 10 + this.height / 2);

            if (screenX >= 0 && screenX <= this.width && screenY >= 0 && screenY <= this.height) {
                int color;
                switch (p.type) {
                    case 0:
                        color = 0xFF00BFFF;
                        break;
                    case 1:
                        color = 0xFF9400D3;
                        break;
                    default:
                        color = 0xFF00FF7F;
                        break;
                }
                int alpha2 = Math.min(255, p.life * 10);
                int particleColor = (alpha2 << 24) | (color & 0x00FFFFFF);
                context.fill(screenX - 2, screenY - 2, screenX + 2, screenY + 2, particleColor);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
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
        double x, y, z;
        double vx, vy, vz;
        int life;
        int type;

        ParticleData(double x, double y, double z, double vx, double vy, double vz, int type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.life = 50;
            this.type = type;
        }
    }
}