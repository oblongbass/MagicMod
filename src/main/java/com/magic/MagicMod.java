package com.magic;

import com.magic.Entity.ModEntities;
import com.magic.effect.GrowEffect;
import com.magic.event.PlayerJoinHandler;
import com.magic.event.EntityRemovalHandler;
import com.magic.item.ItemsRegistry;
import com.magic.item.MagicWandItem;
import com.magic.item.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magic-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// 注册状态效果和药水
	public static final StatusEffect GROW_EFFECT = Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "grow"), new GrowEffect());
	public static final Potion GROW_POTION = Registry.register(
			Registries.POTION,
			Identifier.of(MOD_ID, "grow"),
			new Potion("grow", new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(GROW_EFFECT), 600, 0, false, false))
	);

	@Override
	public void onInitialize() {
		ItemsRegistry.initialize();
		ModEntities.initialize();
		ModItemGroups.register();
		ServerTickEvents.END_SERVER_TICK.register(server -> MagicWandItem.tickParticleEffects());
		
		// 注册玩家加入事件处理器
		PlayerJoinHandler.register();
		
		// 注册实体移除事件处理器
		EntityRemovalHandler.register();

		// 注册GrowEffect和GrowPotion
		// Registry.register(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, "grow"), GROW_EFFECT);
		// Registry.register(Registries.POTION, Identifier.of(MOD_ID, "grow"), GROW_POTION);

		LOGGER.info("Magic Mod 初始化成功!");
	}
}