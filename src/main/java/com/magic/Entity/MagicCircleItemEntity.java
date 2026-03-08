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
        
        // 将所有者UUID存储在物品NBT中
        if (ownerUuid != null) {
            // 在Minecraft 1.21.8中，ItemStack的NBT处理方式可能已改变
            // 我们尝试直接修改物品堆栈
            this.ownerUuid = ownerUuid;
            com.magic.MagicMod.LOGGER.info("[Magic] 创建魔法阵物品实体，所有者UUID: {}", ownerUuid);
        }
        
        this.setStack(stack);
        com.magic.MagicMod.LOGGER.info("[Magic] 创建魔法阵物品实体，物品: {}", stack.getItem());

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
        if (this.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }
        
        // 检查是否是所有者
        if (ownerUuid == null) {
            player.sendMessage(net.minecraft.text.Text.literal("§c错误：魔法阵没有所有者信息！").formatted(net.minecraft.util.Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        if (!ownerUuid.equals(player.getUuid())) {
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
        if (this.getWorld().getBlockState(granitePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
            this.getWorld().setBlockState(granitePos, net.minecraft.block.Blocks.AIR.getDefaultState());
        } else {
            player.sendMessage(net.minecraft.text.Text.literal("§c警告：未找到花岗岩方块，位置：" + granitePos).formatted(net.minecraft.util.Formatting.RED), false);
            // 尝试在物品位置下方2格查找（如果物品位置计算有误）
            BlockPos alternativePos = itemPos.down(2);
            if (this.getWorld().getBlockState(alternativePos).getBlock() == net.minecraft.block.Blocks.POLISHED_GRANITE) {
                this.getWorld().setBlockState(alternativePos, net.minecraft.block.Blocks.AIR.getDefaultState());
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
}