package com.magic.effect;

import com.magic.scale.ScaleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class GrowEffect extends StatusEffect {
    public static final GrowEffect GROW_EFFECT = new GrowEffect();
    
    public GrowEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00FF00);
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity) {
            // 1级药水10倍，每增加一级倍数+1
            float scale = 10.0f + amplifier;
            ScaleManager.setScale(entity, scale);
            
            // 调试信息
            System.out.println("[GrowEffect] 设置玩家缩放: " + scale + "x");
        }
    }

    // 注意：原版StatusEffect没有onRemoved，移除时需用事件或Mixin处理
} 