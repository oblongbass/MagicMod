package com.magic;

import com.magic.Entity.ModEntities;
import com.magic.client.MagicCircleRenderer;
import com.magic.client.MagicCircleMessageHandler;
import com.magic.client.MagicCircleClientNetworking;
import com.magic.client.KeyBindings;
import com.magic.client.MagicCircleItemEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class MagicModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("magic-mod-client");
    @Override
    public void onInitializeClient() {
        // 注册魔法僵尸实体渲染器
        EntityRendererRegistry.register(ModEntities.MAGIC_ZOMBIE, ZombieEntityRenderer::new);
        
        // 注册魔法僵尸实体属性
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
            ModEntities.MAGIC_ZOMBIE, 
            com.magic.Entity.MagicZombieEntity.createMobAttributes()
        );
        
        // 注册魔法阵物品实体渲染器（使用自定义渲染器）
        EntityRendererRegistry.register(ModEntities.MAGIC_CIRCLE_ITEM_ENTITY, 
            MagicCircleItemEntityRenderer::new
        );
        
        // WorldRenderEvents在1.21.9中已被移除，魔法阵渲染现在通过Mixin实现
        
        // 注释掉聊天消息处理器，现在使用网络数据包系统
        // ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
        //     MagicCircleMessageHandler.onChatMessage(message);
        //     return true; // 允许消息继续传递
        // });
        
        // 注册按键绑定
        KeyBindings.register();
        
        // 注册客户端网络数据包处理器
        MagicCircleClientNetworking.initialize();
        
        // 魔法值HUD通过Mixins渲染
        
        LOGGER.info("Magic Mod 客户端初始化成功!");
    }
}
