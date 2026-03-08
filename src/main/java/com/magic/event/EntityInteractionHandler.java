package com.magic.event;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.MagicMod;
import org.jetbrains.annotations.Nullable;

public class EntityInteractionHandler {
    
    public static void register() {
        // 使用高优先级确保我们的处理器先执行
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            return onEntityInteract(player, world, hand, entity, hitResult);
        });
    }
    
    private static ActionResult onEntityInteract(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        // 检查实体是否为null
        if (entity == null) {
            return ActionResult.PASS;
        }
        
        // 检查是否是魔法阵物品实体
        // 现在不允许直接右键物品实体收回，只能通过右键花岗岩收回
        if (entity instanceof MagicCircleItemEntity) {
            // 提示玩家使用正确的方式收回
            if (!world.isClient()) {
                player.sendMessage(net.minecraft.text.Text.literal("§e要收回魔法阵，请右键磨制花岗岩！").formatted(net.minecraft.util.Formatting.YELLOW), false);
            }
            // 返回PASS，允许其他处理器处理（实际上不会执行交互）
            return ActionResult.PASS;
        }
        
        return ActionResult.PASS;
    }
}