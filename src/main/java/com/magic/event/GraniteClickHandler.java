package com.magic.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.MagicMod;

public class GraniteClickHandler {
    
    public static void register() {
        // 监听玩家使用方块事件（右键方块）
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // 只在服务器端处理，避免客户端-服务器不一致
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            
            BlockPos pos = hitResult.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();
            
            // 检查是否右键了平滑花岗岩
            if (block == Blocks.POLISHED_GRANITE) {
                // 检查花岗岩上方是否有魔法阵物品实体
                MagicCircleItemEntity itemEntity = findMagicCircleItemAboveGranite(world, pos, player.getUuid());
                
                if (itemEntity != null) {
                    // 调用魔法阵物品实体的interact方法
                    ActionResult result = itemEntity.interact(player, hand);
                    
                    // 如果交互成功，阻止默认行为
                    if (result.isAccepted()) {
                        return ActionResult.SUCCESS;
                    } else {
                        player.sendMessage(net.minecraft.text.Text.literal("§c[魔法阵] 收回失败，原因：" + result.toString()).formatted(net.minecraft.util.Formatting.RED), false);
                    }
                }
            }
            
            return ActionResult.PASS;
        });
    }
    
    /**
     * 在平滑花岗岩上方查找对应的魔法阵物品实体
     * @param world 世界
     * @param granitePos 花岗岩位置
     * @param playerUuid 玩家UUID（用于检查所有权）
     * @return 找到的魔法阵物品实体，如果未找到则返回null
     */
    private static MagicCircleItemEntity findMagicCircleItemAboveGranite(World world, BlockPos granitePos, java.util.UUID playerUuid) {
        try {
            // 花岗岩上方1格（物品实体位置）
            BlockPos itemPos = granitePos.up();
            
            // 创建一个小区域搜索魔法阵物品实体
            Box searchBox = new Box(
                itemPos.getX() - 0.5, itemPos.getY() - 0.5, itemPos.getZ() - 0.5,
                itemPos.getX() + 1.5, itemPos.getY() + 1.5, itemPos.getZ() + 1.5
            );
            
            // 获取区域内的所有实体
            for (Entity entity : world.getOtherEntities(null, searchBox)) {
                if (entity instanceof MagicCircleItemEntity) {
                    MagicCircleItemEntity itemEntity = (MagicCircleItemEntity) entity;
                    
                    // 检查实体位置是否在花岗岩正上方
                    BlockPos entityPos = entity.getBlockPos();
                    if (entityPos.getX() == itemPos.getX() && 
                        entityPos.getZ() == itemPos.getZ() && 
                        Math.abs(entityPos.getY() - itemPos.getY()) <= 1) {
                        return itemEntity;
                    }
                }
            }
            
            // 如果没有在正上方找到，扩大搜索范围
            Box largerBox = new Box(
                granitePos.getX() - 2, granitePos.getY(), granitePos.getZ() - 2,
                granitePos.getX() + 3, granitePos.getY() + 3, granitePos.getZ() + 3
            );
            
            for (Entity entity : world.getOtherEntities(null, largerBox)) {
                if (entity instanceof MagicCircleItemEntity) {
                    MagicCircleItemEntity itemEntity = (MagicCircleItemEntity) entity;
                    return itemEntity;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 查找魔法阵物品实体异常", e);
            return null;
        }
    }
}