package com.magic.client.screen;

import com.magic.item.ItemsRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class GuideBookScreen extends Screen {
    private int page = 0;
    private static final int PAGES = 8;

    // ========== 文字内容 ==========

    private static final String[][] PAGE_TEXT = {
            { // 第1页：基础入门
                    "§l✧ 魔法模组指南 ✧",
                    "",
                    "§6【基础入门】",
                    "§7一个充满魔法的世界，现在由你，",
                    "§7解锁魔法，向世界根源前进吧！",
                    "",
                    "§e✦ 解锁魔法",
                    "§7找到村庄中的图书管理员，",
                    "§7Shift+右键与他对话即可解锁。",
                    "",
                    "§e✦ 魔法数值",
                    "§7魔法值每秒自动恢复 §b1 §7点。",
                    "§7基础上限 §b100 §7。",
                    "",
                    "§e✦ 材料获取",
                    "§7魔法之星：击杀 §c魔法僵尸§7 掉落",
                    "§7魔法水晶：挖掘 §b魔法矿石§7 获得",
                    "",
                    "§e✦ 快捷键",
                    "§7按 §bI §7键打开魔法界面。",
                    "§7按 §bF7 §7传送到魔法阵。"
            },
            { // 第4页：元素系统
                    "§l✧ 元素系统 ✧",
                    "",
                    "§7法杖可切换§e4§7种元素：",
                    "§7  1=§6土  2=§b水  3=§c火  4=§7风",
                    "§7切换消耗§b5§7魔法值，0.5秒冷却",
                    "",
                    "§6【各元素简析】",
                    "§c火§7-使目标着火",
                    "§b水§7-附加水标记",
                    "§7风§7-扩散/引爆元素",
                    "§6土§7-禁锢目标",
                    "",
                    "§6【元素合成】",
                    "§7将6种物品丢在同一格，",
                    "§7消耗100魔法值合成：",
                    "§c  伤害法杖 + 漂浮法杖",
                    "§6  烈焰粉 + 羽毛 + 泥土",
                    "§b  潮涌核心",
                    "",
                    "§7详细战斗效果见下页 §e→"
            },
            { // 第7页：元素战斗详解
                    "§l✧ 元素战斗详解 ✧",
                    "",
                    "§6【火元素】",
                    "§7着火§e3§7秒+火标记(持续15秒)",
                    "",
                    "§6【水元素】",
                    "§7附加水标记(15秒)，无直接伤害",
                    "",
                    "§6【风元素】",
                    "§7❶ §d蒸发扩散§7：引爆§d蒸发状态§7→弱化蒸发",
                    "§7  伤害=蒸发§e×30%§7，仅触发一次",
                    "§7❷ 扩散火/水标记到周围§e5§7格",
                    "§7  扩散减半(火1秒/标记7.5秒)",
                    "§7  §6土§7标记无法被扩散",
                    "",
                    "§6【土元素】",
                    "§7禁锢§e1-5§7秒+土标记，无法被风扩散",
                    "",
                    "§6【蒸发反应】§c火+§b水§7",
                    "§7伤害§c10-90§7(血低→低伤，血高→高伤)",
                    "§7  可连击2-4次(50%几率追加)",
                    "§7  蒸发后残留§d蒸发状态§e5§7秒",
                    "§7  被风击中→弱化蒸发(伤害§c减半§7)"
            },
            { // 第8页：魔法阵与附魔
                    "§l✧ 魔法阵 & 附魔 ✧",
                    "",
                    "§6【魔法阵】",
                    "§7✦ 传送法阵卷轴右键放置",
                    "§7  消耗 §b50§7 魔法值",
                    "§7✦ 按 §eF7 §7传送回阵",
                    "§7  消耗 §b10§7 魔法值",
                    "§7✦ 右键磨制花岗岩收回",
                    "",
                    "§6【附魔书】（丢地上合成）",
                    "§7书 + 红石 → §e蓄能附魔书",
                    "§7书 + 力量药水 → §e伤能附魔书",
                    "§7右键激活后可用于伤害法杖",
                    "",
                    "§6【世界生成】",
                    "§7魔法矿石在地下生成，",
                    "§7挖掘获得魔法水晶。"
            }

    };

    // ========== 合成配方数据 ==========
    // 只保留已有的工作台配方，不添加新材料

    private record RecipeData(String title, ItemStack[] grid, ItemStack result) {}

    private static final RecipeData[] RECIPES = {
            new RecipeData("§6浮空魔法杖", new ItemStack[]{
                    stack("magic_star", 1), Items.STICK.getDefaultStack(), stack("magic_star", 1),
                    stack("magic_star", 1), Items.STICK.getDefaultStack(), stack("magic_star", 1),
                    null, null, null
            }, stack("magic_wand_levitation", 1)),

            new RecipeData("§6伤害魔法杖", new ItemStack[]{
                    stack("magic_star", 1), stack("magic_star", 1), stack("magic_star", 1),
                    stack("magic_star", 1), Items.DIAMOND_SWORD.getDefaultStack(), stack("magic_star", 1),
                    stack("magic_star", 1), stack("magic_star", 1), stack("magic_star", 1)
            }, stack("magic_wand_damage", 1)),

            new RecipeData("§d水晶球", new ItemStack[]{
                    stack("magic_star", 1), Items.AMETHYST_SHARD.getDefaultStack(), stack("magic_star", 1),
                    null, Items.REDSTONE.getDefaultStack(), null,
                    stack("magic_star", 1), Items.AMETHYST_SHARD.getDefaultStack(), stack("magic_star", 1)
            }, stack("crystal_ball", 1)),

            new RecipeData("§d传送法阵卷轴", new ItemStack[]{
                    Items.REDSTONE.getDefaultStack(), Items.ENDER_PEARL.getDefaultStack(), Items.REDSTONE.getDefaultStack(),
                    Items.REDSTONE.getDefaultStack(), stack("magic_star", 1), Items.REDSTONE.getDefaultStack(),
                    Items.REDSTONE.getDefaultStack(), Items.ENDER_PEARL.getDefaultStack(), Items.REDSTONE.getDefaultStack()
            }, stack("magic_circle", 1)),
    };

    private static ItemStack stack(String modItem, int count) {
        try {
            var field = ItemsRegistry.class.getDeclaredField(modItem.toUpperCase());
            var item = (net.minecraft.item.Item) field.get(null);
            var s = new ItemStack(item);
            s.setCount(count);
            return s;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public GuideBookScreen() {
        super(Text.literal("魔法模组指南"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xFF1A1A2E);
        int x = this.width / 2 - 140;
        int y = 20;

        // 书页背景
        context.fill(x - 10, y - 10, x + 280, y + 260, 0xFF2D2D44);
        context.fill(x - 8, y - 8, x + 278, y + 258, 0xFF1E1E36);

        if (this.textRenderer != null) {
            if (isRecipePage()) {
                renderRecipePage(context, x, y);
            } else {
                renderTextPage(context, x, y);
            }

            // 页码
            String pageStr = "第 " + (page + 1) + " / " + PAGES + " 页";
            context.drawText(this.textRenderer, pageStr,
                    x + 280 - this.textRenderer.getWidth(pageStr), y + 240, 0xFF888888, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean isRecipePage() {
        return page >= 1 && page <= 1 + RECIPES.length - 1;
    }

    private int recipeIndex() {
        return page - 1;
    }

    private void renderTextPage(DrawContext context, int x, int y) {
        // page: 0→基础入门, 5→元素系统, 6→魔法阵与附魔, 7→元素战斗详解
        int idx = page == 0 ? 0 : (page == 5 ? 1 : (page == 7 ? 3 : 2));
        int textY = y;
        for (String line : PAGE_TEXT[idx]) {
            if (line.isEmpty()) { textY += 10; continue; }
            context.drawText(this.textRenderer, Text.literal(line), x + 10, textY, 0xFFFFFFFF, false);
            textY += 11;
        }
    }

    private void renderRecipePage(DrawContext context, int x, int y) {
        int ri = recipeIndex();
        if (ri < 0 || ri >= RECIPES.length) return;
        RecipeData r = RECIPES[ri];

        // 标题
        context.drawText(this.textRenderer, Text.literal("§l" + r.title), x + 10, y + 5, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("§7工作台合成"), x + 10, y + 18, 0xFFAAAAAA, false);

        int gridX = x + 30;
        int gridY = y + 38;
        int slotSize = 22;
        int gap = 4;

        // 绘制3x3格子
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = gridX + col * (slotSize + gap);
                int sy = gridY + row * (slotSize + gap);
                // 格子背景
                context.fill(sx, sy, sx + slotSize, sy + slotSize, 0xFF3D3D5C);
                context.fill(sx + 1, sy + 1, sx + slotSize - 1, sy + slotSize - 1, 0xFF2A2A44);

                int idx = row * 3 + col;
                if (idx < r.grid.length && r.grid[idx] != null && !r.grid[idx].isEmpty()) {
                    context.drawItem(r.grid[idx], sx + 3, sy + 3);
                }
            }
        }

        // 箭号
        int arrowX = gridX + 3 * (slotSize + gap) + 8;
        int arrowY = gridY + slotSize;
        context.drawText(this.textRenderer, Text.literal("§e→"), arrowX, arrowY + 3, 0xFFFFAA00, false);

        // 结果
        int resultX = arrowX + 16;
        context.fill(resultX, arrowY, resultX + slotSize, arrowY + slotSize, 0xFF3D3D5C);
        context.fill(resultX + 1, arrowY + 1, resultX + slotSize - 1, arrowY + slotSize - 1, 0xFF2A2A44);
        if (!r.result.isEmpty()) {
            context.drawItem(r.result, resultX + 3, arrowY + 3);
        }

        // 结果名称和数量
        String resultName = r.result.getCount() > 1
                ? "§f" + r.result.getItem().getName().getString() + " §7×" + r.result.getCount()
                : "§f" + r.result.getItem().getName().getString();
        context.drawText(this.textRenderer, Text.literal(resultName),
                resultX, arrowY + slotSize + 4, 0xFFFFFFFF, false);
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int by = this.height - 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("◀ 上一页"), b -> {
            if (page > 0) page--;
        }).dimensions(cx - 100, by, 72, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("目录"), b -> {
            page = 0;
        }).dimensions(cx - 22, by, 44, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("下一页 ▶"), b -> {
            if (page < PAGES - 1) page++;
        }).dimensions(cx + 28, by, 72, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("✕"), b -> {
            this.client.setScreen(null);
        }).dimensions(cx + 108, by, 30, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int k = keyInput.key();
        if (k == 262 || k == 32) { if (page < PAGES - 1) page++; return true; }
        if (k == 263) { if (page > 0) page--; return true; }
        if (k == 256 || k == 335) { this.client.setScreen(null); return true; }
        return super.keyPressed(keyInput);
    }
}
