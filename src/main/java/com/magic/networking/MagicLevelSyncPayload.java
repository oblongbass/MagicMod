package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record MagicLevelSyncPayload(int magicLevel, boolean magicUnlocked) implements CustomPayload {

    public static final CustomPayload.Id<MagicLevelSyncPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "magic_level_sync"));

    public static final PacketCodec<PacketByteBuf, MagicLevelSyncPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeInt(value.magicLevel());
            buf.writeBoolean(value.magicUnlocked());
        },
        MagicLevelSyncPayload::read
    );

    private static MagicLevelSyncPayload read(PacketByteBuf buf) {
        int magicLevel = buf.readInt();
        boolean magicUnlocked = buf.readBoolean();
        return new MagicLevelSyncPayload(magicLevel, magicUnlocked);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}