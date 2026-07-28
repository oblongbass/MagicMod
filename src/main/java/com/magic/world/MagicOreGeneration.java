package com.magic.world;

import com.magic.MagicMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class MagicOreGeneration {
    
    public static final RegistryKey<PlacedFeature> ORE_MAGIC_PLACED_KEY = RegistryKey.of(
            RegistryKeys.PLACED_FEATURE,
            Identifier.of(MagicMod.MOD_ID, "ore_magic")
    );
    
    public static void registerOres() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                ORE_MAGIC_PLACED_KEY
        );
        
        MagicMod.LOGGER.info("Magic ore generation registered!");
    }
}
