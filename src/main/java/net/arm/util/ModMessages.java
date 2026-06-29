package net.arm.util;

import net.arm.CapesAndTitul;
import net.arm.cape.CapeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModMessages {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CapesAndTitul.MODID).versioned("1").optional();

        registrar.playToClient(
                SyncUnlockedItemsPacket.TYPE,
                SyncUnlockedItemsPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (FMLEnvironment.getDist() == Dist.CLIENT) {
                            CapeManager.OTHER_PLAYER_TITLES.put(payload.playerUuid(), payload.activeTitle());
                            CapeManager.OTHER_PLAYER_CAPES.put(payload.playerUuid(), payload.activeCape());

                            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                            if (mc.player != null && payload.playerUuid().equals(mc.player.getUUID())) {
                                CapeManager.UNLOCKED_ITEMS.clear();
                                CapeManager.UNLOCKED_ITEMS.addAll(payload.unlockedItems());
                                CapeManager.loadActiveCapeAndTitle(payload.activeCape(), payload.activeTitle());
                            }
                        }
                    });
                }
        );

        registrar.playToServer(
                EquipItemPacket.TYPE,
                EquipItemPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            net.arm.server.ServerDataManager.equipItem(player, payload.section(), payload.itemId());

                            player.refreshDisplayName();

                            player.connection.send(new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                                    net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                    player
                            ));
                        }
                    });
                }
        );
    }

    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(packet);
        }
    }
}