package com.magic.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import com.magic.networking.MagicNetworking;
import com.magic.networking.ManaSyncPayload;
import com.magic.networking.MagicLevelSyncPayload;
import com.magic.networking.DialogueStartPayload;
import com.magic.networking.SimpleMagicPayload;
import com.magic.networking.ElementSyncPayload;
import com.magic.MagicMod;
import java.util.UUID;

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
        
        // 注册魔法值同步Payload
        try {
            PayloadTypeRegistry.playS2C().register(ManaSyncPayload.ID, ManaSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 ManaSyncPayload 注册成功");
        } catch (IllegalArgumentException e) {
            MagicMod.LOGGER.info("[Magic] ManaSyncPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 ManaSyncPayload 注册失败", e);
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
                                // 使用数据包中的所有者UUID（如果存在），否则使用当前玩家UUID（向后兼容）
                                UUID ownerUuid = payload.ownerUuid() != null ? payload.ownerUuid() : context.client().player.getUuid();
                                // 调试日志：确认客户端收到数据包
                                com.magic.MagicMod.LOGGER.info("[Magic] 客户端收到魔法阵放置数据包，位置: {}, 所有者: {}", 
                                    payload.position(), ownerUuid);
                                // 激活魔法阵特效
                                MagicCircleRenderer.activateCircleForPlayer(
                                    ownerUuid, 
                                    payload.position()
                                );
                                com.magic.MagicMod.LOGGER.info("[Magic] 已调用activateCircleForPlayer");
                            }
                        });
                    }
                    // 检查是否是魔法阵移除数据包
                    else if (payload.id().equals(MagicNetworking.MAGIC_CIRCLE_REMOVED)) {
                        // 在客户端线程处理
                        context.client().execute(() -> {
                            if (context.client().player != null) {
                                // 使用数据包中的所有者UUID（如果存在），否则使用当前玩家UUID（向后兼容）
                                UUID ownerUuid = payload.ownerUuid() != null ? payload.ownerUuid() : context.client().player.getUuid();
                                // 移除魔法阵特效
                                MagicCircleRenderer.deactivateCircleForPlayer(
                                    ownerUuid
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
        
        // 注册魔法值同步数据包处理器
        try {
            ClientPlayNetworking.registerGlobalReceiver(ManaSyncPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        // 更新客户端魔法值
                        ManaManager.setMana(payload.currentMana(), payload.maxMana());
                    });
                });
            
            MagicMod.LOGGER.info("[Magic] 魔法值同步数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 魔法值同步数据包处理器注册失败", e);
        }

        try {
            PayloadTypeRegistry.playS2C().register(MagicLevelSyncPayload.ID, MagicLevelSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 MagicLevelSyncPayload 注册成功");
        } catch (IllegalArgumentException e) {
            MagicMod.LOGGER.info("[Magic] MagicLevelSyncPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 MagicLevelSyncPayload 注册失败", e);
        }

        try {
            ClientPlayNetworking.registerGlobalReceiver(MagicLevelSyncPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        ManaManager.setMagicLevel(payload.magicLevel());
                        ManaManager.setMagicUnlocked(payload.magicUnlocked());
                    });
                });

            MagicMod.LOGGER.info("[Magic] 魔法级同步数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 魔法级同步数据包处理器注册失败", e);
        }

        // 注册元素同步Payload
        try {
            PayloadTypeRegistry.playS2C().register(ElementSyncPayload.ID, ElementSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 ElementSyncPayload 注册成功");
        } catch (IllegalArgumentException e) {
            MagicMod.LOGGER.info("[Magic] ElementSyncPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 ElementSyncPayload 注册失败", e);
        }

        // 注册元素同步数据包处理器
        try {
            ClientPlayNetworking.registerGlobalReceiver(ElementSyncPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        ManaManager.setCurrentElement(payload.element());
                    });
                });
            MagicMod.LOGGER.info("[Magic] 元素同步数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 元素同步数据包处理器注册失败", e);
        }

        // Register dialogue start payload handler
        try {
            PayloadTypeRegistry.playS2C().register(com.magic.networking.DialogueStartPayload.ID, com.magic.networking.DialogueStartPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 DialogueStartPayload 注册成功");
        } catch (IllegalArgumentException e) {
            MagicMod.LOGGER.info("[Magic] DialogueStartPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 DialogueStartPayload 注册失败", e);
        }

        try {
            ClientPlayNetworking.registerGlobalReceiver(com.magic.networking.DialogueStartPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        // Start chat dialogue
                        com.magic.client.ChatDialogueManager.getInstance().startDialogue();
                    });
                });

            MagicMod.LOGGER.info("[Magic] 对话开始数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 对话开始数据包处理器注册失败", e);
        }

        // Register dialogue end payload
        try {
            PayloadTypeRegistry.playC2S().register(com.magic.networking.DialogueEndPayload.ID, com.magic.networking.DialogueEndPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] 客户端 DialogueEndPayload 注册成功");
        } catch (IllegalArgumentException e) {
            MagicMod.LOGGER.info("[Magic] DialogueEndPayload 已注册，忽略重复注册");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 客户端 DialogueEndPayload 注册失败", e);
        }
    }
}