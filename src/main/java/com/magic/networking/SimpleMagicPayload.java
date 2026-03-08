package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

// 简单的自定义Payload实现
public record SimpleMagicPayload(Identifier id, Vec3d position, UUID ownerUuid) implements CustomPayload {
    
    public static final CustomPayload.Id<SimpleMagicPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "simple_magic"));
    
    // PacketCodec用于编码和解码
    public static final PacketCodec<PacketByteBuf, SimpleMagicPayload> CODEC = PacketCodec.of(
        (value, buf) -> write(buf, value),
        SimpleMagicPayload::read
    );
    
    public SimpleMagicPayload(Identifier id) {
        this(id, null, null);
    }
    
    public SimpleMagicPayload(Identifier id, Vec3d position) {
        this(id, position, null);
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    // 编码方法
    public static void write(PacketByteBuf buf, SimpleMagicPayload payload) {
        buf.writeString(payload.id().toString()); // 写入ID字符串
        
        // 写入位置数据标记和位置
        if (payload.position() != null) {
            buf.writeBoolean(true); // 标记有位置数据
            buf.writeDouble(payload.position().x);
            buf.writeDouble(payload.position().y);
            buf.writeDouble(payload.position().z);
        } else {
            buf.writeBoolean(false); // 标记没有位置数据
        }
        
        // 写入所有者UUID（支持向后兼容）
        if (payload.ownerUuid() != null) {
            buf.writeBoolean(true); // 标记有所有者UUID
            buf.writeUuid(payload.ownerUuid());
        } else {
            buf.writeBoolean(false); // 标记没有所有者UUID
        }
    }
    
    // 解码方法
    public static SimpleMagicPayload read(PacketByteBuf buf) {
        Identifier id = Identifier.of(buf.readString());
        
        // 读取位置数据
        boolean hasPosition = buf.readBoolean();
        Vec3d position = null;
        if (hasPosition) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            position = new Vec3d(x, y, z);
        }
        
        // 读取所有者UUID（向后兼容：如果缓冲区还有数据则读取，否则为null）
        UUID ownerUuid = null;
        if (buf.readableBytes() > 0) {
            boolean hasOwnerUuid = buf.readBoolean();
            if (hasOwnerUuid) {
                ownerUuid = buf.readUuid();
            }
        }
        
        return new SimpleMagicPayload(id, position, ownerUuid);
    }
}