package net.arm.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.arm.CapesAndTitul;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

public class CapesAndTitulClient {

    public static final KeyMapping.Category CUSTOM_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(CapesAndTitul.MODID, "main")
    );

    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.capesandtitls.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            CUSTOM_CATEGORY
    );

    public static void init(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(CapesAndTitulClient::registerKeyMappings);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU_KEY);
    }
}