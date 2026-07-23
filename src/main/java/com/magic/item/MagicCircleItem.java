package com.magic.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.magic.client.MagicCircleRenderer;
import com.magic.data.SimplePlayerDataManager;
import com.magic.Entity.MagicCircleItemEntity;
import com.magic.networking.MagicNetworking;
import java.util.UUID;

public class MagicCircleItem extends Item {
    
    public MagicCircleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // 使用useOnBlock方法来处理点击地面放置
        // 不在use方法中激活特效，因为不知道具体放置位置
        return ActionResult.PASS; // 传递给useOnBlock处理
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = player.getStackInHand(hand);
        BlockPos hitPos = context.getBlockPos();
        
        if (!world.isClient()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            
            // 检查玩家是否已经有激活的魔法阵
            if (SimplePlayerDataManager.hasActiveMagicCircle(serverPlayer)) {
                // 玩家已经有魔法阵，提示去花岗岩上右键物品实体收回
                serverPlayer.sendMessage(Text.literal("§e你已经有魔法阵了！要收回魔法阵，请右键磨制花岗岩。").formatted(Formatting.YELLOW), false);
                return ActionResult.FAIL;
            }
            
            // 检查点击的面
            Direction side = context.getSide();
            BlockPos placementPos;
            
            if (side == Direction.UP) {
                // 点击方块上面，就在点击的方块上放置
                placementPos = hitPos.up();
            } else {
                // 点击其他面，在相邻位置放置
                placementPos = hitPos.offset(side);
            }
            
            // 放置新的魔法阵
            return placeMagicCircleAt(serverPlayer, stack, hand, world, placementPos);
        }
        
        // 如果是客户端，只处理预览，不重复激活已有魔法阵
        if (world.isClient()) {
            // 客户端只返回成功，让服务器处理实际放置
            // 不在客户端激活特效，因为服务器放置后会通过其他方式通知客户端
            return ActionResult.SUCCESS;
        }
        
        return ActionResult.SUCCESS;
    }
    
    private ActionResult placeMagicCircle(ServerPlayerEntity player, ItemStack stack, Hand hand, World world) {
        // 旧方法，使用玩家当前位置
        BlockPos playerFeetPos = player.getBlockPos();
        return placeMagicCircleAt(player, stack, hand, world, playerFeetPos);
    }
    
    private ActionResult placeMagicCircleAt(ServerPlayerEntity player, ItemStack stack, Hand hand, World world, BlockPos targetPos) {
        // 检查玩家是否在创造模式或有足够的物品
        if (!player.isCreative() && stack.getCount() < 1) {
            player.sendMessage(Text.literal("§c你需要一个传送法阵卷轴来放置魔法阵！").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 检查是否有足够的魔法值（需要50点）
        int manaCost = 50;
        if (!player.isCreative() && !com.magic.data.SimplePlayerDataManager.hasMana(player, manaCost)) {
            int currentMana = com.magic.data.SimplePlayerDataManager.getCurrentMana(player);
            player.sendMessage(Text.literal("§c魔法值不足！需要 " + manaCost + " 点，当前 " + currentMana + " 点").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 使用传入的目标位置作为魔法阵中心
        BlockPos circleCenter = new BlockPos(
            targetPos.getX(),
            targetPos.getY(),
            targetPos.getZ()
        );
        
        // 检查位置是否合适（不能在水里或空中）
        // 首先检查目标位置下方是否有坚实地面
        if (!world.getBlockState(circleCenter.down()).isSolid()) {
            player.sendMessage(Text.literal("§c魔法阵必须放置在坚实的地面上！").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 检查目标位置是否被占用
        if (!world.getBlockState(circleCenter).isAir()) {
            player.sendMessage(Text.literal("§c放置位置已被占用！").formatted(Formatting.RED), false);
            return ActionResult.FAIL;
        }
        
        // 创建平滑花岗岩方块（暂时使用普通平滑花岗岩）
        world.setBlockState(circleCenter, net.minecraft.block.Blocks.POLISHED_GRANITE.getDefaultState());
        
        // 在花岗岩上方生成魔法阵物品实体
        ItemStack magicCircleStack = new ItemStack(this, 1);
        MagicCircleItemEntity itemEntity = new MagicCircleItemEntity(
            world,
            circleCenter.getX() + 0.5,  // 方块中心
            circleCenter.getY() + 1.0,  // 方块上方1格
            circleCenter.getZ() + 0.5,  // 方块中心
            magicCircleStack,
            player.getUuid()
        );
        
        // 调试信息已移除
        
        world.spawnEntity(itemEntity);
        
        // 验证实体是否生成
        if (!itemEntity.isAlive()) {
            player.sendMessage(Text.literal("§c警告：物品实体生成失败！").formatted(Formatting.RED), false);
        }
        
        // 设置魔法阵数据
        SimplePlayerDataManager.setMagicCircle(player, circleCenter, world.getRegistryKey().getValue().toString(), itemEntity.getUuid());
        
        // 消耗魔法值（50点）
        if (!player.isCreative()) {
            com.magic.data.SimplePlayerDataManager.consumeMana(player, 50);
            // 同步魔法值到客户端
            com.magic.networking.MagicNetworking.sendManaSync(
                player, 
                com.magic.data.SimplePlayerDataManager.getCurrentMana(player),
                com.magic.data.SimplePlayerDataManager.getMaxMana(player)
            );
        }
        
        player.sendMessage(Text.literal("§e注意：魔法阵中心石已放置（平滑花岗岩）").formatted(Formatting.YELLOW), false);
        
        // 如果是生存模式，消耗一个物品
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        
        // 发送成功消息
        player.sendMessage(Text.literal("§a魔法阵已放置！按下F7可以返回这里。").formatted(Formatting.GREEN), false);
        
        // 计算魔法阵中心坐标（花岗岩顶部）
        double centerX = circleCenter.getX() + 0.5;
        double centerY = circleCenter.getY() + 0.5; // 花岗岩顶部，不是物品实体位置
        double centerZ = circleCenter.getZ() + 0.5;
        
        // 发送网络数据包激活客户端特效
        MagicNetworking.sendMagicCirclePlaced(player, new Vec3d(centerX, centerY, centerZ), player.getUuid());
        
        // 简单数据管理器不需要保存
        
        return ActionResult.SUCCESS;
    }
    
}