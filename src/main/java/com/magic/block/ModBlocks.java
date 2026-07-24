package com.magic.block;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import java.util.function.Function;

import static com.magic.MagicMod.MOD_ID;

public final class ModBlocks {
    public static Block MAGIC_CIRCLE_BLOCK;
    public static Block MAGIC_ORE;
    
    private ModBlocks() {}
    
    public static Block register(String path, Function<Block.Settings, Block> factory) {
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, path));
        
        // 创建方块设置
        Block.Settings settings = Block.Settings.create()
            .strength(-1.0f, 3600000.0f) // 不可破坏
            .sounds(BlockSoundGroup.STONE)
            .requiresTool() // 需要工具（但不可破坏）
            .nonOpaque(); // 非不透明，允许光线通过
            
        return Blocks.register(registryKey, factory, settings);
    }
    
    public static Block registerOre(String path, Function<Block.Settings, Block> factory) {
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, path));
        
        // 创建矿石方块设置
        Block.Settings settings = Block.Settings.create()
            .strength(3.0f, 3.0f) // 硬度和抗性类似钻石矿
            .sounds(BlockSoundGroup.STONE)
            .requiresTool(); // 需要工具
            
        return Blocks.register(registryKey, factory, settings);
    }
    
    public static void initialize() {
        MAGIC_CIRCLE_BLOCK = register("magic_circle_block", MagicCircleBlock::new);
        MAGIC_ORE = registerOre("magic_ore", MagicOreBlock::new);
        
        System.out.println("方块注册完成");
    }
}