package com.magic.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.magic.MagicMod;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;

public class GuideBookItem extends Item {
    public GuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        MagicMod.LOGGER.info("[GuideBook] use() called, world.isClient: {}, hand: {}", world.isClient(), hand);
        
        // 只在客户端打开GUI
        if (world.isClient()) {
            openGuideBookScreen();
        }
        
        // 服务端返回成功结果
        return ActionResult.SUCCESS;
    }
    
    @Environment(EnvType.CLIENT)
    private void openGuideBookScreen() {
        try {
            // 使用完全限定名避免服务端导入问题
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            client.setScreen(new com.magic.client.screen.GuideBookScreen());
            MagicMod.LOGGER.info("[GuideBook] Opened custom guide book GUI");
        } catch (Exception e) {
            MagicMod.LOGGER.error("[GuideBook] Failed to open guide book GUI", e);
        }
    }

    public ItemStack createBook(PlayerEntity player) {
        // 创建一个简单的引导书物品
        return new ItemStack(this);
    }
}