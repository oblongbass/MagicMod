package com.magic.mixin;

import com.magic.scale.ScaleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityScaleMixin {
    
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void modifyDimensions(CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float scale = ScaleManager.getScale(entity);
        
        if (scale != 1.0f) {
            EntityDimensions original = cir.getReturnValue();
            cir.setReturnValue(original.scaled(scale));
        }
    }
} 