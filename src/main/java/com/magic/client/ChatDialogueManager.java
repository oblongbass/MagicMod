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
        "§b1. 按 I 键打开魔法界面，可以查看你的魔法级和魔法值",
        "§b2. 魔法值会自动恢复，每秒恢复1点",
        "§b3. 随着你使用魔法，你的魔法级会提升",
        "§b4. 魔法级越高，你能使用的魔法越强大",
        "§b5. 你可以通过使用魔法杖等物品来消耗魔法值",
        "§b6. 不同的魔法需要不同的魔法值消耗",
        "§b7. 魔法级达到一定等级后，可以解锁更高级的魔法",
        "§b8. 你可以在魔法界面中查看已解锁的魔法",
        "§b9. 使用魔法时要注意魔法值的消耗，避免魔法值耗尽",
        "§b10. 魔法杖可以通过合成获得，也可以在村庄的铁匠铺购买",
        "§b11. 除了魔法杖，还有其他魔法物品可以使用",
        "§b12. 魔法技能需要不断练习才能提升等级",
        "§b13. 当你遇到困难时，可以回来找我咨询更多魔法知识"
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

        client.player.sendMessage(Text.literal("§a图书管理员: 你好，年轻的冒险者！我是这里的图书管理员。").formatted(net.minecraft.util.Formatting.GOLD), false);
        client.player.sendMessage(Text.literal("§e按空格键继续...").formatted(net.minecraft.util.Formatting.YELLOW), false);
    }

    private void showAsk() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(Text.literal("§a图书管理员: 你知道 SimpleMagic 模组的玩法吗？").formatted(net.minecraft.util.Formatting.GOLD), false);
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