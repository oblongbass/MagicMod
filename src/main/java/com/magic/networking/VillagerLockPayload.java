package com.magic.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import java.util.UUID;

public record VillagerLockPayload(UUID villagerUuid) implements CustomPayload {

    public static final CustomPayload.Id<VillagerLockPayload> ID = new CustomPayload.Id<>(Identifier.of("magic-mod", "villager_lock"));

    public static final PacketCodec<PacketByteBuf, VillagerLockPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeUuid(value.villagerUuid);
        },
        VillagerLockPayload::read
    );

    private static VillagerLockPayload read(PacketByteBuf buf) {
        return new VillagerLockPayload(buf.readUuid());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
