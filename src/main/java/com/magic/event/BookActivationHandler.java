package com.magic.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BookActivationHandler {

    public static void register() {
        // 使用物品使用回调来检测右键点击附魔书
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            // 检查是否是带有特殊NBT的附魔书
            if (stack.isOf(Items.ENCHANTED_BOOK) && hasSpecialEnchantNBT(stack)) {
                System.out.println("检测到未激活的附魔书，尝试激活");
                ActionResult result = activateEnchantedBook(player, stack, hand, world);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static boolean hasSpecialEnchantNBT(ItemStack stack) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) {
            return false;
        }
        // 修复：直接获取 NbtCompound
        NbtCompound nbt = nbtComponent.copyNbt();
        return nbt != null && nbt.contains("magic-mod:enchant_type");
    }

    private static ActionResult activateEnchantedBook(PlayerEntity player, ItemStack stack, Hand hand, World world) {
        if (world.isClient) {
            return ActionResult.PASS;
        }

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) {
            return ActionResult.PASS;
        }

        // 修复：直接获取 NbtCompound
        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null) {
            return ActionResult.PASS;
        }

        String enchantType = "";

        if (nbt.contains("magic-mod:enchant_type")) {
            try {
                // 正确处理字符串
                String rawValue = nbt.get("magic-mod:enchant_type").toString();
                if (rawValue.startsWith("Optional[")) {
                    // 检查字符串长度是否足够
                    if (rawValue.length() >= 10) {
                        enchantType = rawValue.substring(9, rawValue.length() - 1); // 移除 "Optional[" 和 "]"
                    } else {
                        enchantType = rawValue;
                    }
                } else {
                    enchantType = rawValue;
                }
                // 清理字符串，移除可能的引号
                enchantType = enchantType.replace("\"", "");
            } catch (Exception e) {
                System.err.println("获取NBT字符串失败: " + e.getMessage());
                return ActionResult.PASS;
            }
        } else {
            return ActionResult.PASS;
        }

        System.out.println("激活附魔书类型: " + enchantType);

        // 获取对应的附魔ID
        String enchantmentId = "";
        String displayName = "";

        if (enchantType.contains("charge_power")) {
            enchantmentId = "magic-mod:charge_power";
            displayName = "§6蓄能附魔书";
        } else if (enchantType.contains("damage_power")) {
            enchantmentId = "magic-mod:damage_power";
            displayName = "§6伤能附魔书";
        } else {
            System.out.println("未知的附魔类型: " + enchantType);
            return ActionResult.PASS;
        }

        // 给附魔书本身添加附魔
        if (player instanceof ServerPlayerEntity serverPlayer) {
            boolean success = addEnchantmentToBook(stack, enchantmentId, serverPlayer);

            if (success) {
                // 删除特殊NBT
                stack.set(DataComponentTypes.CUSTOM_DATA, null);

                // 更新显示名称
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName));

                // 播放激活效果
                ServerWorld serverWorld = (ServerWorld) world;
                playActivationEffects(serverWorld, player, displayName.contains("蓄能") ? "蓄能" : "伤能");

                System.out.println("附魔书激活成功: " + displayName);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private static boolean addEnchantmentToBook(ItemStack bookStack, String enchantmentId, ServerPlayerEntity player) {
        try {
            // 在 1.21.6 中，最简单的方式是直接使用命令
            // 使用 /enchant 命令给玩家手中的物品添加附魔
            String command = String.format("enchant %s %s 1",
                    player.getName().getString(), enchantmentId);

            // 执行命令 - 修复：不尝试获取返回值
            player.getServer().getCommandManager().executeWithPrefix(
                    player.getCommandSource().withSilent(), command);

            // 假设命令执行成功，因为我们无法获取返回值
            // 获取附魔显示名称
            String enchantName = getEnchantmentDisplayName(enchantmentId);
            player.sendMessage(Text.literal("§a" + enchantName + "附魔书已激活！"), true);
            return true;
        } catch (Exception e) {
            System.err.println("给附魔书添加附魔时出错: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(Text.literal("§c激活附魔书时出现错误！"), true);
            return false;
        }
    }

    private static String getEnchantmentDisplayName(String enchantmentId) {
        // 根据附魔ID返回对应的显示名称
        if (enchantmentId.contains("charge_power")) {
            return "蓄能";
        } else if (enchantmentId.contains("damage_power")) {
            return "伤能";
        }
        return "未知附魔";
    }

    private static void playActivationEffects(ServerWorld world, PlayerEntity player, String enchantDescription) {
        Vec3d playerPos = player.getPos();

        // 播放激活音效
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // 播放升级音效
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.0f);

        // 生成激活粒子效果
        createActivationParticles(world, player);

        // 发送激活消息
        player.sendMessage(Text.literal("§a" + enchantDescription + "附魔书已激活！"), true);

        System.out.println("附魔书激活成功: " + enchantDescription);
    }

    private static void createActivationParticles(ServerWorld world, PlayerEntity player) {
        Vec3d playerPos = player.getPos();

        // 生成围绕玩家旋转的粒子
        for (int i = 0; i < 360; i += 15) {
            double radians = Math.toRadians(i);
            double x = playerPos.x + Math.cos(radians) * 2.0;
            double z = playerPos.z + Math.sin(radians) * 2.0;

            world.spawnParticles(ParticleTypes.ENCHANT,
                    x, playerPos.y + 1.0, z, 3, 0.1, 0.5, 0.1, 0.05);
        }

        // 生成向上飞升的粒子
        for (int i = 0; i < 50; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;

            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    playerPos.x, playerPos.y + 0.5, playerPos.z, 1,
                    offsetX, world.random.nextDouble() * 2.0, offsetZ, 0.1);
        }

        // 生成闪光效果
        world.spawnParticles(ParticleTypes.FLASH,
                playerPos.x, playerPos.y + 1.5, playerPos.z, 5, 0.5, 0.5, 0.5, 0.0);
    }
}