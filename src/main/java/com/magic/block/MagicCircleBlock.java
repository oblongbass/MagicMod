package com.magic.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MagicCircleBlock extends Block {
    
    public MagicCircleBlock(Block.Settings settings) {
        super(settings);
    }
    
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {
            // 显示信息
            player.sendMessage(Text.literal("§6这是魔法阵的中心石").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7站在魔法阵上按下F7可以传送回来").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("§7手持传送法阵卷轴右键可收回魔法阵").formatted(Formatting.GRAY), false);
        }
        return ActionResult.SUCCESS;
    }
}