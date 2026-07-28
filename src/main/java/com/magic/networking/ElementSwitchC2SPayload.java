package com.magic.networking;

import com.magic.MagicMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ElementSwitchC2SPayload(int element) implements CustomPayload {
    public static final CustomPayload.Id<ElementSwitchC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MagicMod.MOD_ID, "element_switch_c2s"));

    public static final PacketCodec<RegistryByteBuf, ElementSwitchC2SPayload> CODEC =
            PacketCodec.of(ElementSwitchC2SPayload::write, ElementSwitchC2SPayload::read);

    private void write(RegistryByteBuf buf) {
        buf.writeInt(element);
    }

    private static ElementSwitchC2SPayload read(RegistryByteBuf buf) {
        return new ElementSwitchC2SPayload(buf.readInt());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
