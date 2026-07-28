package com.magic.item;

import com.magic.data.SimplePlayerDataManager;
import com.magic.event.ElementAttackHandler;
import com.magic.networking.MagicNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ElementWandItem extends Item {
    // 元素类型常量
    public static final int ELEMENT_FIRE = 0;
    public static final int ELEMENT_WATER = 1;
    public static final int ELEMENT_WIND = 2;
    public static final int ELEMENT_EARTH = 3;
    public static final String[] ELEMENT_NAMES = {"§c火", "§b水", "§7风", "§6土"};

    public ElementWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.SUCCESS;
        ServerPlayerEntity player = (ServerPlayerEntity) user;

        if (!SimplePlayerDataManager.isMagicUnlocked(player)) {
            player.sendMessage(Text.literal("§c你还未解锁魔法！"), true);
            return ActionResult.FAIL;
        }

        // 消耗5魔法值激活元素能力
        if (!player.isCreative() && !SimplePlayerDataManager.hasMana(player, 5)) {
            player.sendMessage(Text.literal("§c魔法值不足！需要 5 点"), true);
            return ActionResult.FAIL;
        }
        if (!player.isCreative()) {
            SimplePlayerDataManager.consumeMana(player, 5);
        }
        ElementAttackHandler.setActivated(player, true);

        // 同步法力值
        MagicNetworking.sendManaSync(player,
                SimplePlayerDataManager.getCurrentMana(player),
                SimplePlayerDataManager.getMaxMana(player));

        int currentElement = SimplePlayerDataManager.getCurrentElement(player);
        player.sendMessage(Text.literal("§a✦ 元素已激活！当前: " + ELEMENT_NAMES[currentElement] + " §7[Z水 X火 V土 R风]"), true);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS,
                0.6f, 0.8f + currentElement * 0.2f);

        return ActionResult.SUCCESS;
    }
}
