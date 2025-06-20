package com.magic.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MagicWandItem extends Item {
    public static final Item MAGIC_WAND = ItemsRegistry.register(
            "magic_wand_levitation",
            MagicWandItem::new,
            new Item.Settings().maxCount(1)
    );

    public MagicWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return ActionResult.PASS;
        }

        // 获取玩家视线方向
        Vec3d start = user.getEyePos();
        Vec3d rotation = user.getRotationVec(1.0F);
        double maxDistance = 15.0;
        Vec3d end = start.add(rotation.multiply(maxDistance));

        // 创建检测框
        Box box = user.getBoundingBox().stretch(rotation.multiply(maxDistance)).expand(1.0);

        // 检测实体
        EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(
                world, user, start, end, box,
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator()
        );

        if (entityHitResult != null) {
            Entity target = entityHitResult.getEntity();
            if (target instanceof LivingEntity) {
                applyLevitationEffect((LivingEntity) target);
                user.sendMessage(Text.literal("对 " + target.getName().getString() + " 施加了悬浮效果!"), true);
                return ActionResult.SUCCESS;
            }
        }

        user.sendMessage(Text.literal("未找到目标生物!"), true);
        return ActionResult.FAIL;
    }

    private void applyLevitationEffect(LivingEntity target) {
        // 施加悬浮效果（2级，7秒，隐藏所有效果）
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LEVITATION,
                7 * 20,   // 7秒 * 20刻/秒 = 140刻
                1,         // 等级2（0=等级1，1=等级2）
                false,     // 不是环境效果
                false,     // 不显示粒子
                false      // 不显示图标
        ));
    }
}