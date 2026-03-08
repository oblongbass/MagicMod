package com.magic.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;

public class MagicCircleMessageHandler {
    
    public static void onChatMessage(Text message) {
        String messageString = message.getString();
        
        // 检查是否是魔法阵放置消息
        if (messageString.contains("[MAGIC_CIRCLE_PLACED]")) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                // 尝试解析消息中的坐标
                Vec3d circlePos = parseCirclePosition(messageString);
                if (circlePos != null) {
                    // 使用解析到的坐标作为魔法阵中心
                    MagicCircleRenderer.activateCircleForPlayer(client.player.getUuid(), circlePos);
                } else {
                    // 如果解析失败，使用玩家当前位置作为后备方案
                    Vec3d playerPos = client.player.getPos();
                    MagicCircleRenderer.activateCircleForPlayer(client.player.getUuid(), playerPos);
                }
            }
        }
        
        // 检查是否是魔法阵收回消息
        if (messageString.contains("[MAGIC_CIRCLE_REMOVED]")) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                // 移除当前玩家的魔法阵特效
                MagicCircleRenderer.deactivateCircleForPlayer(client.player.getUuid());
            }
        }
    }
    
    /**
     * 从消息字符串中解析魔法阵坐标
     * 消息格式: "§7[MAGIC_CIRCLE_PLACED] x y z§r"
     */
    private static Vec3d parseCirclePosition(String message) {
        try {
            // 提取坐标部分
            int startIndex = message.indexOf("[MAGIC_CIRCLE_PLACED]");
            if (startIndex == -1) return null;
            
            String coordPart = message.substring(startIndex + "[MAGIC_CIRCLE_PLACED]".length()).trim();
            // 移除可能存在的§r等格式代码
            coordPart = coordPart.replaceAll("§[0-9a-fk-or]", "").trim();
            
            String[] coords = coordPart.split("\\s+");
            if (coords.length >= 3) {
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);
                // 将粒子中心从物品实体位置（花岗岩上方1格）下降到花岗岩顶部
                // 物品实体在 y + 1.0，花岗岩顶部在 y + 0.5，所以我们减去 0.5
                y -= 0.5;
                return new Vec3d(x, y, z);
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
    }
}