package com.magic.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * 临时占位符 GuideBookScreen - 1.21.9 API 兼容性问题待修复
 */
public class GuideBookScreen extends Screen {
    
    public GuideBookScreen() {
        super(Text.literal("魔法模组指南"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 80;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int bottomY = this.height - 30;
        
        // 关闭按钮
        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> {
            this.client.setScreen(null);
        })
        .dimensions(centerX - buttonWidth / 2, bottomY, buttonWidth, buttonHeight)
        .build();
        
        this.addDrawableChild(closeButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 绘制深灰色背景
        context.fill(0, 0, this.width, this.height, 0xFF202020);
        
        // 调用super渲染子组件
        super.render(context, mouseX, mouseY, delta);
        
        // 显示提示文本
        if (this.textRenderer != null) {
            String text = "魔法模组指南 - 暂未适配 1.21.9";
            int textWidth = this.textRenderer.getWidth(text);
            context.drawTextWithShadow(this.textRenderer, text, (this.width - textWidth) / 2, this.height / 2, 0xFFFFFFFF);
        }
    }
}
