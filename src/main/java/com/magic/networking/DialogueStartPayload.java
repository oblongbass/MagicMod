package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record DialogueStartPayload() implements CustomPayload {

    public static final CustomPayload.Id<DialogueStartPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "dialogue_start"));

    public static final PacketCodec<PacketByteBuf, DialogueStartPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            // No data to write
        },
        DialogueStartPayload::read
    );

    private static DialogueStartPayload read(PacketByteBuf buf) {
        return new DialogueStartPayload();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}