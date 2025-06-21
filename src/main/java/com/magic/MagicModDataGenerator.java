package com.magic;

import com.magic.Entity.ModEntities;
import com.magic.item.ItemsRegistry;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class MagicModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(MagicEntityLootTableProvider::new);
	}

	private static class MagicEntityLootTableProvider extends SimpleFabricLootTableProvider {
		public MagicEntityLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup, LootContextTypes.ENTITY);
		}

		@Override
		public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> consumer) {
			consumer.accept(ModEntities.MAGIC_ZOMBIE.getLootTableKey().get(), LootTable.builder()
					.pool(LootPool.builder()
							.rolls(ConstantLootNumberProvider.create(1))
							.with(ItemEntry.builder(ItemsRegistry.MAGIC_STAR))
							.conditionally(RandomChanceLootCondition.builder(0.35f))
					)
			);
		}
	}
}
