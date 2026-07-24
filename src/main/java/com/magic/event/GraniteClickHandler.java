package com.magic.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.MagicMod;
import com.magic.data.SimplePlayerDataManager;
import com.magic.data.PlayerData;
import com.magic.item.ItemsRegistry;
import com.magic.networking.MagicNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.UUID;

public class GraniteClickHandler {

    public static void register() {
        // 监听玩家使用方块事件(右键方块)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // 只在服务器端处理,避免客户端-服务器不一致
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();

            // 检查是否右键了平滑花岗岩
            if (block == Blocks.POLISHED_GRANITE) {
                // --- 魔法等级升级：手持魔法水晶右键魔法阵 ---
                if (player instanceof ServerPlayerEntity serverPlayer
                        && player.getStackInHand(hand).getItem() == ItemsRegistry.MAGIC_CRYSTAL) {
                    if (handleMagicUpgrade(serverPlayer, world, pos)) {
                        return ActionResult.SUCCESS;
                    }
                    return ActionResult.FAIL;
                }

                // 检查花岗岩上方是否有魔法阵物品实体
                MagicCircleItemEntity itemEntity = findMagicCircleItemAboveGranite(world, pos, player.getUuid());

                if (itemEntity != null) {
                    // 调用魔法阵物品实体的interact方法
                    ActionResult result = itemEntity.interact(player, hand);

                    // 如果交互成功,阻止默认行为
                    if (result.isAccepted()) {
                        return ActionResult.SUCCESS;
                    } else {
                        // 根据ActionResult类型显示具体原因
                        String reason = "未知错误";
                        if (result == ActionResult.FAIL) {
                            reason = "操作失败(可能是权限或背包空间问题)";
                        } else if (result == ActionResult.PASS) {
                            reason = "操作被跳过";
                        } else if (result == ActionResult.CONSUME) {
                            reason = "操作已消耗";
                        }
                        player.sendMessage(net.minecraft.text.Text.literal("§c[魔法阵] 收回失败,原因:" + reason).formatted(net.minecraft.util.Formatting.RED), false);
                        MagicMod.LOGGER.warn("[Magic] 魔法阵收回失败,ActionResult: {}, 玩家: {}", result, player.getName().getString());
                    }
                }
            }

            return ActionResult.PASS;
        });
    }

    /**
     * 在平滑花岗岩上方查找对应的魔法阵物品实体
     * @param world 世界
     * @param granitePos 花岗岩位置
     * @param playerUuid 玩家UUID(用于检查所有权)
     * @return 找到的魔法阵物品实体,如果未找到则返回null
     */
    private static MagicCircleItemEntity findMagicCircleItemAboveGranite(World world, BlockPos granitePos, java.util.UUID playerUuid) {
        try {
            MagicMod.LOGGER.info("[Magic] 查找花岗岩位置 {} 上方的魔法阵物品实体,玩家UUID: {}", granitePos, playerUuid);

            // 花岗岩上方1格(物品实体位置)
            BlockPos itemPos = granitePos.up();

            // 创建一个小区域搜索魔法阵物品实体
            Box searchBox = new Box(
                itemPos.getX() - 0.5, itemPos.getY() - 0.5, itemPos.getZ() - 0.5,
                itemPos.getX() + 1.5, itemPos.getY() + 1.5, itemPos.getZ() + 1.5
            );

            MagicMod.LOGGER.info("[Magic] 搜索区域: {}", searchBox);

            // 获取区域内的所有实体
            for (Entity entity : world.getOtherEntities(null, searchBox)) {
                if (entity instanceof MagicCircleItemEntity) {
                    MagicCircleItemEntity itemEntity = (MagicCircleItemEntity) entity;
                    UUID ownerUuid = itemEntity.getOwnerUuid();

                    MagicMod.LOGGER.info("[Magic] 找到魔法阵物品实体,所有者UUID: {}, 位置: {}", ownerUuid, entity.getBlockPos());

                    // 检查实体位置是否在花岗岩正上方
                    BlockPos entityPos = entity.getBlockPos();
                    if (entityPos.getX() == itemPos.getX() &&
                        entityPos.getZ() == itemPos.getZ() &&
                        Math.abs(entityPos.getY() - itemPos.getY()) <= 1) {
                        // 检查所有者UUID是否匹配
                                            if (ownerUuid == null) {
                                                MagicMod.LOGGER.warn("[Magic] 魔法阵物品实体所有者UUID为null,允许尝试恢复,实体UUID: {}", itemEntity.getUuid());
                                                // 所有者UUID为null,可能是NBT数据丢失,但仍允许尝试收回
                                                return itemEntity;
                                            } else if (ownerUuid.equals(playerUuid)) {
                                                MagicMod.LOGGER.info("[Magic] 找到匹配的魔法阵物品实体,所有者验证通过");
                                                return itemEntity;
                                            } else {
                                                MagicMod.LOGGER.warn("[Magic] 魔法阵物品实体所有者不匹配: {} != {}", ownerUuid, playerUuid);
                                            }                    }
                }
            }

            // 如果没有在正上方找到,扩大搜索范围
            Box largerBox = new Box(
                granitePos.getX() - 2, granitePos.getY(), granitePos.getZ() - 2,
                granitePos.getX() + 3, granitePos.getY() + 3, granitePos.getZ() + 3
            );

            MagicMod.LOGGER.info("[Magic] 扩大搜索区域: {}", largerBox);

            for (Entity entity : world.getOtherEntities(null, largerBox)) {
                if (entity instanceof MagicCircleItemEntity) {
                    MagicCircleItemEntity itemEntity = (MagicCircleItemEntity) entity;
                    UUID ownerUuid = itemEntity.getOwnerUuid();

                    MagicMod.LOGGER.info("[Magic] 在扩大区域找到魔法阵物品实体,所有者UUID: {}, 位置: {}", ownerUuid, entity.getBlockPos());

                    // 检查所有者UUID是否匹配
                    if (ownerUuid == null) {
                        MagicMod.LOGGER.warn("[Magic] 魔法阵物品实体所有者UUID为null(扩大区域),允许尝试恢复,实体UUID: {}", itemEntity.getUuid());
                        // 所有者UUID为null,可能是NBT数据丢失,但仍允许尝试收回
                        return itemEntity;
                    } else if (ownerUuid.equals(playerUuid)) {
                        MagicMod.LOGGER.info("[Magic] 找到匹配的魔法阵物品实体(扩大区域),所有者验证通过");
                        return itemEntity;
                    } else {
                        MagicMod.LOGGER.warn("[Magic] 魔法阵物品实体所有者不匹配(扩大区域): {} != {}", ownerUuid, playerUuid);
                    }
                }
            }

            MagicMod.LOGGER.warn("[Magic] 未找到匹配的魔法阵物品实体");
            return null;

        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 查找魔法阵物品实体异常", e);
            return null;
        }
    }

    /**
     * 处理魔法等级升级:玩家手持魔法水晶右键自己的魔法阵花岗岩
     */
    private static boolean handleMagicUpgrade(ServerPlayerEntity player, World world, BlockPos granitePos) {
        if (!SimplePlayerDataManager.isMagicUnlocked(player)) {
            player.sendMessage(Text.literal("§c你还没有解锁魔法!去找图书管理员吧。").formatted(Formatting.RED), false);
            return false;
        }
        if (!SimplePlayerDataManager.hasActiveMagicCircle(player)) {
            player.sendMessage(Text.literal("§c你还没有放置魔法阵!").formatted(Formatting.RED), false);
            return false;
        }
        BlockPos magicPos = SimplePlayerDataManager.getMagicCirclePos(player);
        if (magicPos == null || !magicPos.equals(granitePos)) {
            player.sendMessage(Text.literal("§c这不是你的魔法阵!").formatted(Formatting.RED), false);
            return false;
        }

        PlayerData data = SimplePlayerDataManager.getPlayerData(player);
        int currentLevel = data.getMagicLevel();
        if (currentLevel >= PlayerData.MAX_MAGIC_LEVEL) {
            player.sendMessage(Text.literal("§e魔法等级已达到最高(" + PlayerData.MAX_MAGIC_LEVEL + "级)!").formatted(Formatting.GOLD), false);
            return false;
        }

        int targetLevel = currentLevel + 1;
        int cost = PlayerData.getUpgradeCost(targetLevel);
        int crystalCount = countMagicCrystals(player);
        if (crystalCount < cost) {
            player.sendMessage(Text.literal("§c魔法水晶不足!升级到" + targetLevel + "级需要 " + cost + " 个魔法水晶(你有 " + crystalCount + " 个)").formatted(Formatting.RED), false);
            return false;
        }

        if (!player.isCreative() && !consumeMagicCrystals(player, cost)) {
            return false;
        }

        com.magic.data.MagicCircleSavedData savedData = com.magic.data.MagicCircleSavedData.getOrCreate(player.getEntityWorld());
        if (savedData == null) return false;
        PlayerData serverData = savedData.getPlayerData(player.getUuid());
        if (!serverData.tryUpgrade()) return false;
        savedData.setMagicCircle(player.getUuid(), serverData);
        savedData.markDirty();

        MagicNetworking.sendMagicLevelSync(player, serverData.getMagicLevel(), true);
        MagicNetworking.sendManaSync(player, serverData.getCurrentMana(), serverData.getMaxMana());

        int newMaxMana = serverData.getMaxMana();
        player.sendMessage(Text.literal("§a✨ 魔法等级提升至 " + targetLevel + " 级!最大魔法值: " + newMaxMana).formatted(Formatting.GREEN), false);

        world.playSound(null, granitePos.getX() + 0.5, granitePos.getY() + 0.5, granitePos.getZ() + 0.5,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 2.0f);
        world.playSound(null, granitePos.getX() + 0.5, granitePos.getY() + 0.5, granitePos.getZ() + 0.5,
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.5f);

        if (world instanceof net.minecraft.server.world.ServerWorld sw) {
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD,
                    granitePos.getX() + 0.5, granitePos.getY() + 1.5, granitePos.getZ() + 0.5,
                    50, 0.5, 0.5, 0.5, 0.1);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.ENCHANT,
                    granitePos.getX() + 0.5, granitePos.getY() + 1.5, granitePos.getZ() + 0.5,
                    30, 0.5, 0.5, 0.5, 0.2);
        }
        return true;
    }

    private static int countMagicCrystals(ServerPlayerEntity player) {
        int count = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (stack.getItem() == ItemsRegistry.MAGIC_CRYSTAL) count += stack.getCount();
        }
        return count;
    }

    private static boolean consumeMagicCrystals(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        var inv = player.getInventory();
        for (int i = 0; i < inv.size() && remaining > 0; i++) {
            var stack = inv.getStack(i);
            if (stack.getItem() == ItemsRegistry.MAGIC_CRYSTAL) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
        return remaining == 0;
    }
}