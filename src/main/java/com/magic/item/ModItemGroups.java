package com.magic.item;

import com.magic.MagicMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final RegistryKey<ItemGroup> MAGIC_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(MagicMod.MOD_ID, "magic_group")
    );

    public static final ItemGroup MAGIC_GROUP = FabricItemGroup.builder()
            .displayName(Text.literal("Magic Mod"))
            .icon(() -> new ItemStack(ItemsRegistry.MAGIC_STAR))
            .entries((context, entries) -> {
                // 基础材料
                entries.add(ItemsRegistry.MAGIC_STAR);
                entries.add(ItemsRegistry.MAGIC_CRYSTAL);

                // 魔法杖系列
                entries.add(ItemsRegistry.MAGIC_WAND_LEVITATION);
                entries.add(ItemsRegistry.MAGIC_WAND_DAMAGE);
                entries.add(ItemsRegistry.ELEMENT_WAND);

                // 工具类物品
                entries.add(ItemsRegistry.CRYSTAL_BALL);
                entries.add(ItemsRegistry.GUIDE_BOOK);
                
                // 法阵物品
                entries.add(ItemsRegistry.MAGIC_CIRCLE);
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