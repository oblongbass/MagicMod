package com.magic.event;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.*;

import com.magic.MagicMod;
import com.magic.item.ItemsRegistry;
import com.magic.data.SimplePlayerDataManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemThrowListener {

    private static final Map<BlockPos, List<ItemEntity>> itemsAtPositions = new HashMap<>();
    private static final Set<BlockPos> processedPositions = new HashSet<>();

    public static void register() {
        // 监听实体加载（物品被扔出）
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ItemEntity itemEntity && world instanceof ServerWorld serverWorld) {
                onItemThrown(itemEntity, serverWorld);
            }
        });

        // 每tick检查物品组合
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            checkItemCombinations(server);

            // 每100tick清理一次（5秒）
            if (server.getTicks() % 100 == 0) {
                cleanup();
            }
        });

        System.out.println("注册物品投掷监听器");
    }

    private static void onItemThrown(ItemEntity itemEntity, ServerWorld world) {
        BlockPos pos = itemEntity.getBlockPos();
        ItemStack stack = itemEntity.getStack();

        // 关注：附魔书合成材料 + 元素法杖合成材料
        boolean isBookMaterial = stack.isOf(Items.BOOK) || stack.isOf(Items.REDSTONE) || stack.isOf(Items.POTION);
        boolean isElementMaterial = isElementInfusionItem(stack);
        if (!isBookMaterial && !isElementMaterial) {
            return;
        }

        System.out.println("检测到投掷物品: " + stack.getItem().getName().getString() + " 在位置: " + pos);

        // 记录物品位置
        itemsAtPositions.computeIfAbsent(pos, k -> new ArrayList<>()).add(itemEntity);

        // 标记该位置需要检查
        processedPositions.add(pos);
    }

    private static void checkItemCombinations(net.minecraft.server.MinecraftServer server) {
        if (processedPositions.isEmpty()) {
            return;
        }

        // 复制位置列表以避免并发修改
        Set<BlockPos> positionsToCheck = new HashSet<>(processedPositions);
        processedPositions.clear();

        for (BlockPos pos : positionsToCheck) {
            List<ItemEntity> items = itemsAtPositions.get(pos);
            if (items != null) {
                System.out.println("检查位置 " + pos + " 的物品组合，共有 " + items.size() + " 个物品");
            }

            for (ServerWorld world : server.getWorlds()) {
                checkPosition(world, pos);
            }
        }
    }

    private static void checkPosition(ServerWorld world, BlockPos pos) {
        // 元素灌注只在主世界维度检查
        if (world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) return;

        List<ItemEntity> items = itemsAtPositions.get(pos);
        if (items == null || items.size() < 2) return;

        // 清理无效的物品实体
        items.removeIf(item -> item.isRemoved() || !item.isAlive());
        if (items.size() < 2) return;

        System.out.println("在位置 " + pos + " 检查 " + items.size() + " 个物品的组合");

        // 如果全是元素灌注材料，直接跳到灌注检查，跳过附魔书合成
        if (allElementItems(items)) {
            checkAndCreateElementWand(world, items);
            return;
        }

        // 检查物品组合 - 使用列表副本避免并发修改
        List<ItemEntity> itemsCopy = new ArrayList<>(items);
        for (int i = 0; i < itemsCopy.size(); i++) {
            for (int j = i + 1; j < itemsCopy.size(); j++) {
                ItemEntity item1 = itemsCopy.get(i);
                ItemEntity item2 = itemsCopy.get(j);

                if (checkAndCreateEnchantedBook(world, item1, item2)) {
                    item1.discard();
                    item2.discard();
                    items.remove(item1);
                    items.remove(item2);
                    return;
                }
            }
        }

        // 检查元素法杖合成
        checkAndCreateElementWand(world, items);
    }

    private static boolean checkAndCreateEnchantedBook(ServerWorld world, ItemEntity item1, ItemEntity item2) {
        ItemStack stack1 = item1.getStack();
        ItemStack stack2 = item2.getStack();

        System.out.println("检查物品组合: " +
                stack1.getItem().getName().getString() + " + " +
                stack2.getItem().getName().getString());

        // 检查书 + 红石 -> 蓄能附魔书（无属性）
        if ((stack1.isOf(Items.BOOK) && stack2.isOf(Items.REDSTONE)) ||
                (stack1.isOf(Items.REDSTONE) && stack2.isOf(Items.BOOK))) {
            System.out.println("检测到书+红石组合，生成蓄能附魔书（无属性）");
            spawnSpecialBook(world, item1.getLerpedPos(1.0F), "charge_power");
            return true;
        }

        // 检查书 + 力量药水 -> 伤能附魔书（无属性）
        if ((stack1.isOf(Items.BOOK) && isStrengthPotion(stack2)) ||
                (stack2.isOf(Items.BOOK) && isStrengthPotion(stack1))) {
            System.out.println("检测到书+力量药水组合，生成伤能附魔书（无属性）");
            spawnSpecialBook(world, item1.getLerpedPos(1.0F), "damage_power");
            return true;
        }

        return false;
    }

    private static boolean isStrengthPotion(ItemStack stack) {
        if (!stack.isOf(Items.POTION)) {
            return false;
        }

        PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents != null) {
            Optional<RegistryEntry<Potion>> potion = potionContents.potion();
            if (potion.isPresent()) {
                RegistryEntry<Potion> potionEntry = potion.get();
                // 检查是否是力量药水（普通、长效或强效）
                boolean isStrength = potionEntry.matches(Potions.STRENGTH) ||
                        potionEntry.matches(Potions.LONG_STRENGTH) ||
                        potionEntry.matches(Potions.STRONG_STRENGTH);

                if (isStrength) {
                    System.out.println("检测到力量药水");
                }

                return isStrength;
            }
        }

        return false;
    }

    private static void spawnSpecialBook(ServerWorld world, Vec3d pos, String enchantType) {
        try {
            // 创建无属性附魔书，但带有特殊NBT标记
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

            // 创建NBT组件来存储自定义数据
            NbtCompound nbt = new NbtCompound();
            nbt.putString("magic-mod:enchant_type", enchantType);
            book.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

            // 设置显示名称（无属性状态）
            String displayName = "";
            if ("charge_power".equals(enchantType)) {
                displayName = "§e未激活的蓄能附魔书 §7(右键激活)";
            } else if ("damage_power".equals(enchantType)) {
                displayName = "§e未激活的伤能附魔书 §7(右键激活)";
            }

            // 设置自定义名称
            book.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName));

            // 生成物品实体
            ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, book);
            world.spawnEntity(itemEntity);

            System.out.println("成功生成特殊附魔书: " + enchantType);

            // 播放合成音效
            world.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);

            // 生成合成粒子效果 - 多彩粒子
            for (int i = 0; i < 30; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
                double offsetY = world.random.nextDouble() * 1.5;
                double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;

                // 随机选择粒子颜色
                if (world.random.nextBoolean()) {
                    world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x, pos.y + 0.5, pos.z, 1, offsetX, offsetY, offsetZ, 0.1);
                } else {
                    world.spawnParticles(ParticleTypes.GLOW,
                            pos.x, pos.y + 0.5, pos.z, 1, offsetX, offsetY, offsetZ, 0.1);
                }
            }

            // 生成光柱效果
            for (int i = 0; i < 10; i++) {
                double y = pos.y + (i * 0.2);
                world.spawnParticles(ParticleTypes.END_ROD,
                        pos.x, y, pos.z, 3, 0.1, 0.1, 0.1, 0.05);
            }

        } catch (Exception e) {
            System.err.println("生成特殊附魔书时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 清理方法，定期调用以避免内存泄漏
    public static void cleanup() {
        // 收集需要移除的位置
        List<BlockPos> positionsToRemove = new ArrayList<>();
        
        // 使用迭代器遍历键集，避免 ConcurrentModificationException
        Iterator<BlockPos> iterator = itemsAtPositions.keySet().iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            List<ItemEntity> items = itemsAtPositions.get(pos);
            if (items != null) {
                items.removeIf(item -> item.isRemoved() || !item.isAlive());
                if (items.isEmpty()) {
                    positionsToRemove.add(pos);
                }
            }
        }
        
        // 统一移除空的位置
        for (BlockPos pos : positionsToRemove) {
            itemsAtPositions.remove(pos);
        }
    }

    // ========== 元素法杖掉落合成 ==========

    private static boolean isElementInfusionItem(ItemStack stack) {
        return stack.isOf(ItemsRegistry.MAGIC_WAND_DAMAGE)
            || stack.isOf(ItemsRegistry.MAGIC_WAND_LEVITATION)
            || stack.isOf(Items.BLAZE_POWDER)
            || stack.isOf(Items.FEATHER)
            || stack.isOf(Items.DIRT)
            || stack.isOf(Items.CONDUIT);
    }

    /** 检查列表中是否全是灌注材料，跳过附魔书合成 */
    private static boolean allElementItems(List<ItemEntity> items) {
        for (ItemEntity item : items) {
            if (!isElementInfusionItem(item.getStack())) return false;
        }
        return true;
    }

    private static boolean checkAndCreateElementWand(ServerWorld world, List<ItemEntity> items) {
        if (items.size() < 2) return false;

        ItemEntity dW = null, lW = null, bl = null, fe = null, di = null, he = null;
        int infusionCount = 0;
        for (ItemEntity item : items) {
            if (item.isRemoved() || !item.isAlive()) continue;
            ItemStack s = item.getStack();
            if (!isElementInfusionItem(s)) continue;
            infusionCount++;
            if (dW == null && s.isOf(ItemsRegistry.MAGIC_WAND_DAMAGE)) dW = item;
            else if (lW == null && s.isOf(ItemsRegistry.MAGIC_WAND_LEVITATION)) lW = item;
            else if (bl == null && s.isOf(Items.BLAZE_POWDER)) bl = item;
            else if (fe == null && s.isOf(Items.FEATHER)) fe = item;
            else if (di == null && s.isOf(Items.DIRT)) di = item;
            else if (he == null && s.isOf(Items.CONDUIT)) he = item;
        }

        if (dW == null || lW == null || bl == null || fe == null || di == null || he == null) {
            // 有灌注物品但不齐全，提示缺失
            if (infusionCount >= 2 && infusionCount <= 5) {
                hintMissing(world, dW, lW, bl, fe, di, he);
            }
            return false;
        }

        UUID throwerId = null;
        if (dW.getOwner() != null) throwerId = dW.getOwner().getUuid();
        if (throwerId == null && lW.getOwner() != null) throwerId = lW.getOwner().getUuid();
        if (throwerId != null) {
            PlayerEntity thrower = world.getPlayerByUuid(throwerId);
            if (thrower instanceof ServerPlayerEntity sp) {
                if (!sp.isCreative() && !SimplePlayerDataManager.hasMana(sp, 100)) {
                    sp.sendMessage(Text.literal("§c元素灌注失败！需要100点魔法值，当前只有 "
                            + SimplePlayerDataManager.getCurrentMana(sp) + " 点。物品不会消失。"), false);
                    // 不移除物品，等待魔法值足够时再次触发
                    return false;
                }
                if (!sp.isCreative()) {
                    SimplePlayerDataManager.consumeMana(sp, 100);
                    com.magic.networking.MagicNetworking.sendManaSync(sp,
                            SimplePlayerDataManager.getCurrentMana(sp),
                            SimplePlayerDataManager.getMaxMana(sp));
                }
            }
        }

        dW.discard(); lW.discard(); bl.discard(); fe.discard(); di.discard(); he.discard();
        items.remove(dW); items.remove(lW); items.remove(bl);
        items.remove(fe); items.remove(di); items.remove(he);

        ItemStack wand = new ItemStack(ItemsRegistry.ELEMENT_WAND);
        ItemEntity wandEntity = new ItemEntity(world, dW.getX(), dW.getY(), dW.getZ(), wand);
        world.spawnEntity(wandEntity);

        world.playSound(null, wandEntity.getX(), wandEntity.getY(), wandEntity.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0f, 0.8f);
        world.playSound(null, wandEntity.getX(), wandEntity.getY(), wandEntity.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.5f);
        world.spawnParticles(ParticleTypes.END_ROD,
                wandEntity.getX(), wandEntity.getY() + 0.5, wandEntity.getZ(), 50, 0.3, 0.3, 0.3, 0.1);

        if (throwerId != null) {
            PlayerEntity thrower = world.getPlayerByUuid(throwerId);
            if (thrower != null) thrower.sendMessage(Text.literal("§a✨ 元素灌注成功！你获得了元素法杖！"), false);
        }
        System.out.println("[Magic] 元素灌注成功！");
        return true;
    }

    private static void hintMissing(ServerWorld world, ItemEntity dW, ItemEntity lW, ItemEntity bl, ItemEntity fe, ItemEntity di, ItemEntity he) {
        UUID ownerId = null;
        if (dW != null && dW.getOwner() != null) ownerId = dW.getOwner().getUuid();
        if (ownerId == null && lW != null && lW.getOwner() != null) ownerId = lW.getOwner().getUuid();
        if (ownerId == null) return;
        PlayerEntity p = world.getPlayerByUuid(ownerId);
        if (!(p instanceof ServerPlayerEntity sp)) return;

        String missing = "§e元素灌注材料不足：";
        if (dW == null) missing += "§c[伤害法杖] ";
        if (lW == null) missing += "§c[漂浮法杖] ";
        if (bl == null) missing += "§c[烈焰粉] ";
        if (fe == null) missing += "§c[羽毛] ";
        if (di == null) missing += "§c[泥土] ";
        if (he == null) missing += "§c[潮涌核心(Conduit)] ";
        missing += "§7(需全部丢在同一格)";
        sp.sendMessage(Text.literal(missing), false);
    }
}