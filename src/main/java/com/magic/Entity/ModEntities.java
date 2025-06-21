package com.magic.Entity;

import com.magic.Entity.MagicZombieEntity;
import com.magic.MagicMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    private static final RegistryKey<EntityType<?>> MAGIC_ZOMBIE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MagicMod.MOD_ID, "magic_zombie"));

    public static final EntityType<MagicZombieEntity> MAGIC_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            MAGIC_ZOMBIE_KEY,
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MagicZombieEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build(MAGIC_ZOMBIE_KEY)
    );

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(MAGIC_ZOMBIE, MagicZombieEntity.createMobAttributes());
    }
}