package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

// 简单的自定义Payload实现
public record SimpleMagicPayload(Identifier id, Vec3d position) implements CustomPayload {
    
    public static final CustomPayload.Id<SimpleMagicPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "simple_magic"));
    
    // PacketCodec用于编码和解码
    public static final PacketCodec<PacketByteBuf, SimpleMagicPayload> CODEC = PacketCodec.of(
        (value, buf) -> write(buf, value),
        SimpleMagicPayload::read
    );
    
    public SimpleMagicPayload(Identifier id) {
        this(id, null);
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    // 编码方法
    public static void write(PacketByteBuf buf, SimpleMagicPayload payload) {
        buf.writeString(payload.id().toString()); // 写入ID字符串
        if (payload.position() != null) {
            buf.writeBoolean(true); // 标记有位置数据
            buf.writeDouble(payload.position().x);
            buf.writeDouble(payload.position().y);
            buf.writeDouble(payload.position().z);
        } else {
            buf.writeBoolean(false); // 标记没有位置数据
        }
    }
    
    // 解码方法
    public static SimpleMagicPayload read(PacketByteBuf buf) {
        Identifier id = Identifier.of(buf.readString());
        boolean hasPosition = buf.readBoolean();
        if (hasPosition) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            return new SimpleMagicPayload(id, new Vec3d(x, y, z));
        } else {
            return new SimpleMagicPayload(id);
        }
    }
}