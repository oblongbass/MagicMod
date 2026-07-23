package com.magic.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import com.magic.MagicMod;
import com.magic.event.LibrarianInteractionHandler;
import java.util.UUID;

public class MagicNetworking {
    public static final Identifier MAGIC_CIRCLE_PLACED = Identifier.of("magic-mod", "magic_circle_placed");
    public static final Identifier MAGIC_CIRCLE_REMOVED = Identifier.of("magic-mod", "magic_circle_removed");
    public static final Identifier MANA_SYNC = Identifier.of("magic-mod", "mana_sync");
    public static final Identifier MAGIC_LEVEL_SYNC = Identifier.of("magic-mod", "magic_level_sync");
    public static final Identifier DIALOGUE_START = Identifier.of("magic-mod", "dialogue_start");

    public static void initialize() {
        MagicMod.LOGGER.info("[Magic] 初始化网络数据包");

        try {
            PayloadTypeRegistry.playS2C().register(SimpleMagicPayload.ID, SimpleMagicPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] SimpleMagicPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] SimpleMagicPayload 注册失败", e);
        }

        try {
            PayloadTypeRegistry.playS2C().register(ManaSyncPayload.ID, ManaSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] ManaSyncPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] ManaSyncPayload 注册失败", e);
        }

        try {
            PayloadTypeRegistry.playS2C().register(MagicLevelSyncPayload.ID, MagicLevelSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] MagicLevelSyncPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] MagicLevelSyncPayload 注册失败", e);
        }

        try {
            PayloadTypeRegistry.playS2C().register(ElementSyncPayload.ID, ElementSyncPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] ElementSyncPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] ElementSyncPayload 注册失败", e);
        }

        try {
            PayloadTypeRegistry.playS2C().register(com.magic.networking.DialogueStartPayload.ID, com.magic.networking.DialogueStartPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] DialogueStartPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] DialogueStartPayload 注册失败", e);
        }

        try {
            PayloadTypeRegistry.playC2S().register(com.magic.networking.DialogueEndPayload.ID, com.magic.networking.DialogueEndPayload.CODEC);
            MagicMod.LOGGER.info("[Magic] DialogueEndPayload 注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] DialogueEndPayload 注册失败", e);
        }

        // Register dialogue end payload handler
        try {
            ServerPlayNetworking.registerGlobalReceiver(com.magic.networking.DialogueEndPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        ServerPlayerEntity player = context.player();
                        if (player != null) {
                            if (payload.shouldUnlockMagic()) {
                                // 解锁魔法并解锁村民
                                LibrarianInteractionHandler.unlockMagicAndUnlockVillager(player);
                            } else {
                                // 只解锁村民（选择"知道"的情况）
                                LibrarianInteractionHandler.clearDialogueState(player);

                                World world = player.getEntityWorld();
                                if (world instanceof ServerWorld serverWorld) {
                                    var villagers = serverWorld.getEntitiesByClass(
                                        net.minecraft.entity.passive.VillagerEntity.class,
                                        player.getBoundingBox().expand(5),
                                        entity -> true
                                    );
                                    for (var villager : villagers) {
                                        villager.setAiDisabled(false);
                                        villager.setNoGravity(false);
                                    }
                                    MagicMod.LOGGER.info("[Magic] 对话结束（选择知道），解锁附近 {} 个村民", villagers.size());
                                }
                            }
                        }
                    });
                });
            MagicMod.LOGGER.info("[Magic] 对话结束数据包处理器注册成功");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 对话结束数据包处理器注册失败", e);
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

    public static void sendMagicLevelSync(ServerPlayerEntity player, int magicLevel, boolean magicUnlocked) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                MagicLevelSyncPayload payload = new MagicLevelSyncPayload(magicLevel, magicUnlocked);
                ServerPlayNetworking.send(player, payload);
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送魔法级同步数据包失败", e);
            }
        }
    }

    public static void sendElementSync(ServerPlayerEntity player, int element) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                ElementSyncPayload payload = new ElementSyncPayload(element);
                ServerPlayNetworking.send(player, payload);
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送元素同步数据包失败", e);
            }
        }
    }

    public static void sendDialogueStart(ServerPlayerEntity player) {
        if (player != null && !player.getEntityWorld().isClient()) {
            try {
                DialogueStartPayload payload = new DialogueStartPayload();
                ServerPlayNetworking.send(player, payload);
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 发送对话开始数据包失败", e);
            }
        }
    }
}
