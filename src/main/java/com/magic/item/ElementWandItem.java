package com.magic.item;

import com.magic.data.SimplePlayerDataManager;
import com.magic.networking.MagicNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ElementWandItem extends Item {
    private static final int SWITCH_COOLDOWN_TICKS = 40; // 2秒冷却
    private static final int SWITCH_MANA_COST = 5;

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
        ItemStack stack = user.getStackInHand(hand);

        if (!SimplePlayerDataManager.isMagicUnlocked(player)) {
            player.sendMessage(Text.literal("§c你还未解锁魔法！"), true);
            return ActionResult.FAIL;
        }

        int currentElement = SimplePlayerDataManager.getCurrentElement(player);
        int nextElement = (currentElement + 1) % 4;

        if (!player.isCreative() && !SimplePlayerDataManager.hasMana(player, SWITCH_MANA_COST)) {
            player.sendMessage(Text.literal("§c魔法值不足！需要 " + SWITCH_MANA_COST + " 点"), true);
            return ActionResult.FAIL;
        }

        // 消耗魔法值并设置新元素
        if (!player.isCreative()) {
            SimplePlayerDataManager.consumeMana(player, SWITCH_MANA_COST);
        }
        SimplePlayerDataManager.setCurrentElement(player, nextElement);

        // 同步到客户端
        MagicNetworking.sendElementSync(player, nextElement);
        MagicNetworking.sendManaSync(player,
                SimplePlayerDataManager.getCurrentMana(player),
                SimplePlayerDataManager.getMaxMana(player));

        player.sendMessage(Text.literal("§a元素切换: " + ELEMENT_NAMES[nextElement] + "§a模式"), true);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS,
                0.6f, 0.8f + nextElement * 0.2f);

        player.getItemCooldownManager().set(stack, SWITCH_COOLDOWN_TICKS);
        return ActionResult.SUCCESS;
    }
}
