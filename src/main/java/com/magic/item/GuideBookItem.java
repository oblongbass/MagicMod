package com.magic.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.text.RawFilteredPair;

import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Settings settings) {
        super(settings);
    }

    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.magic-mod.guide_book.tooltip"));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            user.setStackInHand(hand, createBook(user));
        }
        return ActionResult.SUCCESS;
    }

    public WrittenBookContentComponent createBookContent(PlayerEntity player) {
        List<RawFilteredPair<Text>> pages = List.of(
            RawFilteredPair.of(Text.literal("§6欢迎来到魔法模组！\n\n§f这本指引书将帮助你了解模组中的所有内容。\n\n§7翻页查看详细信息。")),
            RawFilteredPair.of(Text.literal("§6物品介绍：\n\n§6魔法星星 (Magic Star)\n§7- 从魔法僵尸身上掉落\n§7- 掉落概率：35%\n\n§6魔法法杖 (Magic Wand)\n§7- 具有悬浮效果\n§7- 右键使用")),
            RawFilteredPair.of(Text.literal("§6实体介绍：\n\n§6魔法僵尸 (Magic Zombie)\n§7- 特殊的僵尸变种\n§7- 会掉落魔法星星\n§7- 具有独特的魔法能力")),
            RawFilteredPair.of(Text.literal("§6制作配方：\n\n§6魔法法杖\n§7材料：\n§7- 木棍 x1\n§7- 魔法星星 x1\n\n§7在工作台中制作")),
            RawFilteredPair.of(Text.literal("§6使用说明：\n\n§6魔法法杖\n§7- 右键点击使用\n§7- 对目标施加悬浮效果\n§7- 消耗耐久度\n\n§6魔法星星\n§7- 用于制作魔法法杖\n§7- 也可以作为装饰品")),
            RawFilteredPair.of(Text.literal("§6注意事项：\n\n§7- 魔法僵尸只在特定条件下生成\n§7- 魔法法杖有使用次数限制\n§7- 悬浮效果有时间限制\n\n§6祝你在魔法世界中玩得开心！"))
        );
        return new WrittenBookContentComponent(
            RawFilteredPair.of("魔法模组指引书"),
            player.getName().getString(),
            0,
            pages,
            true
        );
    }

    public ItemStack createBook(PlayerEntity player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createBookContent(player));
        return book;
    }
} 