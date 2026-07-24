package com.magic.mixin;

import com.magic.client.MagicCircleRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(targets = "net.minecraft.client.render.RenderTickCounter$Dynamic")
public class RenderTickCounterMixin {
    
    @Inject(method = "beginRenderTick", at = @At("HEAD"))
    private void onBeginRenderTick(long tickCount, boolean inGame, CallbackInfoReturnable ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            // 更新魔法阵粒子
            MagicCircleRenderer.updateCircles(client);
        }
    }
}