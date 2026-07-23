package com.magic.networking;

import com.magic.MagicMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ElementSyncPayload(int element) implements CustomPayload {
    public static final CustomPayload.Id<ElementSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MagicMod.MOD_ID, "element_sync"));

    public static final PacketCodec<RegistryByteBuf, ElementSyncPayload> CODEC =
            PacketCodec.of(ElementSyncPayload::write, ElementSyncPayload::read);

    private void write(RegistryByteBuf buf) {
        buf.writeInt(element);
    }

    private static ElementSyncPayload read(RegistryByteBuf buf) {
        return new ElementSyncPayload(buf.readInt());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
