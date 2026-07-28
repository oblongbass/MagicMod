package com.magic.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MagicCrystalItem extends Item {
    
    public MagicCrystalItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public Text getName(ItemStack stack) {
        return Text.literal("§b魔法水晶").formatted(Formatting.AQUA);
    }
}
