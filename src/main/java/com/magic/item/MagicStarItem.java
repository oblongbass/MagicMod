package com.magic.item;

import net.minecraft.item.Item;

public class MagicStarItem extends Item {
    public MagicStarItem(Settings settings) {
        super(settings);
    }

    // 静态注册方法
    public static final Item MAGIC_STAR = ItemsRegistry.register(
            "magic_star",
            MagicStarItem::new,
            new Item.Settings()
    );
}