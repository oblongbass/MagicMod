package com.magic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import com.magic.data.SimplePlayerDataManager;
import com.magic.MagicMod;

public class MagicCommands {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("magic_teleport_to_circle")
            .executes(MagicCommands::teleportToMagicCircle)
        );
        
        // 注册测试魔法僵尸命令
        TestMagicZombieCommand.register(dispatcher, null, null);
    }
    
    private static int teleportToMagicCircle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("只有玩家可以使用这个命令！"));
            return 0;
        }
        
        ServerPlayerEntity player = source.getPlayer();
        
        // 检查玩家是否有激活的魔法阵
        if (!SimplePlayerDataManager.hasActiveMagicCircle(player)) {
            player.sendMessage(Text.literal("§c你还没有放置魔法阵！").formatted(Formatting.RED), false);
            return 0;
        }
        
        // 检查是否有足够的魔法值（需要10点）
        int manaCost = 10;
        if (!player.isCreative() && !SimplePlayerDataManager.hasMana(player, manaCost)) {
            int currentMana = SimplePlayerDataManager.getCurrentMana(player);
            player.sendMessage(Text.literal("§c魔法值不足！需要 " + manaCost + " 点，当前 " + currentMana + " 点").formatted(Formatting.RED), false);
            return 0;
        }
        
        // 检查是否在同一个维度
        String currentDimension = player.getEntityWorld().getRegistryKey().getValue().toString();
        String magicCircleDimension = SimplePlayerDataManager.getMagicCircleDimension(player);
        if (!currentDimension.equals(magicCircleDimension)) {
            player.sendMessage(Text.literal("§c你需要在魔法阵所在的维度才能传送！").formatted(Formatting.RED), false);
        return 0;
    }

        // 执行传送
        BlockPos targetPos = SimplePlayerDataManager.getMagicCirclePos(player);
        if (targetPos != null) {
            // 传送到魔法阵中心上方1格，避免卡在方块里
            player.teleport(
                targetPos.getX() + 0.5,
                targetPos.getY() + 1.0,
                targetPos.getZ() + 0.5,
                true
            );

            // 发送传送成功消息
            player.sendMessage(Text.literal("§a已传送到魔法阵中心！").formatted(Formatting.GREEN), false);

            // 消耗魔法值（10点）
            if (!player.isCreative()) {
                MagicMod.LOGGER.info("[Magic] 玩家 {} 尝试消耗10点魔法值进行传送", player.getName().getString());
                boolean success = SimplePlayerDataManager.consumeMana(player, 10);
                MagicMod.LOGGER.info("[Magic] 消耗结果: {}", success);
                // 同步魔法值到客户端
                com.magic.networking.MagicNetworking.sendManaSync(
                    player, 
                    SimplePlayerDataManager.getCurrentMana(player),
                    SimplePlayerDataManager.getMaxMana(player)
                );
            }

            // 播放传送音效
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

            return 1;
}

        return 0;
    }
}
