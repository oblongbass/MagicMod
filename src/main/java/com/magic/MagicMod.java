package com.magic;

import com.magic.item.ItemsRegistry;
import com.magic.item.ModItemGroups;
import com.magic.item.MagicStarItem;
import com.magic.item.MagicWandItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicMod implements ModInitializer {
	public static final String MOD_ID = "magic-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 初始化物品注册
		ItemsRegistry.initialize();

		// 注册物品组
		ModItemGroups.register();

		// 添加测试命令 - 修正括号问题
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("givemagic")
					.executes(context -> {
						ServerCommandSource source = context.getSource();
						if (source.getPlayer() != null) {
							ServerPlayerEntity player = source.getPlayer();
							player.giveItemStack(new ItemStack(MagicStarItem.MAGIC_STAR));
							player.giveItemStack(new ItemStack(MagicWandItem.MAGIC_WAND));
							source.sendFeedback(() -> Text.literal("Gave magic items!"), false);
						}
						return 1;
					})); // 修正这里：添加了缺失的括号
		});

		LOGGER.info("Magic Mod initialized successfully!");

		// 添加调试信息
		LOGGER.info("Magic Star registered: {}", MagicStarItem.MAGIC_STAR);
		LOGGER.info("Magic Wand registered: {}", MagicWandItem.MAGIC_WAND);
	}
}