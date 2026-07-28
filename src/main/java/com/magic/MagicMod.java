package com.magic;

import com.magic.Entity.ModEntities;
import com.magic.enchantment.ModEnchantmentEffects;
import com.magic.event.*;
import com.magic.item.ItemsRegistry;
import com.magic.item.MagicWandItem;
import com.magic.item.CrystalBallItem;
import com.magic.item.ModItemGroups;
import com.magic.block.ModBlocks;
import com.magic.command.MagicCommands;
import com.magic.data.SimplePlayerDataManager;
import com.magic.networking.MagicNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magic-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ItemsRegistry.initialize();
		ModBlocks.initialize();
		ModEntities.initialize();
		ModItemGroups.register();

		// 注册自定义附魔效果类型
		ModEnchantmentEffects.register();

		// 注册魔法杖粒子效果更新
		ServerTickEvents.END_SERVER_TICK.register(server -> MagicWandItem.tickParticleEffects());

		// 注册水晶球粒子效果更新
		ServerTickEvents.END_SERVER_TICK.register(server -> CrystalBallItem.tickParticleEffects());
		
		// 注册魔法值恢复更新（每秒恢复1点）
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				SimplePlayerDataManager.tickManaRegen(player);
			}
		});

		// 注册玩家加入事件处理器
		PlayerJoinHandler.register();

		// 注册实体移除事件处理器
		EntityRemovalHandler.register();

		// 注册魔法僵尸生成系统
		MagicZombieSpawn.register();

		// 注册附魔书激活处理器
		BookActivationHandler.register();

		// 注册物品投掷监听器
		ItemThrowListener.register();

		// 注册命令
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			MagicCommands.register(dispatcher);
		});

		// 注册实体交互处理器（用于魔法阵物品实体右键收回）
		EntityInteractionHandler.register();

		// 注册玩家右键检测处理器（通过射线检测寻找魔法阵物品实体）
		PlayerRightClickHandler.register();

		// 注册花岗岩右键检测处理器（检测右键平滑花岗岩并收回魔法阵）
		GraniteClickHandler.register();

		// 注册花岗岩保护处理器（防止玩家挖掘魔法阵花岗岩）
		GraniteProtectionHandler.register();

		// 注册图书管理员交互处理器（解锁魔法技能）
		LibrarianInteractionHandler.register();

		// 注册网络数据包
		MagicNetworking.initialize();

		// 注册魔法矿石生成
		com.magic.world.MagicOreGeneration.registerOres();

		// 注册元素攻击处理器
		ElementAttackHandler.register();

		// 玩家断开连接时清除元素激活状态
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (handler.player instanceof ServerPlayerEntity player) {
				ElementAttackHandler.setActivated(player, false);
			}
		});

		// 元素攻击处理 + 伤害数字动画 + 土粒子（每 tick）
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ElementAttackHandler.tickDamageNumbers();
			var world = server.getOverworld();
			if (world != null) ElementAttackHandler.tickEarthParticles(world);
			// 每5秒清理过期元素标记
			if (server.getTicks() % 100 == 0) {
				for (var w : server.getWorlds()) {
					ElementAttackHandler.cleanExpired(w);
				}
			}
		});

		LOGGER.info("Magic Mod 初始化成功!");
	}
}