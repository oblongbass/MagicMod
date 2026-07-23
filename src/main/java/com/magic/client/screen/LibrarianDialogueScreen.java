package com.magic.client.screen;

import com.magic.event.DialogueManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LibrarianDialogueScreen extends Screen {
    private final DialogueManager dialogueManager;
    private ButtonWidget selectButton;
    private ButtonWidget nextButton;
    
    private int selectedOption = 0;
    private String[] currentOptions;

    public LibrarianDialogueScreen(DialogueManager dialogueManager) {
        super(Text.literal("图书管理员对话"));
        this.dialogueManager = dialogueManager;
    }

    @Override
    protected void init() {
        super.init();
        clearChildren();
        
        int centerX = this.width / 2;
        int startY = 50;
        
        // Navigation buttons
        selectButton = ButtonWidget.builder(Text.literal("选择"), button -> {
            dialogueManager.selectOption(selectedOption);
            init();
        })
        .dimensions(centerX - 100, startY + 160, 200, 20)
        .build();
        
        nextButton = ButtonWidget.builder(Text.literal("继续"), button -> {
            dialogueManager.next();
            init();
        })
        .dimensions(centerX - 100, startY + 160, 200, 20)
        .build();
        
        // Close button
        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> {
            this.client.setScreen(null);
        })
        .dimensions(centerX - 100, startY + 190, 200, 20)
        .build();
        addDrawableChild(closeButton);
        
        updateButtons();
    }

    private void updateButtons() {
        clearChildren();
        
        int centerX = this.width / 2;
        int startY = 50;
        
        // Close button
        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> {
            this.client.setScreen(null);
        })
        .dimensions(centerX - 100, startY + 190, 200, 20)
        .build();
        addDrawableChild(closeButton);
        
        // Handle options
        currentOptions = dialogueManager.getCurrentOptions();
        if (currentOptions != null && currentOptions.length > 0) {
            selectedOption = Math.max(0, Math.min(selectedOption, currentOptions.length - 1));
            addDrawableChild(selectButton);
        } else {
            addDrawableChild(nextButton);
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int keyCode = keyInput.key();
        if (currentOptions != null && currentOptions.length > 0) {
            switch (keyCode) {
                case 87: // W key
                    selectedOption = (selectedOption - 1 + currentOptions.length) % currentOptions.length;
                    return true;
                case 83: // S key
                    selectedOption = (selectedOption + 1) % currentOptions.length;
                    return true;
                case 32: // Space key
                    dialogueManager.selectOption(selectedOption);
                    init();
                    return true;
            }
        } else {
            if (keyCode == 32) { // Space key
                dialogueManager.next();
                init();
                return true;
            }
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int startY = 50;
        
        // Draw dialogue box
        context.fill(centerX - 200, startY, centerX + 200, startY + 150, 0xCC202020);
        context.fill(centerX - 198, startY + 2, centerX + 198, startY + 148, 0xCC101010);
        
        // Draw NPC name
        context.drawTextWithShadow(textRenderer, Text.literal("§6图书管理员"), centerX - 190, startY + 10, 0xFFFFFF);
        
        // Draw dialogue text
        String dialogue = dialogueManager.getCurrentDialogue();
        drawMultilineText(context, dialogue, centerX - 180, startY + 40, 360, 0xFFFFFF);
        
        // Draw options
        currentOptions = dialogueManager.getCurrentOptions();
        if (currentOptions != null && currentOptions.length > 0) {
            selectedOption = Math.max(0, Math.min(selectedOption, currentOptions.length - 1));
            
            for (int i = 0; i < currentOptions.length; i++) {
                int optionY = startY + 80 + (i * 25);
                String optionText = currentOptions[i];
                if (i == selectedOption) {
                    optionText = "§e> " + optionText;
                }
                context.drawTextWithShadow(textRenderer, Text.literal(optionText), centerX - 170, optionY, 0xFFFFFF);
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawMultilineText(DrawContext context, String text, int x, int y, int width, int color) {
        int lineHeight = textRenderer.fontHeight + 2;
        int currentY = y;
        
        // Split text into lines
        String[] lines = textRenderer.wrapLines(Text.literal(text), width).toArray(new String[0]);
        
        for (String line : lines) {
            context.drawTextWithShadow(textRenderer, line, x, currentY, color);
            currentY += lineHeight;
        }
    }
}