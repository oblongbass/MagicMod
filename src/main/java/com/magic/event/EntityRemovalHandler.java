package com.magic.event;

import com.magic.scale.ScaleManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;

public class EntityRemovalHandler {
    
    public static void register() {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof LivingEntity) {
                ScaleManager.onEntityRemoved((LivingEntity) entity);
            }
        });
    }
} 