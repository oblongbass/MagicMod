package com.magic.client;

import com.magic.Entity.MagicCircleItemEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

/**
 * 魔法阵物品实体渲染器
 * 继承自ItemEntityRenderer，使用标准渲染逻辑
 */
public class MagicCircleItemEntityRenderer extends ItemEntityRenderer {
    
    public MagicCircleItemEntityRenderer(net.minecraft.client.render.entity.EntityRendererFactory.Context context) {
        super(context);
    }
    
    @Override
    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }
    
    @Override
    public void updateRenderState(ItemEntity entity, ItemEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        
        // 确保实体类型正确
        if (!(entity instanceof MagicCircleItemEntity)) {
            return;
        }
        
        // 魔法阵实体需要特殊处理
        // 保持物品静止，不旋转
        state.age = 0;
    }
}