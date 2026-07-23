package com.magic.enchantment;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.magic.MagicMod.MOD_ID;

public class ModEnchantmentEffects {

    // 注册自定义附魔效果类型
    public static void register() {
        // 注册 damage_power_effect
        Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                Identifier.of(MOD_ID, "damage_power_effect"),
                com.magic.enchantment.effect.DamagePowerEnchantmentEffect.CODEC);

        // 注册 charge_power_effect
        Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE,
                Identifier.of(MOD_ID, "charge_power_effect"),
                com.magic.enchantment.effect.ChargePowerEnchantmentEffect.CODEC);

        System.out.println("注册自定义附魔效果类型");
    }
}