package net.arm.server;

import net.arm.cape.CapeManager;
import net.arm.util.SyncUnlockedItemsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "capesandtitls")
public class TitleEventHandler {

    private static MutableComponent parseTitle(String titleId) {
        MutableComponent container = Component.empty();

        TitleConfig.TitleDataHolder data = TitleConfig.getTitle(titleId);

        if (data != null) {
            String text = data.text();

            if (text.contains("&#")) {
                String[] parts = text.split("&#");
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    if (part.length() >= 7) {
                        String hexColor = "#" + part.substring(0, 6);
                        String character = part.substring(6);

                        TextColor textColor = TextColor.parseColor(hexColor)
                                .result()
                                .orElse(TextColor.fromRgb(0xFFFFFF));

                        Style style = Style.EMPTY.withColor(textColor).withBold(true);
                        container.append(Component.literal(character).setStyle(style));
                    } else {
                        container.append(Component.literal(part).withStyle(net.minecraft.ChatFormatting.GRAY));
                    }
                }
            } else {
                TextColor textColor = TextColor.parseColor(data.color())
                        .result()
                        .orElse(TextColor.fromRgb(0xFFFFFF));

                Style style = Style.EMPTY
                        .withColor(textColor)
                        .withBold(data.bold())
                        .withItalic(data.italic())
                        .withUnderlined(data.underlined());

                container.append(Component.literal(text).setStyle(style));
            }
        } else {
            Style style = Style.EMPTY.withColor(net.minecraft.ChatFormatting.GREEN).withBold(true);
            container.append(Component.literal(titleId.toUpperCase()).setStyle(style));
        }

        container.append(Component.literal(" "));
        return container;
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
    }

    @SubscribeEvent
    public static void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String activeTitleId = ServerDataManager.getActiveTitle(player.getUUID());
            if (activeTitleId == null || activeTitleId.equals("none")) return;

            MutableComponent titleComponent = parseTitle(activeTitleId);
            event.setDisplayname(Component.empty().append(titleComponent).append(event.getUsername()));
        }
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String activeTitleId = ServerDataManager.getActiveTitle(player.getUUID());
            if (activeTitleId == null || activeTitleId.equals("none")) return;

            MutableComponent titleComponent = parseTitle(activeTitleId);
            event.setDisplayName(Component.empty().append(titleComponent).append(player.getName()));
        }
    }
}