package com.magic;

import com.magic.Entity.ModEntities;
import com.magic.event.PlayerJoinHandler;
import com.magic.event.EntityRemovalHandler;
import com.magic.event.MagicZombieSpawn;
import com.magic.item.ItemsRegistry;
import com.magic.item.MagicWandItem;
import com.magic.item.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magic-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

		// 注册魔法僵尸生成系统
		MagicZombieSpawn.register();

		LOGGER.info("Magic Mod 初始化成功!");
	}
}