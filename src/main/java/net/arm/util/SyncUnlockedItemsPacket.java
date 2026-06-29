package net.arm.util;

import net.arm.CapesAndTitul;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record SyncUnlockedItemsPacket(UUID playerUuid, Set<String> unlockedItems, String activeCape, String activeTitle) implements CustomPacketPayload {
    public static final Type<SyncUnlockedItemsPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "sync_items"));

    public static final StreamCodec<FriendlyByteBuf, SyncUnlockedItemsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeUUID(value.playerUuid());
                buf.writeInt(value.unlockedItems().size());
                for (String item : value.unlockedItems()) {
                    buf.writeUtf(item);
                }
                buf.writeUtf(value.activeCape());
                buf.writeUtf(value.activeTitle());
            },
            buf -> {
                UUID uuid = buf.readUUID();
                int size = buf.readInt();
                Set<String> items = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    items.add(buf.readUtf());
                }
                String cape = buf.readUtf();
                String title = buf.readUtf();
                return new SyncUnlockedItemsPacket(uuid, items, cape, title);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}