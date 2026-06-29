package net.arm.client;

import net.arm.CapesAndTitul;
import net.arm.gui.CapesAndTitulsScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CapesAndTitul.MODID, value = Dist.CLIENT)
public class ClientKeyEvents {

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.screen == null) {
            while (CapesAndTitulClient.OPEN_MENU_KEY.consumeClick()) {
                mc.setScreen(new CapesAndTitulsScreen(0));
            }
        }
    }
}