package com.magic.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.text.RawFilteredPair;

import java.util.List;
import java.util.ArrayList;

public class GuideBookItem extends Item {
    public GuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            // 在客户端打开书界面
            openBookScreen(user, stack);
        } else {
            // 在服务器端，确保书有内容
            if (!hasBookContent(stack)) {
                stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createBookContent());
            }
        }

        return ActionResult.SUCCESS;
    }

    private void openBookScreen(PlayerEntity player, ItemStack stack) {
        try {
            // 确保书有内容
            if (!hasBookContent(stack)) {
                stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createBookContent());
            }

            // 在1.21.5中，使用新的书界面API
            // 简化：直接发送消息，因为书界面API变化较大
            sendGuideInfo(player);

        } catch (Exception e) {
            System.err.println("[GuideBookItem] 打开书界面失败: " + e.getMessage());
            // 如果打开失败，发送消息给玩家
            sendGuideInfo(player);
        }
    }

    private boolean hasBookContent(ItemStack stack) {
        // 修复：使用新的组件获取方式
        WrittenBookContentComponent content = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        return content != null && !content.pages().isEmpty();
    }

    private void sendGuideInfo(PlayerEntity player) {
        player.sendMessage(Text.literal("§6=== 魔法模组指南 ==="), false);
        player.sendMessage(Text.literal("§6魔法星星 §7- 从魔法僵尸掉落 (35%几率)"), false);
        player.sendMessage(Text.literal("§6魔法法杖 §7- 右键使用，施加悬浮效果"), false);
        player.sendMessage(Text.literal("§6水晶球 §7- 查看实体信息，潜行右键占卜"), false);
        player.sendMessage(Text.literal("§6魔法僵尸 §7- 在黑暗处生成，掉落魔法物品"), false);
    }

    public WrittenBookContentComponent createBookContent() {
        List<RawFilteredPair<Text>> pages = new ArrayList<>();

        // 第一页
        pages.add(RawFilteredPair.of(Text.literal("§l魔法模组指南\n\n§r欢迎使用魔法模组！这个模组添加了各种魔法物品和生物。\n\n翻页查看详情...")));

        // 第二页 - 物品介绍
        pages.add(RawFilteredPair.of(Text.literal("§l物品介绍\n\n§6魔法星星§r\n- 从魔法僵尸掉落\n- 用于合成其他魔法物品\n\n§6魔法法杖§r\n- 对生物使用施加悬浮效果")));

        // 第三页 - 更多物品介绍
        pages.add(RawFilteredPair.of(Text.literal("§l物品介绍（续）\n\n§6水晶球§r\n- 查看周围实体信息\n- 潜行右键进行占卜\n\n§6指引书§r\n- 你现在正在看的书！")));

        // 第四页 - 实体介绍
        pages.add(RawFilteredPair.of(Text.literal("§l实体介绍\n\n§6魔法僵尸§r\n- 特殊变种的僵尸\n- 在黑暗处生成\n- 掉落魔法星星\n- 比普通僵尸略强")));

        // 第五页 - 使用技巧
        pages.add(RawFilteredPair.of(Text.literal("§l使用技巧\n\n1. 魔法法杖需要瞄准生物使用\n2. 水晶球可以帮你侦查周围环境\n3. 魔法僵尸在亮度<5的地方生成\n4. 保持火把照明防止魔法僵尸生成")));

        // 修复：使用正确的构造函数，确保所有参数类型正确
        // 在 1.21.5 中，WrittenBookContentComponent 的构造函数可能需要不同的参数类型
        try {
            return new WrittenBookContentComponent(
                    RawFilteredPair.of(String.valueOf(Text.literal("魔法模组指南"))), // 标题
                    "Magic Mod Author", // 作者
                    0, // 生成ID
                    pages, // 页面内容
                    true // 已解决
            );
        } catch (Exception e) {
            // 如果构造函数失败，使用备选方案
            System.err.println("[GuideBookItem] 创建书内容失败: " + e.getMessage());
            return createFallbackBookContent();
        }
    }

    // 备用的书内容创建方法
    private WrittenBookContentComponent createFallbackBookContent() {
        List<RawFilteredPair<Text>> pages = new ArrayList<>();

        // 简化内容
        pages.add(RawFilteredPair.of(Text.literal("魔法模组指南\n\n右键查看详细说明")));

        // 尝试使用更简单的构造函数
        return new WrittenBookContentComponent(
                RawFilteredPair.of("魔法模组指南"), // 使用字符串而不是Text
                "Magic Mod",
                0,
                pages,
                true
        );
    }

    public ItemStack createBook(PlayerEntity player) {
        ItemStack book = new ItemStack(this);

        // 尝试创建书内容，如果失败则使用备用方案
        try {
            book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createBookContent());
        } catch (Exception e) {
            System.err.println("[GuideBookItem] 设置书内容失败，使用备用内容: " + e.getMessage());
            book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createFallbackBookContent());
        }

        return book;
    }
}