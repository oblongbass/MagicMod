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
    private ItemsRegistry() {}

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final Identifier itemId = Identifier.of(MOD_ID, path);
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, itemId);

        // 创建物品实例时设置 registryKey
        Item item = factory.apply(settings.registryKey(registryKey));

        // 注册物品
        return Registry.register(Registries.ITEM, registryKey, item);
    }

    public static void initialize() {
        // 空方法，用于触发静态初始化
    }
}