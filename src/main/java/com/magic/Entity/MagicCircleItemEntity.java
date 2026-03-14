package com.magic.Entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import com.magic.data.SimplePlayerDataManager;
import com.magic.MagicMod;
import com.magic.networking.MagicNetworking;
import java.util.UUID;

/**
 * 魔法阵物品实体 - 显示在花岗岩上的魔法阵物品
 * 不可捡起，不会消失，只有所有者可以右键收回
 */
public class MagicCircleItemEntity extends ItemEntity {
    private UUID ownerUuid;
    
    public MagicCircleItemEntity(EntityType<? extends ItemEntity> entityType, World world) {
        super(entityType, world);
    }
    
    public MagicCircleItemEntity(World world, double x, double y, double z, ItemStack stack, UUID ownerUuid) {
        super(ModEntities.MAGIC_CIRCLE_ITEM_ENTITY, world);
        this.setPosition(x, y, z);
        
        // 设置所有者UUID
        this.ownerUuid = ownerUuid;
        MagicMod.LOGGER.info("[Magic] 创建魔法阵物品实体，位置: ({}, {}, {}), 所有者UUID: {}, 实体UUID: {}", 
            x, y, z, ownerUuid, this.getUuid());
        
        // 将所有者UUID保存到物品堆栈的NBT中作为备份
        if (ownerUuid != null) {
            NbtCompound stackNbt = new NbtCompound();
            stackNbt.putString("OwnerUuid", ownerUuid.toString());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(stackNbt));
            MagicMod.LOGGER.info("[Magic] 已将所有者UUID保存到物品堆栈NBT");
        }
        
        this.setStack(stack);
        MagicMod.LOGGER.info("[Magic] 创建魔法阵物品实体，物品: {}", stack.getItem());

        // 设置属性：不可捡起、无敌、无重力
        this.setInvulnerable(true);
        this.setNoGravity(true);
        this.setPickupDelay(32767); // 最大延迟
    }
    
    @Override
    public void tick() {
        // 保持位置固定，不移动
        this.setVelocity(0, 0, 0);
        
        // 保持无重力
        this.setNoGravity(true);
        
        // 防止消失
        if (this.age > 6000) { // 5分钟后重置年龄
            this.age = 0;
        }
        
        super.tick();
    }
    
    @Override
    public boolean canHit() {
        // 允许玩家右键交互
        return true;
    }
    
    @Override
    public boolean isPushable() {
        // 禁止被推动
        return false;
    }
    
    public boolean cannotDespawn() {
        return true;
    }
    
    public boolean isFireImmune() {
        return true;
    }
    
    @Override
    public boolean shouldSave() {
        // 魔法阵物品实体应该被保存，以便重启后恢复
        return true;
    }
    
    @Override
    public void onPlayerCollision(PlayerEntity player) {
        // 禁止被捡起
        // 不调用父类方法
    }
    
    // 覆盖Entity的interact方法 - 所有者可以收回魔法阵
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (this.getEntityWorld().isClient()) {
            return ActionResult.SUCCESS;
        }
        
        MagicMod.LOGGER.info("[Magic] 玩家 {} 尝试收回魔法阵，实体UUID: {}, 所有者UUID: {}", 
            player.getName().getString(), this.getUuid(), ownerUuid);
        
        // 检查是否是所有者
        if (ownerUuid == null) {
            MagicMod.LOGGER.warn("[Magic] 魔法阵物品实体 {} 没有所有者信息，尝试从存档数据恢复...", this.getUuid());
            
            // 尝试从存档数据中恢复所有者UUID
            if (tryRecoverOwnerUuidFromSavedData()) {
                MagicMod.LOGGER.info("[Magic] 成功从存档数据恢复所有者UUID: {}", ownerUuid);
            } else {
                MagicMod.LOGGER.error("[Magic] 无法从存档数据恢复所有者信息！");
                player.sendMessage(net.minecraft.text.Text.literal("§c错误：魔法阵没有所有者信息！").formatted(net.minecraft.util.Formatting.RED), false);
                return ActionResult.FAIL;
            }
        }
        
        if (!ownerUuid.equals(player.getUuid())) {
            MagicMod.LOGGER.warn("[Magic] 玩家 {} 尝试收回不属于自己的魔法阵，所有者UUID: {}", 
                player.getName().getString(), ownerUuid);
            player.sendMessage(net.minecraft.text.Text.literal("§c只有魔法阵的主人才能收回！").formatted(net.minecraft.util.Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 检查玩家是否有空余的背包空间
        if (!player.isCreative() && player.getInventory().getEmptySlot() == -1) {
            player.sendMessage(net.minecraft.text.Text.literal("§c背包已满，无法收回魔法阵！").formatted(net.minecraft.util.Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 移除平滑花岗岩方块 - 更精确的位置计算
        // 物品实体在方块中心上方1格，所以我们需要找到正下方的方块
        BlockPos itemPos = this.getBlockPos(); // 物品所在的方块位置
        BlockPos granitePos = itemPos.down(); // 正下方的方块应该是花岗岩
        
        // 检查并移除花岗岩
        if (this.getEntityWorld().getBlockState(granitePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
            this.getEntityWorld().setBlockState(granitePos, net.minecraft.block.Blocks.AIR.getDefaultState());
        } else {
            player.sendMessage(net.minecraft.text.Text.literal("§c警告：未找到花岗岩方块，位置：" + granitePos).formatted(net.minecraft.util.Formatting.RED), false);
            // 尝试在物品位置下方2格查找（如果物品位置计算有误）
            BlockPos alternativePos = itemPos.down(2);
            if (this.getEntityWorld().getBlockState(alternativePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
                this.getEntityWorld().setBlockState(alternativePos, net.minecraft.block.Blocks.AIR.getDefaultState());
            }
        }
        
        // 如果是生存模式，返还一个魔法阵物品
        if (!player.isCreative()) {
            ItemStack magicCircleStack = this.getStack().copy();
            player.getInventory().offerOrDrop(magicCircleStack);
        }
        
        // 清除魔法阵数据
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity) {
            SimplePlayerDataManager.clearMagicCircle((net.minecraft.server.network.ServerPlayerEntity) player);
        }
        
        // 移除物品实体
        this.discard();
        
        // 发送成功消息
        player.sendMessage(net.minecraft.text.Text.literal("§a魔法阵已收回！").formatted(net.minecraft.util.Formatting.GREEN), false);
        
        // 发送网络数据包移除客户端特效
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity) {
            MagicNetworking.sendMagicCircleRemoved((net.minecraft.server.network.ServerPlayerEntity) player, ownerUuid);
        }
        
        return ActionResult.SUCCESS;
    }
    
    // 获取所有者UUID
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        // 不调用super，因为父类方法名可能不同
        MagicMod.LOGGER.info("[Magic] 写入魔法阵物品实体NBT数据，实体UUID: {}, 所有者UUID: {}", this.getUuid(), ownerUuid);
        if (ownerUuid != null) {
            nbt.putString("OwnerUuid", ownerUuid.toString());
            // 同时保存到物品堆栈的NBT中作为备份
            ItemStack stack = this.getStack();
            NbtCompound stackNbt = new NbtCompound();
            stackNbt.putString("OwnerUuid", ownerUuid.toString());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(stackNbt));
        }
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        // 不调用super，因为父类方法名可能不同
        MagicMod.LOGGER.info("[Magic] 读取魔法阵物品实体NBT数据，实体UUID: {}", this.getUuid());
        if (nbt.contains("OwnerUuid")) {
            try {
                String uuidStr = nbt.getString("OwnerUuid").orElse(null);
                if (uuidStr != null) {
                    ownerUuid = UUID.fromString(uuidStr);
                    MagicMod.LOGGER.info("[Magic] 从NBT读取所有者UUID: {}", ownerUuid);
                } else {
                    ownerUuid = null;
                    MagicMod.LOGGER.warn("[Magic] OwnerUuid字段为空");
                }
            } catch (IllegalArgumentException e) {
                ownerUuid = null;
                MagicMod.LOGGER.error("[Magic] 无效的OwnerUuid NBT数据", e);
            }
        } else {
            MagicMod.LOGGER.warn("[Magic] NBT中没有OwnerUuid字段，所有者信息可能丢失");
            // 尝试从物品堆栈的NBT中读取（向后兼容）
            ItemStack stack = this.getStack();
            NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComponent != null) {
                NbtCompound stackNbt = nbtComponent.copyNbt();
                if (stackNbt != null && stackNbt.contains("OwnerUuid")) {
                    try {
                        String uuidStr = stackNbt.getString("OwnerUuid").orElse(null);
                        if (uuidStr != null) {
                            ownerUuid = UUID.fromString(uuidStr);
                            MagicMod.LOGGER.info("[Magic] 从物品堆栈NBT读取所有者UUID: {}", ownerUuid);
                        }
                    } catch (IllegalArgumentException e) {
                        MagicMod.LOGGER.error("[Magic] 物品堆栈NBT中的OwnerUuid无效", e);
                    }
                }
            }
        }
    }
    
    /**
     * 尝试从存档数据中恢复所有者UUID
     * @return 是否成功恢复
     */
    private boolean tryRecoverOwnerUuidFromSavedData() {
        try {
            if (this.getEntityWorld().isClient()) {
                return false;
            }
            
            World world = this.getEntityWorld();
            com.magic.data.MagicCircleSavedData savedData = com.magic.data.MagicCircleSavedData.getOrCreate(world);
            if (savedData == null) {
                MagicMod.LOGGER.error("[Magic] 无法获取存档数据");
                return false;
            }
            
            java.util.Map<UUID, com.magic.data.PlayerData> allPlayerData = savedData.getAllPlayerData();
            UUID entityUuid = this.getUuid();
            MagicMod.LOGGER.info("[Magic] 尝试从存档数据恢复所有者，实体UUID: {}，存档中有 {} 个玩家数据", 
                entityUuid, allPlayerData.size());
            
            for (java.util.Map.Entry<UUID, com.magic.data.PlayerData> entry : allPlayerData.entrySet()) {
                UUID playerUuid = entry.getKey();
                com.magic.data.PlayerData playerData = entry.getValue();
                
                if (playerData.magicCircleItemUuid != null && playerData.magicCircleItemUuid.equals(entityUuid)) {
                    ownerUuid = playerUuid;
                    MagicMod.LOGGER.info("[Magic] 从存档数据找到所有者: {}，魔法阵位置: {}", ownerUuid, playerData.magicCirclePos);
                    
                    // 更新NBT数据，确保下次加载时正确
                    // this.setDirty(true); // Entity中可能没有这个方法
                    return true;
                }
            }
            
            MagicMod.LOGGER.warn("[Magic] 在存档数据中未找到匹配的魔法阵物品实体UUID: {}", entityUuid);
            return false;
            
        } catch (Exception e) {
            MagicMod.LOGGER.error("[Magic] 从存档数据恢复所有者UUID失败", e);
            return false;
        }
    }
}