package com.magic.Entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.util.math.random.Random;

public class MagicZombieEntity extends ZombieEntity {

    public MagicZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.MAX_HEALTH, 25.0) // 修复：使用新的属性名
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0); // 修复：使用新的属性名
    }

    // 添加生成检查方法
    public static boolean canMobSpawn(EntityType<? extends MagicZombieEntity> type, ServerWorldAccess world, net.minecraft.entity.SpawnReason spawnReason, net.minecraft.util.math.BlockPos pos, Random random) {
        // 只在亮度小于5的地方生成
        return world.getLightLevel(pos) < 5 && HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random);
    }

    @Override
    protected boolean burnsInDaylight() {
        return true; // 和普通僵尸一样会在阳光下燃烧
    }
}