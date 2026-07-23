package com.magic.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.particle.ParticleManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicCircleRenderer {
    private static final Map<UUID, CircleData> activeCircles = new HashMap<>();
    private static long lastRenderTime = 0;
    private static final long RENDER_INTERVAL = 50; // 50毫秒，每秒约20帧，比之前慢一些
    
    // 颜色常量
    private static final int COLOR_BLUE = 0x00BFFF;
    private static final int COLOR_PURPLE = 0x9370DB;
    private static final int COLOR_GOLD = 0xFFD700;
    
    // 半径常量
    private static final float RADIUS_OUTER = 4.0f;
    private static final float RADIUS_INNER = 1.5f;
    private static final float RADIUS_INNER_RING = 0.8f;
    
    // 粒子大小常量
    private static final float SIZE_LARGE = 0.8f;
    private static final float SIZE_MEDIUM = 0.6f;
    private static final float SIZE_SMALL = 0.4f;

    public static int getActiveCircleCount() {
        return activeCircles.size();
    }

    // 添加调试方法：检查是否有激活的魔法阵
    public static boolean hasActiveCircles() {
        return !activeCircles.isEmpty();
    }

    private static class CircleData {
        Vec3d center;
        long startTime;
        boolean active;

        CircleData(Vec3d center) {
            this.center = center;
            this.startTime = System.currentTimeMillis();
            this.active = true;
        }
    }

    public static void toggleCircle(UUID playerId, Vec3d position) {
        if (activeCircles.containsKey(playerId)) {
            activeCircles.remove(playerId);
        } else {
            activeCircles.put(playerId, new CircleData(position));
        }
    }

    public static void updateCircles(MinecraftClient client) {
        if (client.world == null) return;
        
        // 在1.21.7版本中，使用固定的tickDelta值
        renderCircles(client.world, 1.0f);
    }

    public static void renderCircles(ClientWorld world, float tickDelta) {
        long currentTime = System.currentTimeMillis();
        
        // 检查渲染间隔，控制粒子刷新频率
        if (currentTime - lastRenderTime < RENDER_INTERVAL) {
            return;
        }
        lastRenderTime = currentTime;

        // 渲染所有激活的魔法阵
        for (CircleData data : activeCircles.values()) {
            if (!data.active) continue;

            // 移除周期性，让法阵一直显示
            long elapsed = currentTime - data.startTime;
            float progress = (elapsed % 10000) / 10000.0f; // 10秒周期，用于动画效果

            // 基础半径
            float baseRadius = 4.0f;
            int baseColor = 0x00BFFF; // 蓝色

            // 渲染圆形法阵（持续显示）
            renderCircle(world, data.center, progress);

            // 渲染内部粒子效果（持续显示）
            renderInnerParticles(world, data.center, progress);

            // 渲染中心粒子效果（持续显示）
            renderCenterParticles(world, data.center, progress);

            // 渲染连接线（恢复之前的特效）
            renderIntersectingLines(world, data.center, baseRadius, baseColor, progress);

            // 渲染魔法图案（恢复之前的特效）
            renderMagicPatterns(world, data.center, baseRadius, baseColor);
        }
        
        // 额外：尝试从SimplePlayerDataManager获取魔法阵位置并渲染
        // 注意：这需要客户端也能访问数据管理器，可能需要网络通信
        // 暂时注释掉，因为SimplePlayerDataManager是服务器端的
        /*
        try {
            // 这里可以添加从服务器获取魔法阵位置的逻辑
        } catch (Exception e) {
            // 忽略错误
        }
        */
    }
    
    // 添加一个方法，在放置魔法阵时从服务器接收位置并激活特效
    public static void activateCircleForPlayer(UUID playerId, net.minecraft.util.math.Vec3d position) {
        // 如果已经存在，更新位置；否则创建新的
        if (activeCircles.containsKey(playerId)) {
            CircleData data = activeCircles.get(playerId);
            data.center = position;
            data.startTime = System.currentTimeMillis(); // 重置开始时间
            data.active = true;
            com.magic.MagicMod.LOGGER.info("[Magic] 激活魔法阵特效（更新）: 玩家 {}, 位置 {}", playerId, position);
        } else {
            activeCircles.put(playerId, new CircleData(position));
            com.magic.MagicMod.LOGGER.info("[Magic] 激活魔法阵特效（新建）: 玩家 {}, 位置 {}", playerId, position);
        }
    }
    
    // 添加一个方法，在收回魔法阵时移除特效
    public static void deactivateCircleForPlayer(UUID playerId) {
        activeCircles.remove(playerId);
    }

    private static void renderCircle(ClientWorld world, Vec3d center, float progress) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        
        // 简单的外圆 - 24个粒子
        int outerPoints = 24;
        float outerRadius = RADIUS_OUTER;
        int outerColor = COLOR_BLUE;
        
        for (int i = 0; i < outerPoints; i++) {
            float angle = (float) (2 * Math.PI * i / outerPoints + progress * 2 * Math.PI);
            double x = center.x + outerRadius * MathHelper.cos(angle);
            double y = center.y;
            double z = center.z + outerRadius * MathHelper.sin(angle);

            DustParticleEffect dustEffect = new DustParticleEffect(outerColor, SIZE_LARGE);
            particleManager.addParticle(dustEffect, x, y, z, 0.0, 0.0, 0.0);
        }
        
        // 简单的内圆 - 12个粒子，反向旋转
        int innerPoints = 12;
        float innerRadius = RADIUS_INNER;
        int innerColor = COLOR_PURPLE;
        
        for (int i = 0; i < innerPoints; i++) {
            float angle = (float) (2 * Math.PI * i / innerPoints - progress * 2 * Math.PI);
            double x = center.x + innerRadius * MathHelper.cos(angle);
            double y = center.y;
            double z = center.z + innerRadius * MathHelper.sin(angle);

            DustParticleEffect dustEffect = new DustParticleEffect(innerColor, SIZE_MEDIUM);
            particleManager.addParticle(dustEffect, x, y, z, 0.0, 0.0, 0.0);
        }
    }
    
    private static void renderInnerRing(ClientWorld world, Vec3d center, float radius, float progress, int color) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        int points = 24;
        
        // 反向旋转
        float rotation = -progress * 3 * (float)Math.PI;
        
        for (int i = 0; i < points; i++) {
            float angle = (float) (2 * Math.PI * i / points) + rotation;
            double x = center.x + radius * MathHelper.cos(angle);
            double y = center.y + 0.1; // 更高一点
            double z = center.z + radius * MathHelper.sin(angle);
            
            DustParticleEffect dustEffect = new DustParticleEffect(
                    color,
                    0.6f
            );
            
            particleManager.addParticle(
                    dustEffect,
                    x, y, z,
                    0.0, 0.0, 0.0
            );
        }
    }

    private static void renderInnerParticles(ClientWorld world, Vec3d center, float progress) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        
        // 简单的内部漂浮粒子 - 8个
        int particles = 8;
        int color = 0x00BFFF; // 蓝色
        
        for (int i = 0; i < particles; i++) {
            float angle = (float) (2 * Math.PI * i / particles);
            float floatHeight = 0.2f * MathHelper.sin(progress * 4 * (float)Math.PI + i * 0.5f);
            float particleRadius = 0.8f + 0.3f * MathHelper.sin(progress * 2 * (float)Math.PI + i * 0.3f);
            
            double x = center.x + particleRadius * MathHelper.cos(angle);
            double y = center.y + floatHeight;
            double z = center.z + particleRadius * MathHelper.sin(angle);
            
            float size = 0.4f + 0.1f * MathHelper.sin(progress * 3 * (float)Math.PI + i);
            DustParticleEffect dustEffect = new DustParticleEffect(color, size);
            particleManager.addParticle(dustEffect, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    private static void renderIntersectingLines(ClientWorld world, Vec3d center, float radius, int color, float progress) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        
        // 创建规律的交杂线条 - 六角星形
        int lines = 6; // 6条主线
        int particlesPerLine = 12; // 增加每条线的粒子数以提高可见度
        
        for (int line = 0; line < lines; line++) {
            float angle = (float) (2 * Math.PI * line / lines);
            float endAngle = (float) (2 * Math.PI * ((line + 3) % lines) / lines); // 对角线
            
            for (int i = 0; i < particlesPerLine; i++) {
                float t = i / (float)(particlesPerLine - 1);
                
                // 线性插值计算线条上的点
                float startX = radius * MathHelper.cos(angle);
                float startZ = radius * MathHelper.sin(angle);
                float endX = radius * MathHelper.cos(endAngle);
                float endZ = radius * MathHelper.sin(endAngle);
                
                float x = (float) center.x + startX + t * (endX - startX);
                float z = (float) center.z + startZ + t * (endZ - startZ);
                
                // 添加一些偏移，使线条看起来更自然
                float offset = MathHelper.sin(t * MathHelper.PI) * 0.1f;
                x += offset * MathHelper.cos(angle + MathHelper.PI/2);
                z += offset * MathHelper.sin(angle + MathHelper.PI/2);
                
                DustParticleEffect dustEffect = new DustParticleEffect(
                            color,
                            0.8f  // 增大粒子尺寸
                    );
                
                particleManager.addParticle(
                        dustEffect,
                        x, center.y, z,
                        0.0, 0.0, 0.0
                );
            }
        }
        
        // 添加内圆的连接线
        int innerLines = 12;
        for (int i = 0; i < innerLines; i++) {
            float angle = (float) (2 * Math.PI * i / innerLines);
            float nextAngle = (float) (2 * Math.PI * ((i + 2) % innerLines) / innerLines);
            
            for (int j = 0; j < 8; j++) {
                float t = j / 7f;
                float innerRadius = radius * 0.6f;
                
                float x = (float) center.x + innerRadius * (MathHelper.cos(angle) * (1-t) + MathHelper.cos(nextAngle) * t);
                float z = (float) center.z + innerRadius * (MathHelper.sin(angle) * (1-t) + MathHelper.sin(nextAngle) * t);
                
                DustParticleEffect dustEffect = new DustParticleEffect(
                            color,
                            0.6f  // 增大粒子尺寸
                    );
                
                particleManager.addParticle(
                        dustEffect,
                        x, center.y, z,
                        0.0, 0.0, 0.0
                );
            }
        }
    }
    
    private static void renderMagicPatterns(ClientWorld world, Vec3d center, float radius, int color) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        
        // 创建三角形图案
        int triangles = 3;
        for (int tri = 0; tri < triangles; tri++) {
            float rotation = (float) (2 * Math.PI * tri / triangles);
            float triRadius = radius * 0.7f;
            
            for (int i = 0; i < 3; i++) {
                float angle1 = rotation + (float) (2 * Math.PI * i / 3);
                float angle2 = rotation + (float) (2 * Math.PI * ((i + 1) % 3) / 3);
                
                for (int j = 0; j < 10; j++) {
                    float t = j / 9f;
                    float x = (float) center.x + triRadius * (MathHelper.cos(angle1) * (1-t) + MathHelper.cos(angle2) * t);
                    float z = (float) center.z + triRadius * (MathHelper.sin(angle1) * (1-t) + MathHelper.sin(angle2) * t);
                    
                    DustParticleEffect dustEffect = new DustParticleEffect(
                            color,
                            0.7f  // 增大粒子尺寸
                    );
                    
                    particleManager.addParticle(
                            dustEffect,
                            x, center.y, z,
                            0.0, 0.0, 0.0
                    );
                }
            }
        }
    }

    private static void renderCenterParticles(ClientWorld world, Vec3d center, float progress) {
        ParticleManager particleManager = MinecraftClient.getInstance().particleManager;
        
        // 简单的中心旋转粒子 - 4个
        for (int i = 0; i < 4; i++) {
            float angle = progress * 4 * (float)Math.PI + (i * (float)Math.PI / 2);
            float radius = 0.4f + 0.1f * MathHelper.sin(progress * 3 * (float)Math.PI + i);
            
            double x = center.x + radius * MathHelper.cos(angle);
            double y = center.y + 0.1f + 0.05f * MathHelper.sin(progress * 2 * (float)Math.PI + i);
            double z = center.z + radius * MathHelper.sin(angle);
            
            // 简单的颜色循环
            int color = 0x00BFFF + i * 0x333333;
            DustParticleEffect dustEffect = new DustParticleEffect(color, 0.5f);
            particleManager.addParticle(dustEffect, x, y, z, 0.0, 0.02f, 0.0);
        }
        
        // 简单的中心光点
        if (progress < 0.5f) {
            DustParticleEffect dustEffect = new DustParticleEffect(0xFFD700, 0.6f);
            particleManager.addParticle(dustEffect, center.x, center.y + 0.05f, center.z, 0.0, 0.01f, 0.0);
        }
    }
}