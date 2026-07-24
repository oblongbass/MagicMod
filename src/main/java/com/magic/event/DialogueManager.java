package com.magic.event;

public class DialogueManager {
    private int currentStep = 0;
    private boolean knowsMagic = false;
    private boolean tutorialCompleted = false;

    // Dialogue steps
    private static final String[] DIALOGUES = {
        "你好，年轻的冒险者！我是这里的图书管理员。我注意到你似乎对魔法有些兴趣...",
        "你知道 SimpleMagic 模组的玩法吗？",
        "很高兴听到你已经了解！那你一定知道魔法的基本原理了。",
        "没关系，我来慢慢教你。首先，让我为你解锁魔法技能。",
        "太好了！你现在已经解锁了魔法技能。让我来告诉你如何使用：",
        "1. 按 I 键打开魔法界面，可以查看你的魔法级和魔法值",
        "2. 魔法值会自动恢复，每秒恢复1点",
        "3. 随着你使用魔法，你的魔法级会提升",
        "4. 魔法级越高，你能使用的魔法越强大",
        "5. 你可以通过使用魔法杖等物品来消耗魔法值",
        "6. 不同的魔法需要不同的魔法值消耗",
        "7. 魔法级达到一定等级后，可以解锁更高级的魔法",
        "8. 你可以在魔法界面中查看已解锁的魔法",
        "9. 使用魔法时要注意魔法值的消耗，避免魔法值耗尽",
        "10. 魔法杖可以通过合成获得，也可以在村庄的铁匠铺购买",
        "11. 除了魔法杖，还有其他魔法物品可以使用",
        "12. 魔法技能需要不断练习才能提升等级",
        "13. 当你遇到困难时，可以回来找我咨询更多魔法知识",
        "记住，魔法需要不断练习才能掌握。祝你在魔法之路上一切顺利！"
    };

    // Dialogue options
    private static final String[][] OPTIONS = {
        null, // Step 0: No options
        {"知道", "不知道"}, // Step 1: Options for knowing magic
        null, // Step 2: No options (already knows)
        null, // Step 3: No options (doesn't know)
        null, // Step 4: No options (unlocked)
        null, // Step 5: No options
        null, // Step 6: No options
        null, // Step 7: No options
        null, // Step 8: No options
        null, // Step 9: No options
        null, // Step 10: No options
        null, // Step 11: No options
        null, // Step 12: No options
        null, // Step 13: No options
        null, // Step 14: No options
        null, // Step 15: No options
        null, // Step 16: No options
        null  // Step 17: No options
    };

    public String getCurrentDialogue() {
        return DIALOGUES[Math.min(currentStep, DIALOGUES.length - 1)];
    }

    public String[] getCurrentOptions() {
        return OPTIONS[currentStep];
    }

    public void selectOption(int optionIndex) {
        if (currentStep == 1) { // First question
            if (optionIndex == 0) { // Knows magic
                knowsMagic = true;
                currentStep = DIALOGUES.length - 1; // Skip to final step
            } else { // Doesn't know magic
                knowsMagic = false;
                currentStep = 3;
            }
        }
    }

    public void next() {
        if (currentStep < DIALOGUES.length - 1) {
            currentStep++;
        } else {
            tutorialCompleted = true;
        }
    }

    public boolean isTutorialCompleted() {
        return tutorialCompleted;
    }

    public boolean knowsMagic() {
        return knowsMagic;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void reset() {
        currentStep = 0;
        knowsMagic = false;
        tutorialCompleted = false;
    }
}