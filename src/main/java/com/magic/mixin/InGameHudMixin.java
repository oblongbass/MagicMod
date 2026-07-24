package com.magic.mixin;

import com.magic.client.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    
    @Shadow
    private MinecraftClient client;
    
    @Inject(
        method = "render",
        at = @At(
            value = "HEAD"
        )
    )
    private void onRender(CallbackInfo ci) {
        // 这个方法会在InGameHud.render()开始时调用
        // 但我们需要在渲染完成后绘制，所以用另一种方式
    }
    
    // 使用Redirect来在render方法末尾添加我们的渲染
    @Inject(
        method = "render",
        at = @At(
            value = "TAIL"
        )
    )
    private void onRenderTail(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.player != null && !this.client.options.hudHidden) {
            HudRenderer.renderHud(drawContext, tickCounter);
        }
    }
}
