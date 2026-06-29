package net.arm.util;

import net.arm.CapesAndTitul;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EquipItemPacket(int section, String itemId) implements CustomPacketPayload {
    public static final Type<EquipItemPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "equip_item"));

    public static final StreamCodec<FriendlyByteBuf, EquipItemPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.section());
                buf.writeUtf(value.itemId());
            },
            buf -> new EquipItemPacket(buf.readInt(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}