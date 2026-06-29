package net.arm.util;

import net.arm.CapesAndTitul;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenMenuPacket(int section) implements CustomPacketPayload {
    public static final Type<OpenMenuPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "open_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeInt(value.section()),
            buf -> new OpenMenuPacket(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}