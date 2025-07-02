package com.magic.scale;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 轻量级缩放管理器
 * 类似于Pehkui但更简单，专门为模组服务
 */
public class ScaleManager {
    private static final Map<UUID, ScaleData> entityScales = new HashMap<>();
    
    /**
     * 设置实体的缩放值
     * @param entity 目标实体
     * @param scale 缩放值 (1.0f = 原始大小)
     */
    public static void setScale(LivingEntity entity, float scale) {
        if (entity == null) return;
        
        UUID entityId = entity.getUuid();
        ScaleData data = entityScales.computeIfAbsent(entityId, k -> new ScaleData());
        data.setScale(scale);
        
        // 更新实体的碰撞箱
        updateEntityDimensions(entity);
    }
    
    /**
     * 获取实体的缩放值
     * @param entity 目标实体
     * @return 缩放值，如果未设置则返回1.0f
     */
    public static float getScale(LivingEntity entity) {
        if (entity == null) return 1.0f;
        
        ScaleData data = entityScales.get(entity.getUuid());
        return data != null ? data.getScale() : 1.0f;
    }
    
    /**
     * 重置实体的缩放值
     * @param entity 目标实体
     */
    public static void resetScale(LivingEntity entity) {
        if (entity == null) return;
        
        entityScales.remove(entity.getUuid());
        updateEntityDimensions(entity);
    }
    
    /**
     * 更新实体的碰撞箱
     * @param entity 目标实体
     */
    private static void updateEntityDimensions(LivingEntity entity) {
        float scale = getScale(entity);
        if (scale != 1.0f) {
            // 强制更新碰撞箱
            entity.calculateDimensions();
        }
    }
    
    /**
     * 清理已移除实体的数据
     * @param entity 被移除的实体
     */
    public static void onEntityRemoved(LivingEntity entity) {
        if (entity != null) {
            entityScales.remove(entity.getUuid());
        }
    }
    
    /**
     * 获取所有缩放数据（用于调试）
     */
    public static Map<UUID, ScaleData> getAllScales() {
        return new HashMap<>(entityScales);
    }
} 