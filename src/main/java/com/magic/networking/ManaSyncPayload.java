package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

/**
 * 魔法值同步Payload
 */
public record ManaSyncPayload(int currentMana, int maxMana) implements CustomPayload {
    
    public static final CustomPayload.Id<ManaSyncPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "mana_sync"));
    
    public static final PacketCodec<PacketByteBuf, ManaSyncPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeInt(value.currentMana());
            buf.writeInt(value.maxMana());
        },
        ManaSyncPayload::read
    );
    
    private static ManaSyncPayload read(PacketByteBuf buf) {
        int current = buf.readInt();
        int max = buf.readInt();
        return new ManaSyncPayload(current, max);
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
