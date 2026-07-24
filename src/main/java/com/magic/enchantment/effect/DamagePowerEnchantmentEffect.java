package com.magic.enchantment.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record DamagePowerEnchantmentEffect(EnchantmentLevelBasedValue damageBonus) implements EnchantmentEntityEffect {
    public static final MapCodec<DamagePowerEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    EnchantmentLevelBasedValue.CODEC.fieldOf("damage_bonus").forGetter(DamagePowerEnchantmentEffect::damageBonus)
            ).apply(instance, DamagePowerEnchantmentEffect::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        // 这个效果会在攻击后触发
        // 由于我们的伤害逻辑在魔杖内部处理，这里可以留空
        // 或者添加一些视觉/声音反馈
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}