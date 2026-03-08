package com.magic.event;

import com.magic.item.ItemsRegistry;
import com.magic.data.SimplePlayerDataManager;
import com.magic.MagicMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerJoinHandler {

    // 使用内存存储跟踪哪些玩家已经获得过书
    private static final Set<UUID> playersWithBook = new HashSet<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity serverPlayer = handler.getPlayer();
            UUID playerId = serverPlayer.getUuid();

            // 恢复玩家的魔法阵数据（如果存在）
            try {
                SimplePlayerDataManager.restoreMagicCircleFromWorld(serverPlayer);
                MagicMod.LOGGER.info("[Magic] 玩家 {} 加入游戏，已尝试恢复魔法阵数据", serverPlayer.getName().getString());
            } catch (Exception e) {
                MagicMod.LOGGER.error("[Magic] 恢复魔法阵数据时发生错误", e);
            }

            // 只在玩家第一次加入时给予书
            if (!playersWithBook.contains(playerId)) {
                // 创建指引书
                ItemStack guideBook = createGuideBook(serverPlayer);
                boolean added = serverPlayer.giveItemStack(guideBook);

                if (added) {
                    // 标记玩家已经收到过指引书
                    playersWithBook.add(playerId);

                    // 发送欢迎消息
                    serverPlayer.sendMessage(Text.literal("欢迎来到魔法模组！").formatted(Formatting.GOLD), false);
                    serverPlayer.sendMessage(Text.literal("你获得了一本指引书，右键查看详细内容。").formatted(Formatting.YELLOW), false);

                    MagicMod.LOGGER.info("[Magic] 已给予玩家 {} 指引书", serverPlayer.getName().getString());
                } else {
                    MagicMod.LOGGER.warn("[Magic] 无法给予玩家 {} 指引书，背包可能已满", serverPlayer.getName().getString());
                }
            }
        });
    }

    // 创建指引书的辅助方法
    private static ItemStack createGuideBook(ServerPlayerEntity player) {
        // 直接创建 GuideBookItem 实例并调用其 createBook 方法
        com.magic.item.GuideBookItem guideBookItem = (com.magic.item.GuideBookItem) ItemsRegistry.GUIDE_BOOK;
        return guideBookItem.createBook(player);
    }
}