package com.magic.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.magic.MagicMod;

public class MagicNetworking {
    public static final Identifier MAGIC_CIRCLE_PLACED = Identifier.of("magic-mod", "magic_circle_placed");
    public static final Identifier MAGIC_CIRCLE_REMOVED = Identifier.of("magic-mod", "magic_circle_removed");
    
    public static void initialize() {
        MagicMod.LOGGER.info("[Magic] 初始化网络数据包");
        
        // 注册自定义Payload到PayloadTypeRegistry
        try {
            PayloadTypeRegistry.playS2C().register(SimpleMagicPayload.ID, SimpleMagicPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] SimpleMagicPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] SimpleMagicPayload 注册失败", e);
        }
    }
    
    // 向玩家发送魔法阵放置数据包
    public static void sendMagicCirclePlaced(ServerPlayerEntity player, Vec3d position) {
        if (player != null && !player.getWorld().isClient()) {
            try {
                // 创建自定义Payload
                SimpleMagicPayload payload = new SimpleMagicPayload(MAGIC_CIRCLE_PLACED, position);
                
                // 发送数据包
                ServerPlayNetworking.send(player, payload);
                // 调试信息已移除
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法阵放置数据包失败", e);
            }
        }
    }
    
    // 向玩家发送魔法阵移除数据包
    public static void sendMagicCircleRemoved(ServerPlayerEntity player) {
        if (player != null && !player.getWorld().isClient()) {
            try {
                // 创建自定义Payload（无位置数据）
                SimpleMagicPayload payload = new SimpleMagicPayload(MAGIC_CIRCLE_REMOVED);
                
                // 发送数据包
                ServerPlayNetworking.send(player, payload);
                // 调试信息已移除
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法阵移除数据包失败", e);
            }
        }
    }
}