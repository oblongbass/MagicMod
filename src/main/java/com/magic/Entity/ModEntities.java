package com.magic.Entity;

import com.magic.MagicMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

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

    public static final EntityType<MagicZombieEntity> MAGIC_ZOMBIE = 
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MagicMod.MOD_ID, "magic_zombie"),
            EntityType.Builder.<MagicZombieEntity>create(MagicZombieEntity::new, SpawnGroup.MONSTER)
                .dimensions(0.6f, 1.95f) // 僵尸大小
                .maxTrackingRange(8) // 最大跟踪范围
                .trackingTickInterval(3) // 跟踪间隔
                .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MagicMod.MOD_ID, "magic_zombie")))
        );

    public static void initialize() {
        // 注册魔法僵尸的生成限制
        // 暂时注释掉，需要检查正确的API用法
        // SpawnRestriction.register(MAGIC_ZOMBIE, net.minecraft.entity.SpawnRestriction.Location.ON_GROUND, 
        //     Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MagicZombieEntity::canMobSpawn);
    }
}