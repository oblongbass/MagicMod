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

public class MagicCommands {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("magic_teleport_to_circle")
            .executes(MagicCommands::teleportToMagicCircle)
        );
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
        
        // 检查是否在同一个维度
        String currentDimension = player.getWorld().getRegistryKey().getValue().toString();
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
            
            // 播放传送音效
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            return 1;
        }
        
        return 0;
    }
}