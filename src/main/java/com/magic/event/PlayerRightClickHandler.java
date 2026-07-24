package com.magic.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.MagicMod;

public class PlayerRightClickHandler {
    
    public static void register() {
        // 监听玩家使用物品事件（包括空手右键）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // 只在服务器端处理，避免客户端-服务器不一致
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            
            // 进行射线检测，寻找玩家前方的实体
            EntityHitResult hitResult = raycastForEntity(player, 5.0);
            
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = hitResult.getEntity();
                
                // 检查是否是魔法阵物品实体
                // 现在不允许通过射线检测右键收回，只能通过右键花岗岩收回
                if (entity instanceof MagicCircleItemEntity) {
                    // 提示玩家使用正确的方式收回
                    player.sendMessage(net.minecraft.text.Text.literal("§e要收回魔法阵，请右键磨制花岗岩！").formatted(net.minecraft.util.Formatting.YELLOW), false);
                    // 返回PASS，不执行交互
                    return ActionResult.PASS;
                }
            }
            
            return ActionResult.PASS;
        });
    }
    
    /**
     * 进行射线检测，寻找玩家前方的实体
     * @param player 玩家
     * @param maxDistance 最大检测距离
     * @return 实体命中结果，如果没有命中实体则返回null
     */
    private static EntityHitResult raycastForEntity(PlayerEntity player, double maxDistance) {
        try {
            // 使用玩家的视线方向进行射线检测
            HitResult hitResult = player.raycast(maxDistance, 1.0f, false);
            
            // 检查是否命中实体
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                return (EntityHitResult) hitResult;
            }
            
            return null;
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 射线检测异常", e);
            return null;
        }
    }
}