package com.magic.item;

import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.item.Items;
import java.util.function.Function;

import static com.magic.MagicMod.MOD_ID;

public final class ItemsRegistry {
    public static Item MAGIC_STAR;
    public static Item MAGIC_WAND_LEVITATION;
    public static Item GUIDE_BOOK;
    public static Item GROW_POTION;
    public static Item SPLASH_GROW_POTION;

    private ItemsRegistry() {}

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }

    public static void initialize() {
        MAGIC_STAR = register("magic_star", MagicStarItem::new, new Item.Settings());
        MAGIC_WAND_LEVITATION = register("magic_wand_levitation", MagicWandItem::new, new Item.Settings().maxCount(1));
        GUIDE_BOOK = register("guide_book", GuideBookItem::new, new Item.Settings().maxCount(1));
        GROW_POTION = register("grow_potion", PotionItem::new, new Item.Settings().maxCount(16));
        SPLASH_GROW_POTION = register("splash_grow_potion", SplashPotionItem::new, new Item.Settings().maxCount(16));
        System.out.println("物品注册完成");
    }
}