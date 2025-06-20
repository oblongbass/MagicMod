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

import static com.magic.item.MagicStarItem.MAGIC_STAR;
import static com.magic.item.MagicWandItem.MAGIC_WAND;

public class ModItemGroups {
    // 使用 RegistryKey 而不是 Identifier
    public static final RegistryKey<ItemGroup> MAGIC_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(MagicMod.MOD_ID, "magic_group")
    );

    public static final ItemGroup MAGIC_GROUP = FabricItemGroup.builder()
            .displayName(Text.literal("Magic Mod"))
            .icon(() -> new ItemStack(MAGIC_STAR))
            .entries((context, entries) -> {
                entries.add(MAGIC_STAR);
                entries.add(MAGIC_WAND);
            })
            .build();

    public static void register() {
        // 使用正确的注册方法
        Registry.register(
                Registries.ITEM_GROUP,
                MAGIC_GROUP_KEY,
                MAGIC_GROUP
        );
    }
}