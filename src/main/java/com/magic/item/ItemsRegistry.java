package com.magic.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import java.util.function.Function;

import static com.magic.MagicMod.MOD_ID;

public final class ItemsRegistry {
    public static Item MAGIC_STAR;
    public static Item MAGIC_WAND_LEVITATION;

    private ItemsRegistry() {}

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final Identifier itemId = Identifier.of(MOD_ID, path);
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, itemId);

        Item item = factory.apply(settings.registryKey(registryKey));
        return Registry.register(Registries.ITEM, registryKey, item);
    }

    public static void initialize() {
        MAGIC_STAR = register(
                "magic_star",
                MagicStarItem::new,
                new Item.Settings()
        );

        MAGIC_WAND_LEVITATION = register(
                "magic_wand_levitation",
                MagicWandItem::new,
                new Item.Settings().maxCount(1)
        );

        System.out.println("物品注册完成");
    }
}