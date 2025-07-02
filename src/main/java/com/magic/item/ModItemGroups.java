package com.magic.item;

import com.magic.MagicMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// 修正1: 导入正确的静态字段
import static com.magic.item.ItemsRegistry.MAGIC_STAR;
import static com.magic.item.ItemsRegistry.MAGIC_WAND_LEVITATION;
import static com.magic.item.ItemsRegistry.GUIDE_BOOK;
import static com.magic.item.ItemsRegistry.GROW_POTION;
import static com.magic.item.ItemsRegistry.SPLASH_GROW_POTION;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> MAGIC_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(MagicMod.MOD_ID, "magic_group")
    );

    public static final ItemGroup MAGIC_GROUP = FabricItemGroup.builder()
            .displayName(Text.literal("Magic Mod"))
            .icon(() -> new ItemStack(MAGIC_STAR))
            .entries((context, entries) -> {
                entries.add(MAGIC_STAR);
                entries.add(MAGIC_WAND_LEVITATION);
                entries.add(GUIDE_BOOK);

                ItemStack growPotionStack = new ItemStack(GROW_POTION);
                growPotionStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Registries.POTION.getEntry(MagicMod.GROW_POTION)));
                entries.add(growPotionStack);

                ItemStack splashGrowPotionStack = new ItemStack(SPLASH_GROW_POTION);
                splashGrowPotionStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Registries.POTION.getEntry(MagicMod.GROW_POTION)));
                entries.add(splashGrowPotionStack);
            })
            .build();

    public static void register() {
        Registry.register(
                Registries.ITEM_GROUP,
                MAGIC_GROUP_KEY,
                MAGIC_GROUP
        );
    }
}