package com.magic.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import com.magic.item.ItemsRegistry;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuideBookScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.of("magic-mod", "textures/guide/background.png");
    private static final Identifier CRYSTAL_BALL_RECIPE = Identifier.of("magic-mod", "textures/guide/recipe_crystal_ball.png");
    private static final Identifier LEVITATION_WAND_RECIPE = Identifier.of("magic-mod", "textures/guide/recipe_levitation_wand.png");
    private static final Identifier DAMAGE_WAND_RECIPE = Identifier.of("magic-mod", "textures/guide/recipe_damage_wand.png");
    private static final Identifier ENCHANTMENT_FLOW = Identifier.of("magic-mod", "textures/guide/enchantment_system.png");
    
    private int currentPage = 0;
    private final List<Page> pages = new ArrayList<>();
    private ButtonWidget prevButton;
    private ButtonWidget nextButton;
    private ButtonWidget closeButton;
    private final Set<Integer> readPages = new HashSet<>();
    private float itemRotation = 0.0f;
    private float itemScale = 1.0f;
    private boolean isDragging = false;
    private float lastMouseX = 0.0f;
    private float lastMouseY = 0.0f;
    
    public GuideBookScreen() {
        super(Text.literal("魔法模组指南"));
        initPages();
    }
    
    private void initPages() {
        // 第1页：模组介绍（仅文本）
        pages.add(new Page(
            "§l魔法模组指南 v0.0.6§r\n\n" +
            "§n作者：§roblongbass(QT)\n" +
            "§n适用版本：§rMinecraft 1.21.8+\n\n" +
            "§n核心特色：§r\n" +
            "• 魔法物品系统\n" +
            "• 自定义生物与掉落\n" +
            "• 附魔与合成扩展\n" +
            "• 交互式魔法工具\n\n" +
            "§n模组目标：§r\n" +
            "为游戏添加简单而有趣的魔法体验，适合生存与冒险模式。"
        ));
        
        // 第2页：物品介绍
        pages.add(new Page(
            "§l物品详细介绍§r\n\n" +
            "§n魔法之星§r\n" +
            "- 基础魔法材料\n" +
            "- 魔法僵尸35%几率掉落\n" +
            "- 所有合成的核心成分\n\n" +
            "§n浮空魔法杖§r\n" +
            "- 使目标生物悬浮7秒\n" +
            "- 冷却时间：2秒\n" +
            "- 无需附魔，直接使用\n\n" +
            "§n伤害魔法杖§r\n" +
            "- 蓄力造成魔法伤害\n" +
            "- 最大伤害：10点\n" +
            "- 可附魔增强威力",
            new ItemStack(ItemsRegistry.MAGIC_STAR)
        ));
        
        // 第3页：水晶球合成（合成网格显示）
        ItemStack[] crystalBallGrid = new ItemStack[9];
        // 第一行
        crystalBallGrid[0] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        crystalBallGrid[1] = new ItemStack(Items.AMETHYST_SHARD);
        crystalBallGrid[2] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        // 第二行  
        crystalBallGrid[3] = new ItemStack(Items.AMETHYST_SHARD);
        crystalBallGrid[4] = new ItemStack(Items.REDSTONE);
        crystalBallGrid[5] = new ItemStack(Items.AMETHYST_SHARD);
        // 第三行
        crystalBallGrid[6] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        crystalBallGrid[7] = new ItemStack(Items.AMETHYST_SHARD);
        crystalBallGrid[8] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        
        pages.add(new Page(
            "§l水晶球合成配方§r\n\n" +
            "§n材料：§r\n" +
            "- 4魔法之星\n" +
            "- 1红石\n" +
            "- 4紫水晶碎片\n\n" +
            "§n合成布局：§r\n" +
            "★ 💎 ★\n" +
            "💎 🔴 💎\n" +
            "★ 💎 ★\n\n" +
            "★=魔法之星 💎=紫水晶碎片 🔴=红石\n\n" +
            "§6提示：§r右侧查看合成网格",
            crystalBallGrid
        ));
        
        // 第4页：浮空魔法杖合成（合成网格显示）
        ItemStack[] levitationWandGrid = new ItemStack[9];
        // 第一行
        levitationWandGrid[0] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        levitationWandGrid[1] = new ItemStack(Items.STICK);
        levitationWandGrid[2] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        // 第二行  
        levitationWandGrid[3] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        levitationWandGrid[4] = new ItemStack(Items.STICK);
        levitationWandGrid[5] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        // 第三行 - 空
        levitationWandGrid[6] = null;
        levitationWandGrid[7] = null;
        levitationWandGrid[8] = null;
        
        pages.add(new Page(
            "§l浮空魔法杖合成配方§r\n\n" +
            "§n材料：§r\n" +
            "- 4魔法之星\n" +
            "- 2木棍\n\n" +
            "§n合成布局：§r\n" +
            "★ 🎋 ★\n" +
            "★ 🎋 ★\n" +
            "      \n\n" +
            "★=魔法之星 🎋=木棍\n\n" +
            "§6提示：§r右侧查看合成网格",
            levitationWandGrid
        ));
        
        // 第5页：伤害魔法杖合成（合成网格显示）
        ItemStack[] damageWandGrid = new ItemStack[9];
        // 所有位置都是魔法之星，除了中心是钻石剑
        for (int i = 0; i < 9; i++) {
            damageWandGrid[i] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        }
        damageWandGrid[4] = new ItemStack(Items.DIAMOND_SWORD); // 中心位置
        
        pages.add(new Page(
            "§l伤害魔法杖合成配方§r\n\n" +
            "§n材料：§r\n" +
            "- 8魔法之星\n" +
            "- 1钻石剑\n\n" +
            "§n合成布局：§r\n" +
            "★ ★ ★\n" +
            "★ ⚔️ ★\n" +
            "★ ★ ★\n\n" +
            "★=魔法之星 ⚔️=钻石剑\n\n" +
            "§6提示：§r右侧查看合成网格",
            damageWandGrid
        ));
        
        // 第6页：附魔系统（仅文本）
        pages.add(new Page(
            "§l附魔系统§r\n\n" +
            "§n蓄能附魔§r\n" +
            "- 材料：书 + 红石\n" +
            "- 产出：未激活蓄能附魔书\n" +
            "- 右键激活\n\n" +
            "§n伤能附魔§r\n" +
            "- 材料：书 + 一级普通力量药水\n" +
            "- 产出：未激活伤能附魔书\n" +
            "- 右键激活"
        ));
        
        // 第7页：魔法僵尸（仅文本）
        pages.add(new Page(
            "§l魔法僵尸§r\n\n" +
            "§n生成条件：§r\n" +
            "- 亮度低于5的区域\n" +
            "- 玩家周围24-48格范围\n" +
            "- 夜间或地下洞穴\n\n" +
            "§n特性：§r\n" +
            "- 生命值：25点（12.5心）\n" +
            "- 攻击伤害：4点（2心）\n" +
            "- 阳光下会燃烧\n" +
            "- 移动速度略快于普通僵尸\n" +
            "- 会隐藏为正常僵尸\n\n" +
            "§n掉落物：§r\n" +
            "- 魔法之星（35%几率）\n" +
            "- 经验球（5-10点）"
        ));
        
        // 第8页：使用技巧（仅文本）
        pages.add(new Page(
            "§l使用技巧§r\n\n" +
            "1. §n法杖使用§r：准确瞄准生物，悬浮效果无视护甲\n" +
            "2. §n水晶球§r：占卜结果随机，适合娱乐与探索\n" +
            "3. §n照明管理§r：保持基地照明，预防魔法僵尸生成\n" +
            "4. §n附魔策略§r：优先为伤害法杖附魔，提升输出\n" +
            "5. §n资源收集§r：建立刷怪塔高效获取魔法之星\n" +
            "6. §n合成提示§r：紫水晶碎片可通过交易或采矿获得"
        ));
        
        // 第9页：传送魔法阵合成（合成网格显示）
        ItemStack[] magicCircleGrid = new ItemStack[9];
        // 第一行
        magicCircleGrid[0] = new ItemStack(Items.REDSTONE);
        magicCircleGrid[1] = new ItemStack(Items.ENDER_PEARL);
        magicCircleGrid[2] = new ItemStack(Items.REDSTONE);
        // 第二行
        magicCircleGrid[3] = new ItemStack(Items.REDSTONE);
        magicCircleGrid[4] = new ItemStack(ItemsRegistry.MAGIC_STAR);
        magicCircleGrid[5] = new ItemStack(Items.REDSTONE);
        // 第三行
        magicCircleGrid[6] = new ItemStack(Items.REDSTONE);
        magicCircleGrid[7] = new ItemStack(Items.ENDER_PEARL);
        magicCircleGrid[8] = new ItemStack(Items.REDSTONE);

        pages.add(new Page(
            "§l传送魔法阵合成配方§r\n\n" +
            "§n功能：§r\n" +
            "- 放置魔法阵作为传送锚点\n" +
            "- 按下F7键返回魔法阵中心\n" +
            "- 每个玩家只能拥有一个魔法阵\n\n" +
            "§n材料：§r\n" +
            "- 4红石\n" +
            "- 1魔法之星\n" +
            "- 4末影珍珠\n\n" +
            "§n合成布局：§r\n" +
            "🔴 🟣 🔴\n" +
            "🔴 ★  🔴\n" +
            "🔴 🟣 🔴\n\n" +
            "🔴=红石 🟣=末影珍珠 ★=魔法之星\n\n" +
            "§6提示：§r右侧查看合成网格\n\n" +
            "§n使用方法：§r\n" +
            "1. §n放置§r：手持传送法阵卷轴右键地面\n" +
            "2. §n传送§r：按下F7键（无论身在何处）\n" +
            "3. §n收回§r：右键花岗岩上的魔法阵物品",
            magicCircleGrid
        ));
        
        // 第10页：联系信息（仅文本）
        pages.add(new Page(
            "§l联系与支持§r\n\n" +
            "§nQQ群：§r824133572\n" +
            "§nGitHub：§rgithub.com/oblongbass/MagicMod\n" +
            "§nModrinth：§rmodrinth.com/mod/simple-magic\n" +
            "§nBilibili：§rspace.bilibili.com/1707955197\n\n" +
            "感谢使用魔法模组！\n" +
            "祝您游戏愉快！"
        ));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 80;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int bottomY = this.height - 30;
        
        // 上一页按钮
        prevButton = ButtonWidget.builder(Text.literal("◀ 上一页"), button -> {
            if (currentPage > 0) {
                if (this.client != null && this.client.player != null) {
                    this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                }
                currentPage--;
                readPages.add(currentPage);
                updateButtons();
            }
        })
        .dimensions(centerX - buttonWidth - 10, bottomY, buttonWidth, buttonHeight)
        .build();
        
        // 下一页按钮
        nextButton = ButtonWidget.builder(Text.literal("下一页 ▶"), button -> {
            if (currentPage < pages.size() - 1) {
                if (this.client != null && this.client.player != null) {
                    this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                }
                currentPage++;
                readPages.add(currentPage);
                updateButtons();
            }
        })
        .dimensions(centerX + 10, bottomY, buttonWidth, buttonHeight)
        .build();
        
        // 关闭按钮
        closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> {
            if (this.client != null && this.client.player != null) {
                this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
            this.client.setScreen(null);
        })
        .dimensions(this.width - buttonWidth - 10, 10, buttonWidth, buttonHeight)
        .build();
        
        this.addDrawableChild(prevButton);
        this.addDrawableChild(nextButton);
        this.addDrawableChild(closeButton);
        updateButtons();
        readPages.add(currentPage);
    }
    
    private void updateButtons() {
        prevButton.active = currentPage > 0;
        nextButton.active = currentPage < pages.size() - 1;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 手动绘制完全不透明的深灰色背景，避免调用 renderBackground 导致的 applyBlur 问题，同时提高文本可见性
        context.fill(0, 0, this.width, this.height, 0xFF202020);
        

        
        // 先调用super.render()渲染按钮等子组件
        super.render(context, mouseX, mouseY, delta);
        
        // 检查文本渲染器是否可用
        if (this.textRenderer == null) {
            return; // 如果textRenderer为null，无法渲染文本
        }
        

        

        

        
        // 渲染标题
        String titleText = "魔法模组指南 - 第" + (currentPage + 1) + "页/共" + pages.size() + "页";
        if (readPages.contains(currentPage)) {
            titleText += " ✓";
        }
        Text title = Text.literal(titleText);
        int titleWidth = this.textRenderer.getWidth(title);
        int titleX = (this.width - titleWidth) / 2;
        int titleY = 50; // 从20调整到50，避免与按钮重叠
        context.drawTextWithShadow(this.textRenderer, title, titleX, titleY, 0xFFFFFFFF);
        
        // 渲染当前页内容
        Page page = pages.get(currentPage);
        renderPageContent(context, page, delta);
    }
    
    private void renderPageContent(DrawContext context, Page page, float delta) {
        int contentX = this.width / 4 + 50;
        int contentY = 100; // 适中位置
        int maxLines = 20; // 最大显示行数
        
        // 分割文本行
        String[] lines = page.text.split("\n");
        
        // 计算实际需要显示的行数
        int displayLines = Math.min(lines.length, maxLines);
        
        // 绘制文本内容 - 按照docs.txt指南使用带阴影的文本
        for (int i = 0; i < displayLines; i++) {
            // 确保文本不为空
            if (lines[i] != null && !lines[i].trim().isEmpty()) {
                // 使用Text.literal()，Minecraft会自动处理§格式代码
                Text lineText = Text.literal(lines[i]);
                // 使用白色文本，带阴影，确保在深色背景上清晰可见
                context.drawTextWithShadow(this.textRenderer, lineText, contentX, contentY + i * 12, 0xFFFFFFFF);
            }
        }
        
        // 根据页面类型渲染右侧内容
        int rightX = this.width * 3 / 4 - 64 + 50;
        int rightY = contentY + 20;
        int rightSize = 128;
        
        switch (page.type) {
            case SINGLE_ITEM:
                // 单个物品页面 - 2D预览
                if (page.itemStack != null && !page.itemStack.isEmpty()) {
                    // 绘制物品预览背景和边框
                    context.fill(rightX - 2, rightY - 2, rightX + rightSize + 2, rightY + rightSize + 2, 0xFF8B4513);
                    context.fill(rightX, rightY, rightX + rightSize, rightY + rightSize, 0x80000000);
                    
                    // 计算物品居中位置
                    int itemX = rightX + (rightSize - 20) / 2;
                    int itemY = rightY + (rightSize - 20) / 2;
                    
                    // 绘制2D物品（放大显示）
                    context.drawItem(page.itemStack, itemX, itemY);
                    
                    // 绘制提示
                    String itemHint = "物品预览";
                    int itemHintWidth = this.textRenderer.getWidth(itemHint);
                    int itemHintX = rightX + (rightSize - itemHintWidth) / 2;
                    int itemHintY = rightY + rightSize + 10;
                    context.drawTextWithShadow(this.textRenderer, Text.literal(itemHint), itemHintX, itemHintY, 0xFFFFFF00);
                }
                break;
                
            case CRAFTING_GRID:
                // 合成网格页面 - 左侧合成网格，右侧输出物品
                if (page.craftingGrid != null) {
                    // 计算布局：合成网格在左侧，输出物品在右侧
                    int gridSize = rightSize * 2 / 3; // 合成网格占2/3宽度
                    int outputSize = rightSize / 3;   // 输出物品占1/3宽度
                    
                    int gridX = rightX - 40; // 向左移动40像素，适中位置
                    int outputX = gridX + gridSize + 2; // 右侧，留2像素间隔，紧挨合成网格
                    int outputY = rightY + (gridSize - outputSize) / 2; // 垂直居中于合成网格
                    
                    // 绘制合成网格背景和边框
                    context.fill(gridX - 2, rightY - 2, gridX + gridSize + 2, rightY + gridSize + 2, 0xFF8B4513);
                    context.fill(gridX, rightY, gridX + gridSize, rightY + gridSize, 0x80000000);
                    
                    // 绘制合成网格
                    drawCraftingGrid(context, page.craftingGrid, gridX, rightY, gridSize);
                    
                    // 绘制输出物品背景和边框
                    context.fill(outputX - 2, outputY - 2, outputX + outputSize + 2, outputY + outputSize + 2, 0xFF8B4513);
                    context.fill(outputX, outputY, outputX + outputSize, outputY + outputSize, 0x80000000);
                    
                    // 根据当前页面确定输出物品
                    ItemStack outputItem = getCraftingOutput(currentPage);
                    if (outputItem != null && !outputItem.isEmpty()) {
                        // 计算物品居中位置（在输出方格内居中）
                        int itemX = outputX + (outputSize - 20) / 2;
                        int itemY = outputY + (outputSize - 20) / 2;
                        
                        // 绘制输出物品
                        context.drawItem(outputItem, itemX, itemY);
                    }
                }
                break;
                
            case TEXT_ONLY:
                // 仅文本页面，不渲染右侧内容
                break;
        }
        
        // 如果文本行数超过最大显示行数，显示提示
        if (lines.length > maxLines) {
            Text moreText = Text.literal("§7... 还有 " + (lines.length - maxLines) + " 行未显示");
            int moreX = contentX;
            int moreY = contentY + displayLines * 12 + 10;
            context.drawTextWithShadow(this.textRenderer, moreText, moreX, moreY, 0xFFAAAAAA);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void draw3DItem(DrawContext context, ItemStack itemStack, int centerX, int centerY, float rotation) {
        // 简单绘制物品，应用缩放和旋转效果
        int itemSize = 16; // 物品默认大小
        
        // 计算缩放后的大小
        int scaledSize = (int)(itemSize * itemScale);
        
        // 计算绘制位置（居中）
        int drawX = centerX - scaledSize / 2;
        int drawY = centerY - scaledSize / 2;
        
        // 绘制物品
        context.drawItem(itemStack, drawX, drawY);
        
        // 绘制旋转指示器（简单圆形）
        int indicatorRadius = scaledSize / 2 + 5;
        for (int i = 0; i < 12; i++) {
            float angle = rotation + (float)i * (float)Math.PI / 6.0f;
            int px = centerX + (int)(Math.cos(angle) * indicatorRadius);
            int py = centerY + (int)(Math.sin(angle) * indicatorRadius);
            context.fill(px - 1, py - 1, px + 1, py + 1, 0xFFFFFF00);
        }
    }
    
    private void drawCraftingGrid(DrawContext context, ItemStack[] grid, int startX, int startY, int totalSize) {
        // 计算每个格子的尺寸
        int cellSize = totalSize / 3;
        int padding = 2;
        
        // 绘制3x3网格
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                int cellX = startX + col * cellSize;
                int cellY = startY + row * cellSize;
                
                // 绘制格子背景
                context.fill(cellX + padding, cellY + padding, 
                            cellX + cellSize - padding, cellY + cellSize - padding, 
                            0x60FFFFFF);
                
                // 绘制格子边框
                context.drawBorder(cellX, cellY, cellSize, cellSize, 0xFF8B4513);
                
                // 绘制物品（如果有）
                if (index < grid.length && grid[index] != null && !grid[index].isEmpty()) {
                    // 计算物品居中位置
                    int itemX = cellX + (cellSize - 16) / 2;
                    int itemY = cellY + (cellSize - 16) / 2;
                    context.drawItem(grid[index], itemX, itemY);
                }
            }
        }
        
        // 绘制网格线
        for (int i = 1; i <= 2; i++) {
            // 垂直线
            context.fill(startX + i * cellSize - 1, startY, 
                        startX + i * cellSize + 1, startY + totalSize, 
                        0xFF000000);
            // 水平线
            context.fill(startX, startY + i * cellSize - 1, 
                        startX + totalSize, startY + i * cellSize + 1, 
                        0xFF000000);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 首先处理按钮点击
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        
        // 检查是否点击在物品查看器区域（仅对SINGLE_ITEM类型页面）
        Page page = pages.get(currentPage);
        if (page.type == PageType.SINGLE_ITEM && page.itemStack != null && !page.itemStack.isEmpty()) {
            int itemX = this.width * 3 / 4 - 64;
            int itemY = 80 + 20; // contentY + 20
            int itemSize = 128;
            
            if (mouseX >= itemX && mouseX <= itemX + itemSize && 
                mouseY >= itemY && mouseY <= itemY + itemSize) {
                if (button == 0) { // 左键
                    isDragging = true;
                    lastMouseX = (float) mouseX;
                    lastMouseY = (float) mouseY;
                    return true;
                } else if (button == 1) { // 右键
                    // 重置旋转和缩放
                    itemRotation = 0.0f;
                    itemScale = 1.0f;

                    return true;
                }
            }
        }
        
        return handled;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            // 根据鼠标移动更新旋转
            float currentMouseX = (float) mouseX;
            float deltaRotation = (currentMouseX - lastMouseX) * 0.01f;
            itemRotation += deltaRotation;
            lastMouseX = currentMouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 检查是否在物品查看器区域（仅对SINGLE_ITEM类型页面）
        Page page = pages.get(currentPage);
        if (page.type == PageType.SINGLE_ITEM && page.itemStack != null && !page.itemStack.isEmpty()) {
            int itemX = this.width * 3 / 4 - 64;
            int itemY = 80 + 20; // contentY + 20
            int itemSize = 128;
            
            if (mouseX >= itemX && mouseX <= itemX + itemSize && 
                mouseY >= itemY && mouseY <= itemY + itemSize) {
                // 更新缩放
                itemScale += verticalAmount * 0.1f;
                itemScale = Math.max(0.5f, Math.min(itemScale, 3.0f)); // 限制缩放范围
                

                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    private enum PageType {
        TEXT_ONLY,          // 仅文本
        SINGLE_ITEM,        // 单个物品
        CRAFTING_GRID       // 合成网格
    }
    
    private static class Page {
        public final String text;
        public final PageType type;
        public final ItemStack itemStack;           // 用于SINGLE_ITEM类型
        public final ItemStack[] craftingGrid;      // 用于CRAFTING_GRID类型，3x3数组
        
        // 仅文本页面
        public Page(String text) {
            this.text = text;
            this.type = PageType.TEXT_ONLY;
            this.itemStack = null;
            this.craftingGrid = null;
        }
        
        // 单个物品页面
        public Page(String text, ItemStack itemStack) {
            this.text = text;
            this.type = PageType.SINGLE_ITEM;
            this.itemStack = itemStack;
            this.craftingGrid = null;
        }
        
        // 合成网格页面
        public Page(String text, ItemStack[] craftingGrid) {
            this.text = text;
            this.type = PageType.CRAFTING_GRID;
            this.itemStack = null;
            this.craftingGrid = craftingGrid;
        }
    }
    
    private ItemStack getCraftingOutput(int pageIndex) {
        // 根据页面索引返回合成输出物品
        switch (pageIndex) {
            case 2: // 第3页：水晶球配方
                return new ItemStack(ItemsRegistry.CRYSTAL_BALL);
            case 3: // 第4页：浮空魔法杖配方
                return new ItemStack(ItemsRegistry.MAGIC_WAND_LEVITATION);
            case 4: // 第5页：伤害魔法杖配方
                return new ItemStack(ItemsRegistry.MAGIC_WAND_DAMAGE);
            case 8: // 第9页：传送魔法阵配方
                return new ItemStack(ItemsRegistry.MAGIC_CIRCLE);
            default:
                return ItemStack.EMPTY;
        }
    }
    
    private void drawArrow(DrawContext context, int x, int y, int color) {
        // 绘制简单箭头（→）
        // 箭头主体
        context.fill(x, y + 6, x + 10, y + 10, color);
        // 箭头头部
        context.fill(x + 10, y + 4, x + 14, y + 12, color);
        context.fill(x + 14, y + 6, x + 18, y + 10, color);
    }
}
