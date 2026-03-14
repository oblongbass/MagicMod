package com.magic.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class MagicWandItem extends Item {
    // 存储正在进行的粒子效果任务
    private static final Map<UUID, ParticleTask> activeTasks = new HashMap<>();
    private static final int COOLDOWN_TICKS = 40; // 2秒冷却

    public MagicWandItem(Settings settings) {
        super(settings.maxCount(1));
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

        // 修复：使用新的实体检测方法
        EntityHitResult entityHitResult = findEntityInDirection(world, user, start, end, box);

        if (entityHitResult != null) {
            Entity target = entityHitResult.getEntity();
            if (target instanceof LivingEntity) {
                startParticleEffect((LivingEntity) target, (ServerWorld) world);
                user.sendMessage(Text.literal("开始对 " + target.getName().getString() + " 施放悬浮魔法!"), true);

                // 添加冷却时间
                user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

                return ActionResult.SUCCESS;
            }
        }

        user.sendMessage(Text.literal("未找到目标生物!"), true);
        return ActionResult.FAIL;
    }

    // 修复：手动实现实体检测，避免使用已更改的 ProjectileUtil 方法
    private EntityHitResult findEntityInDirection(World world, PlayerEntity user, Vec3d start, Vec3d end, Box box) {
        EntityHitResult result = null;
        double closestDistance = Double.MAX_VALUE;

        // 获取所有在检测框内的生物实体
        for (Entity entity : world.getOtherEntities(user, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator())) {

            // 检查视线是否与实体碰撞箱相交
            Box entityBox = entity.getBoundingBox();
            Optional<Vec3d> hit = entityBox.raycast(start, end);

            if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    result = new EntityHitResult(entity, hit.get());
                }
            }
        }

        return result;
    }

    // 开始粒子效果序列
    private void startParticleEffect(LivingEntity target, ServerWorld world) {
        UUID targetId = target.getUuid();

        // 取消任何现有的效果
        if (activeTasks.containsKey(targetId)) {
            activeTasks.remove(targetId);
        }

        // 创建新任务
        ParticleTask task = new ParticleTask(target, world);
        activeTasks.put(targetId, task);

        // 播放开始音效
        world.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON,
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );
    }

    // 在服务器刻事件中更新粒子效果
    public static void tickParticleEffects() {
        for (UUID targetId : activeTasks.keySet().toArray(new UUID[0])) {
            ParticleTask task = activeTasks.get(targetId);
            if (task == null) continue;

            if (task.update()) {
                // 任务完成
                activeTasks.remove(targetId);
            }
        }
    }

    // 粒子效果任务类
    private static class ParticleTask {
        private final LivingEntity target;
        private final ServerWorld world;
        private final long startTime;
        private int stage = 0; // 0=扩散阶段, 1=汇聚阶段

        public ParticleTask(LivingEntity target, ServerWorld world) {
            this.target = target;
            this.world = world;
            this.startTime = world.getTime();
        }

        public boolean update() {
            long elapsedTicks = world.getTime() - startTime;

            // 第一阶段：扩散粒子 (0-40刻 = 2秒)
            if (elapsedTicks < 40) {
                // 在目标周围5格内生成粒子
                spawnParticlesInSphere(target.getLerpedPos(1.0F), 5.0, ParticleTypes.CLOUD, 10);
                return false;
            }

            // 第二阶段：汇聚粒子 (40-60刻)
            if (stage == 0) {
                stage = 1; // 进入第二阶段
            }

            if (elapsedTicks < 60) {
                // 粒子向目标汇聚
                spawnConvergingParticles(target.getLerpedPos(1.0F), ParticleTypes.CLOUD, 20);
                return false;
            }

            // 第三阶段：应用效果
            if (!target.isRemoved()) {
                applyLevitationEffect(target);

                // 在目标位置生成最终效果粒子
                world.spawnParticles(
                        ParticleTypes.POOF,  // 使用 POOF 替代 CLOUD_BURST
                        target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                        30,  // 数量
                        0.5, 0.5, 0.5, // 范围
                        0.1  // 速度
                );
            }

            return true; // 任务完成
        }

        // 在球形区域内生成粒子
        private void spawnParticlesInSphere(Vec3d center, double radius, ParticleEffect particle, int count) {
            for (int i = 0; i < count; i++) {
                // 随机球面上的点
                double theta = world.random.nextDouble() * Math.PI * 2;
                double phi = world.random.nextDouble() * Math.PI;
                double r = radius * world.random.nextDouble();

                double x = center.x + r * Math.sin(phi) * Math.cos(theta);
                double y = center.y + r * Math.sin(phi) * Math.sin(theta);
                double z = center.z + r * Math.cos(phi);

                // 随机速度使粒子飘动
                double vx = (world.random.nextDouble() - 0.5) * 0.05;
                double vy = (world.random.nextDouble() - 0.5) * 0.05;
                double vz = (world.random.nextDouble() - 0.5) * 0.05;

                world.spawnParticles(particle, x, y, z, 1, vx, vy, vz, 0.0);
            }
        }

        // 生成向中心汇聚的粒子
        private void spawnConvergingParticles(Vec3d center, ParticleEffect particle, int count) {
            for (int i = 0; i < count; i++) {
                // 在球体表面生成粒子
                double theta = world.random.nextDouble() * Math.PI * 2;
                double phi = world.random.nextDouble() * Math.PI;
                double r = 5.0;

                double x = center.x + r * Math.sin(phi) * Math.cos(theta);
                double y = center.y + r * Math.sin(phi) * Math.sin(theta);
                double z = center.z + r * Math.cos(phi);

                // 计算指向中心的方向向量
                double dx = center.x - x;
                double dy = center.y - y;
                double dz = center.z - z;

                // 归一化并设置速度
                double length = Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (length > 0) {
                    double speed = 0.2;
                    dx = dx / length * speed;
                    dy = dy / length * speed;
                    dz = dz / length * speed;
                }

                world.spawnParticles(particle, x, y, z, 1, dx, dy, dz, 0.0);
            }
        }

        // 施加悬浮效果
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

            // 播放效果音效
            if (target.getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSound(
                        null,
                        target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                        SoundCategory.PLAYERS,
                        1.0f,
                        0.8f
                );
            }
        }
    }
}