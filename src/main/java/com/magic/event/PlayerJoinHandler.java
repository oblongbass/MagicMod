package com.magic.event;

import com.magic.item.ItemsRegistry;
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

            // 只在玩家第一次加入时给予书
            if (!playersWithBook.contains(playerId)) {
                // 创建指引书
                ItemStack guideBook = createGuideBook();
                boolean added = serverPlayer.giveItemStack(guideBook);

                if (added) {
                    // 标记玩家已经收到过指引书
                    playersWithBook.add(playerId);

                    // 发送欢迎消息
                    serverPlayer.sendMessage(Text.literal("欢迎来到魔法模组！").formatted(Formatting.GOLD), false);
                    serverPlayer.sendMessage(Text.literal("你获得了一本指引书，右键查看详细内容。").formatted(Formatting.YELLOW), false);

                    System.out.println("[PlayerJoinHandler] 已给予玩家 " + serverPlayer.getName().getString() + " 指引书");
                } else {
                    System.out.println("[PlayerJoinHandler] 无法给予玩家 " + serverPlayer.getName().getString() + " 指引书，背包可能已满");
                }
            }
        });
    }

    // 创建指引书的辅助方法
    private static ItemStack createGuideBook() {
        // 直接创建 GuideBookItem 实例并调用其 createBook 方法
        com.magic.item.GuideBookItem guideBookItem = (com.magic.item.GuideBookItem) ItemsRegistry.GUIDE_BOOK;
        return guideBookItem.createBook(null);
    }
}