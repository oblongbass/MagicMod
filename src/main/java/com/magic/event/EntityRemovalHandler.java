package com.magic.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class EntityRemovalHandler {

    public static void register() {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            // 这里可以添加其他实体移除时的处理逻辑
            // 由于删除了放大系统，不再需要处理缩放数据
        });
    }
}