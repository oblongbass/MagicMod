package com.magic;

import com.magic.Entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class MagicModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("magic-mod-client");
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.MAGIC_ZOMBIE, ZombieEntityRenderer::new);
        
        LOGGER.info("Magic Mod 客户端初始化成功!");
    }
}