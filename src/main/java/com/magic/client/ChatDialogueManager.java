package com.magic.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import com.magic.client.screen.TutorialEndScreen;

@Environment(EnvType.CLIENT)
public class ChatDialogueManager {
    private static ChatDialogueManager instance;
    private static boolean dialogueInProgress = false;

    private static final int STEP_GREETING = 0;
    private static final int STEP_ASK = 1;
    private static final int STEP_OPTIONS = 2;
    private static final int STEP_TUTORIAL = 3;
    private static final int STEP_END = 4;

    private int currentStep = STEP_GREETING;
    private int selectedOption = 1;
    private int tutorialIndex = 0;
    private boolean isActive = false;
    private boolean playerChoseKnows = false;

    private final String[] options = {"知道", "不知道"};

    private final String[] tutorials = {
        "§b✦ 欢迎来到魔法世界！以下是完整指南：",
        "",
        "§6【基础入门】",
        "§b1. 找到村庄中的图书管理员，Shift+右键与他对话解锁魔法",
        "§b2. 按 I 键打开魔法界面，查看你的魔法状态",
        "§b3. 魔法值每秒自动恢复1点，初始上限100点",
        "",
        "§6【物品与合成】",
        "§b4. 伤害魔法杖+漂浮魔法杖+烈焰粉+羽毛+泥土+潮涌核心",
        "§b    全部丢在同一格内，消耗100魔法值→生成元素法杖",
        "§b5. 书+红石丢地上→未激活的蓄能附魔书（右键激活）",
        "§b6. 书+力量药水丢地上→未激活的伤能附魔书（右键激活）",
        "",
        "§6【元素系统】",
        "§b7. 手持元素法杖Shift+右键 或 Shift+数字键 切换元素",
        "§b    1=土 2=水 3=火 4=风（切换消耗5魔法值）",
        "§b8. 火元素攻击让生物着火",
        "§b9. 火+水（蒸发反应）：额外4点伤害，显示橙色数字",
        "",
        "§6【魔法阵】",
        "§b10. 传送法阵卷轴右键放置魔法阵（消耗50魔法值）",
        "§b11. 按 F7 传送回魔法阵（消耗10魔法值）",
        "§b12. 右键磨制花岗岩可收回魔法阵",
        "",
        "§6【其他物品】",
        "§b13. 水晶球：右键扫描附近生物，Shift+右键占卜运势",
        "§b14. 浮空魔法杖：右键生物使其悬浮",
        "§b15. 伤害魔法杖：蓄力后释放魔法伤害光束",
        "§b16. 魔法矿石在世界中生成，挖掘获得魔法水晶",
        "§b17. 魔法之星（合成材料）：击杀魔法僵尸获得"
    };

    private static final KeyBinding.Category DIALOGUE_CATEGORY = KeyBinding.Category.create(
        net.minecraft.util.Identifier.of("magic-mod", "dialogue")
    );

    private KeyBinding upKey;
    private KeyBinding downKey;
    private KeyBinding confirmKey;

    private ChatDialogueManager() {
        registerKeyBindings();
        registerEvents();
    }

    public static ChatDialogueManager getInstance() {
        if (instance == null) {
            instance = new ChatDialogueManager();
        }
        return instance;
    }

    private void registerKeyBindings() {
        upKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magicmod.dialogue.up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            DIALOGUE_CATEGORY
        ));

        downKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magicmod.dialogue.down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            DIALOGUE_CATEGORY
        ));

        confirmKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.magicmod.dialogue.confirm",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            DIALOGUE_CATEGORY
        ));
    }

    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isActive) return;

            if (currentStep == STEP_OPTIONS) {
                if (upKey.isPressed()) {
                    selectedOption = (selectedOption - 1 + options.length) % options.length;
                    showOptions();
                    // 重置按键状态
                    upKey.setPressed(false);
                }

                if (downKey.isPressed()) {
                    selectedOption = (selectedOption + 1) % options.length;
                    showOptions();
                    // 重置按键状态
                    downKey.setPressed(false);
                }
            }

            if (confirmKey.isPressed()) {
                handleConfirmation();
                // 重置按键状态
                confirmKey.setPressed(false);
            }
        });
    }

    public void startDialogue() {
        if (dialogueInProgress) {
            return;
        }

        dialogueInProgress = true;
        isActive = true;
        currentStep = STEP_GREETING;
        selectedOption = 1;
        tutorialIndex = 0;
        playerChoseKnows = false;

        // 重置所有按键状态
        upKey.setPressed(false);
        downKey.setPressed(false);
        confirmKey.setPressed(false);

        showGreeting();
    }

    private void endDialogue(boolean shouldUnlockMagic) {
        isActive = false;
        dialogueInProgress = false;

        // 重置所有按键状态
        upKey.setPressed(false);
        downKey.setPressed(false);
        confirmKey.setPressed(false);

        ClientPlayNetworking.send(new com.magic.networking.DialogueEndPayload(shouldUnlockMagic));

        if (shouldUnlockMagic) {
            TutorialEndScreen.open();
        }
    }

    private void showGreeting() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.literal("§a图书管理员: 一个充满魔法的世界，现在由你，解锁魔法，向世界根源前进吧！").formatted(net.minecraft.util.Formatting.GOLD), false);
        client.player.sendMessage(Text.literal("§e按空格键继续...").formatted(net.minecraft.util.Formatting.YELLOW), false);
    }

    private void showAsk() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.literal("§a图书管理员: 你知道这个模组的玩法吗？").formatted(net.minecraft.util.Formatting.GOLD), false);
        client.player.sendMessage(Text.literal("§e按空格键继续...").formatted(net.minecraft.util.Formatting.YELLOW), false);
    }

    private void showOptions() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.literal("§e请选择：").formatted(net.minecraft.util.Formatting.YELLOW), false);

        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                client.player.sendMessage(Text.literal("§l§6> " + options[i]).formatted(net.minecraft.util.Formatting.GOLD), false);
            } else {
                client.player.sendMessage(Text.literal("§7  " + options[i]).formatted(net.minecraft.util.Formatting.GRAY), false);
            }
        }

        client.player.sendMessage(Text.literal("§e按 W 键向上选择，S 键向下选择，空格键确认").formatted(net.minecraft.util.Formatting.YELLOW), false);
    }

    private void showTutorial() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (tutorialIndex < tutorials.length) {
            client.player.sendMessage(Text.literal(tutorials[tutorialIndex]).formatted(net.minecraft.util.Formatting.BLUE), false);
            client.player.sendMessage(Text.literal("§e按空格键继续...").formatted(net.minecraft.util.Formatting.YELLOW), false);
        } else {
            client.player.sendMessage(Text.literal("§a图书管理员: 记住，魔法需要不断练习才能掌握。祝你在魔法之路上一切顺利！").formatted(net.minecraft.util.Formatting.GOLD), false);
            currentStep = STEP_END;
            endDialogue(true);
        }
    }

    private void handleConfirmation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        switch (currentStep) {
            case STEP_GREETING:
                currentStep = STEP_ASK;
                showAsk();
                break;

            case STEP_ASK:
                currentStep = STEP_OPTIONS;
                // 进入选项阶段时重置按键状态
                upKey.setPressed(false);
                downKey.setPressed(false);
                showOptions();
                break;

            case STEP_OPTIONS:
                if (selectedOption == 0) {
                    playerChoseKnows = true;
                    client.player.sendMessage(Text.literal("§a图书管理员: 很高兴听到你已经了解！那你一定知道魔法的基本原理了。").formatted(net.minecraft.util.Formatting.GOLD), false);
                    client.player.sendMessage(Text.literal("§a图书管理员: 继续努力提升你的魔法水平吧！").formatted(net.minecraft.util.Formatting.GOLD), false);
                    currentStep = STEP_END;
                    endDialogue(true);
                } else {
                    client.player.sendMessage(Text.literal("§a图书管理员: 没关系，我来慢慢教你。首先，让我为你解锁魔法技能。").formatted(net.minecraft.util.Formatting.GOLD), false);
                    client.player.sendMessage(Text.literal(""), false);
                    client.player.sendMessage(Text.literal("§a图书管理员: 太好了！你现在已经解锁了魔法技能。让我来告诉你如何使用：").formatted(net.minecraft.util.Formatting.GOLD), false);
                    currentStep = STEP_TUTORIAL;
                    tutorialIndex = 0;
                    showTutorial();
                }
                break;

            case STEP_TUTORIAL:
                tutorialIndex++;
                showTutorial();
                break;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public static boolean isDialogueInProgress() {
        return dialogueInProgress;
    }
}