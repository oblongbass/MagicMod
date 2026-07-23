package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record DialogueEndPayload(boolean shouldUnlockMagic) implements CustomPayload {

    public static final CustomPayload.Id<DialogueEndPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "dialogue_end"));

    public static final PacketCodec<PacketByteBuf, DialogueEndPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeBoolean(value.shouldUnlockMagic());
        },
        DialogueEndPayload::read
    );

    private static DialogueEndPayload read(PacketByteBuf buf) {
        boolean shouldUnlockMagic = buf.readBoolean();
        return new DialogueEndPayload(shouldUnlockMagic);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
