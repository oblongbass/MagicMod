package com.magic.item;

import net.minecraft.item.Item;
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
    public static Item MAGIC_WAND_DAMAGE;
    public static Item GUIDE_BOOK;
    public static Item CRYSTAL_BALL;
    public static Item MAGIC_CIRCLE;

    private ItemsRegistry() {}

    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }

    public static void initialize() {
        MAGIC_STAR = register("magic_star", MagicStarItem::new, new Item.Settings());

        // 浮空魔法棒 - 不可附魔
        MAGIC_WAND_LEVITATION = register("magic_wand_levitation", MagicWandItem::new,
                new Item.Settings().maxCount(1).maxDamage(256));

        // 伤害魔法棒 - 明确设置可附魔和耐久度
        MAGIC_WAND_DAMAGE = register("magic_wand_damage", DamageWandItem::new,
                new Item.Settings()
                        .maxCount(1)
                        .maxDamage(256)
                        .enchantable(15)  // 明确设置附魔能力
        );

        GUIDE_BOOK = register("guide_book", GuideBookItem::new, new Item.Settings().maxCount(1));
        CRYSTAL_BALL = register("crystal_ball", CrystalBallItem::new, new Item.Settings().maxCount(1));
        MAGIC_CIRCLE = register("magic_circle", MagicCircleItem::new, new Item.Settings().maxCount(1));

        System.out.println("物品注册完成");
    }
}