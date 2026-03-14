package com.magic.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.magic.data.SimplePlayerDataManager;
import com.magic.MagicMod;

public class GraniteProtectionHandler {
    
    public static void register() {
        // 监听方块破坏事件（在破坏前）
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            // 只在服务器端处理
            if (world.isClient()) {
                return true; // 允许破坏
            }
            
            Block block = state.getBlock();
            
            // 检查是否是平滑花岗岩
            if (block == Blocks.POLISHED_GRANITE) {
                // 检查这个花岗岩是否是魔法阵的一部分
                if (isMagicCircleGranite(world, pos, (ServerPlayerEntity) player)) {
                    // 阻止破坏
                    player.sendMessage(net.minecraft.text.Text.literal("§c这个花岗岩是魔法阵的一部分，无法破坏！").formatted(net.minecraft.util.Formatting.RED), false);
                    MagicMod.LOGGER.info("[Magic] 阻止玩家 {} 破坏魔法阵花岗岩 {}", player.getName().getString(), pos);
                    return false;
                }
            }
            
            return true; // 允许破坏
        });
    }
    
    /**
     * 检查给定的方块位置是否是某个玩家的魔法阵花岗岩
     * @param world 世界
     * @param pos 方块位置
     * @param breaker 尝试破坏的玩家（可能不是所有者）
     * @return 如果是魔法阵花岗岩则返回true
     */
    private static boolean isMagicCircleGranite(World world, BlockPos pos, ServerPlayerEntity breaker) {
        // 遍历所有在线玩家，检查他们的魔法阵位置
        // 注意：这个方法效率不高，但玩家数量有限，可以接受
        if (world.getServer() != null) {
            for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
                if (SimplePlayerDataManager.hasActiveMagicCircle(player)) {
                    BlockPos magicCirclePos = SimplePlayerDataManager.getMagicCirclePos(player);
                    if (magicCirclePos != null && magicCirclePos.equals(pos)) {
                        // 检查是否在同一维度
                        String dimension = world.getRegistryKey().getValue().toString();
                        String magicDimension = SimplePlayerDataManager.getMagicCircleDimension(player);
                        if (dimension.equals(magicDimension)) {
                            // 如果是所有者，提示如何收回
                            if (player.getUuid().equals(breaker.getUuid())) {
                                breaker.sendMessage(net.minecraft.text.Text.literal("§e要收回魔法阵，请右键磨制花岗岩！").formatted(net.minecraft.util.Formatting.YELLOW), false);
                            } else {
                                breaker.sendMessage(net.minecraft.text.Text.literal("§c这是其他玩家的魔法阵，你无法破坏！").formatted(net.minecraft.util.Formatting.RED), false);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}