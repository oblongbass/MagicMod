package com.magic.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import com.magic.networking.MagicNetworking;
import com.magic.networking.SimpleMagicPayload;
import com.magic.MagicMod;

@Environment(EnvType.CLIENT)
public class MagicCircleClientNetworking {
    
    public static void initialize() {
        MagicMod.LOGGER.info("[Magic] 初始化客户端网络数据包处理器");
        
        // 注册自定义Payload到PayloadTypeRegistry（客户端需要注册以解码从服务器发来的数据包）
        try {
            PayloadTypeRegistry.playS2C().register(SimpleMagicPayload.ID, SimpleMagicPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 SimpleMagicPayload 注册成功");
        } catch (IllegalArgumentException e) {
            // 如果已经注册，忽略这个错误（可能在开发环境中服务器端已经注册过）
            MagicMod.LOGGER.info("[Magic] SimpleMagicPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 SimpleMagicPayload 注册失败", e);
        }
        
        // 注册魔法阵放置数据包处理器
        try {
            ClientPlayNetworking.registerGlobalReceiver(SimpleMagicPayload.ID,
                (payload, context) -> {
                    // 检查是否是魔法阵放置数据包
                    if (payload.id().equals(MagicNetworking.MAGIC_CIRCLE_PLACED) && payload.position() != null) {
                        // 在客户端线程处理
                        context.client().execute(() -> {
                            if (context.client().player != null) {
                                // 激活魔法阵特效
                                MagicCircleRenderer.activateCircleForPlayer(
                                    context.client().player.getUuid(), 
                                    payload.position()
                                );
                                // 调试信息已移除
                            }
                        });
                    }
                    // 检查是否是魔法阵移除数据包
                    else if (payload.id().equals(MagicNetworking.MAGIC_CIRCLE_REMOVED)) {
                        // 在客户端线程处理
                        context.client().execute(() -> {
                            if (context.client().player != null) {
                                // 移除魔法阵特效
                                MagicCircleRenderer.deactivateCircleForPlayer(
                                    context.client().player.getUuid()
                                );
                                // 调试信息已移除
                            }
                        });
                    }
                });
                
            MagicMod.LOGGER.info("[Magic] 网络数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 网络数据包处理器注册失败", e);
        }
    }
}