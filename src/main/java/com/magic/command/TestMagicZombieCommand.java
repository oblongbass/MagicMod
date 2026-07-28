package com.magic.command;

import com.magic.Entity.ModEntities;
import com.magic.Entity.MagicZombieEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TestMagicZombieCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("testmagiczombie")
            .executes(TestMagicZombieCommand::spawnMagicZombie)
        );
    }

    private static int spawnMagicZombie(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        
        if (player == null) {
            source.sendError(Text.literal("该命令只能由玩家执行"));
            return 0;
        }

        try {
            // 在玩家面前生成魔法僵尸
            BlockPos spawnPos = player.getBlockPos().add(0, 0, 3);
            
            // 创建魔法僵尸实体
            MagicZombieEntity magicZombie = ModEntities.MAGIC_ZOMBIE.create(player.getEntityWorld(), net.minecraft.entity.SpawnReason.COMMAND);
            
            if (magicZombie != null) {
                magicZombie.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    player.getYaw(),
                    0.0f
                );
                
                // 初始化实体
                magicZombie.initialize(player.getEntityWorld(), player.getEntityWorld().getLocalDifficulty(spawnPos), 
                    net.minecraft.entity.SpawnReason.COMMAND, null);
                
                // 添加到世界
                player.getEntityWorld().spawnEntity(magicZombie);
                
                source.sendFeedback(() -> Text.literal("成功生成魔法僵尸！"), true);
                return 1;
            } else {
                source.sendError(Text.literal("无法创建魔法僵尸实体"));
                return 0;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("生成魔法僵尸时出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}