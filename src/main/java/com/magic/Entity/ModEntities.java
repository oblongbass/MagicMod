package com.magic.Entity;

import com.magic.MagicMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<MagicCircleItemEntity> MAGIC_CIRCLE_ITEM_ENTITY = 
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MagicMod.MOD_ID, "magic_circle_item_entity"),
            EntityType.Builder.<MagicCircleItemEntity>create(MagicCircleItemEntity::new, SpawnGroup.MISC)
                .dimensions(0.25f, 0.25f) // 物品实体大小
                .maxTrackingRange(10) // 最大跟踪范围
                .trackingTickInterval(20) // 跟踪间隔
                .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MagicMod.MOD_ID, "magic_circle_item_entity")))
        );

    public static void initialize() {
        // 实体已通过Registry.register注册
    }
}