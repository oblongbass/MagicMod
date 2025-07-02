package com.magic.event;

import com.magic.data.StateSaverAndLoader;
import com.magic.data.PlayerData;
import com.magic.item.ItemsRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerJoinHandler {
    
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                // 简单的首次加入检测：检查玩家是否已经有指引书
                boolean hasGuideBook = false;
                for (int i = 0; i < serverPlayer.getInventory().size(); i++) {
                    ItemStack stack = serverPlayer.getInventory().getStack(i);
                    if (stack.getItem() == ItemsRegistry.GUIDE_BOOK) {
                        hasGuideBook = true;
                        break;
                    }
                }
                
                // 如果没有指引书，给予一本
                if (!hasGuideBook) {
                    // 生成带内容的指引书
                    ItemStack guideBook = ((com.magic.item.GuideBookItem) com.magic.item.ItemsRegistry.GUIDE_BOOK).createBook(serverPlayer);
                    serverPlayer.giveItemStack(guideBook);
                    
                    // 发送欢迎消息
                    serverPlayer.sendMessage(Text.literal("欢迎来到魔法模组！").formatted(Formatting.GOLD), false);
                    serverPlayer.sendMessage(Text.literal("你获得了一本指引书，右键查看详细内容。"), false);
                }
            }
        });
    }
} 