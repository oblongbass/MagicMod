package com.magic.event;

import com.magic.data.SimplePlayerDataManager;
import com.magic.networking.MagicNetworking;
import com.magic.MagicMod;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LibrarianInteractionHandler {

    private static final Map<UUID, Boolean> dialogueInProgress = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> playerOriginalPositions = new ConcurrentHashMap<>();

    private static boolean isLibrarian(VillagerEntity villager) {
        try {
            Object profession = villager.getVillagerData().profession();
            String professionName = profession.toString();
            return professionName.toLowerCase().contains("bookseller") || professionName.toLowerCase().contains("librarian");
        } catch (Exception e) {
            MagicMod.LOGGER.warn("[Magic] 检测图书管理员失败: {}", e.getMessage());
            return false;
        }
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (entity instanceof VillagerEntity villager) {
                if (isDialogueInProgressForPlayer((ServerPlayerEntity) player)) {
                    player.sendMessage(Text.literal("§e你正在进行对话,请先完成当前对话。").formatted(Formatting.YELLOW), false);
                    return ActionResult.FAIL;
                }

                if (player.isSneaking() && hand == Hand.MAIN_HAND) {
                    if (!isLibrarian(villager)) {
                        return ActionResult.PASS;
                    }

                    boolean magicUnlocked = SimplePlayerDataManager.isMagicUnlocked((ServerPlayerEntity) player);

                    if (magicUnlocked) {
                        player.sendMessage(Text.literal("§a图书管理员: 你已经知道了具体玩法了,祝你一帆风顺!").formatted(Formatting.GOLD), false);
                        return ActionResult.PASS;
                    }

                    handleLibrarianInteraction(player, villager, world);
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void handleLibrarianInteraction(PlayerEntity player, VillagerEntity villager, World world) {
        UUID playerUuid = player.getUuid();

        playerOriginalPositions.put(playerUuid, player.getBlockPos());
        dialogueInProgress.put(playerUuid, true);

        villager.setAiDisabled(true);
        villager.setNoGravity(true);

        com.magic.networking.DialogueStartPayload payload = new com.magic.networking.DialogueStartPayload();
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send((ServerPlayerEntity) player, payload);
    }

    public static void unlockMagicAndUnlockVillager(ServerPlayerEntity player) {
        com.magic.data.PlayerData data = SimplePlayerDataManager.getPlayerData(player);
        data.unlockMagic();

        MagicNetworking.sendMagicLevelSync(player, data.getMagicLevel(), true);
        MagicNetworking.sendManaSync(player, data.getCurrentMana(), data.getMaxMana());
        MagicNetworking.sendElementSync(player, data.getCurrentElement());

        MagicMod.LOGGER.info("[Magic] 玩家 {} 通过图书管理员解锁了魔法技能!", player.getName().getString());

        clearDialogueState(player);
    }

    public static void clearDialogueState(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        dialogueInProgress.remove(playerUuid);
        playerOriginalPositions.remove(playerUuid);

        World world = player.getEntityWorld();
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            var villagers = serverWorld.getEntitiesByClass(
                VillagerEntity.class,
                player.getBoundingBox().expand(5),
                entity -> true
            );
            for (var villager : villagers) {
                villager.setAiDisabled(false);
                villager.setNoGravity(false);
            }
        }
    }

    public static boolean isDialogueInProgressForPlayer(ServerPlayerEntity player) {
        return dialogueInProgress.getOrDefault(player.getUuid(), false);
    }

    public static BlockPos getOriginalPosition(ServerPlayerEntity player) {
        return playerOriginalPositions.get(player.getUuid());
    }

    private static void sendTeachingMessages(PlayerEntity player) {
        player.sendMessage(Text.literal("§a图书管理员: 你好,年轻的冒险者!我是这里的图书管理员。").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§a图书管理员: 你知道 SimpleMagic 模组的玩法吗?").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("§e按 W 键选择'知道',按 S 键选择'不知道',然后按空格键确认").formatted(Formatting.YELLOW), false);
    }

    private static void sendAlreadyUnlockedMessage(PlayerEntity player) {
        player.sendMessage(Text.literal("§a图书管理员: 你已经掌握了魔法的基础知识,继续努力提升你的魔法水平吧!").formatted(Formatting.GOLD), false);
    }
}