package com.magic.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.magic.MagicMod;
import java.util.UUID;

public class MagicNetworking {
    public static final Identifier MAGIC_CIRCLE_PLACED = Identifier.of("magic-mod", "magic_circle_placed");
    public static final Identifier MAGIC_CIRCLE_REMOVED = Identifier.of("magic-mod", "magic_circle_removed");
    public static final Identifier MANA_SYNC = Identifier.of("magic-mod", "mana_sync");
    
    public static void initialize() {
        MagicMod.LOGGER.info("[Magic] 初始化网络数据包");
        
        // 注册自定义Payload到PayloadTypeRegistry
        try {
            PayloadTypeRegistry.playS2C().register(SimpleMagicPayload.ID, SimpleMagicPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] SimpleMagicPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] SimpleMagicPayload 注册失败", e);
        }
        
        // 注册魔法值同步Payload
        try {
            PayloadTypeRegistry.playS2C().register(ManaSyncPayload.ID, ManaSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] ManaSyncPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] ManaSyncPayload 注册失败", e);
        }
    }
    
    // 向玩家发送魔法阵放置数据包（向后兼容，ownerUuid为null）
    public static void sendMagicCirclePlaced(ServerPlayerEntity player, Vec3d position) {
        sendMagicCirclePlaced(player, position, null);
    }
    
    // 向玩家发送魔法阵放置数据包（包含所有者UUID）
    public static void sendMagicCirclePlaced(ServerPlayerEntity player, Vec3d position, UUID ownerUuid) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                // 创建自定义Payload
                SimpleMagicPayload payload = new SimpleMagicPayload(MAGIC_CIRCLE_PLACED, position, ownerUuid);
                
                // 发送数据包
                ServerPlayNetworking.send(player, payload);
                // 调试信息已移除
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法阵放置数据包失败", e);
            }
        }
    }
    
    // 向玩家发送魔法阵移除数据包（向后兼容，ownerUuid为null）
    public static void sendMagicCircleRemoved(ServerPlayerEntity player) {
        sendMagicCircleRemoved(player, null);
    }
    
    // 向玩家发送魔法阵移除数据包（包含所有者UUID）
    public static void sendMagicCircleRemoved(ServerPlayerEntity player, UUID ownerUuid) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                // 创建自定义Payload（无位置数据）
                SimpleMagicPayload payload = new SimpleMagicPayload(MAGIC_CIRCLE_REMOVED, null, ownerUuid);
                
                // 发送数据包
                ServerPlayNetworking.send(player, payload);
                // 调试信息已移除
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法阵移除数据包失败", e);
            }
        }
    }
    
    // 向玩家发送魔法值同步数据包
    public static void sendManaSync(ServerPlayerEntity player, int currentMana, int maxMana) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                ManaSyncPayload payload = new ManaSyncPayload(currentMana, maxMana);
                ServerPlayNetworking.send(player, payload);
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法值同步数据包失败", e);
            }
        }
    }
}