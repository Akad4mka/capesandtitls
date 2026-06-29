package net.arm.cape;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import java.util.*;

public class CapeManager {
    public static String currentCapeId = "none";
    public static Identifier currentCapeTexture = null;

    public static final Map<UUID, String> OTHER_PLAYER_CAPES = new HashMap<>();
    public static final Map<UUID, String> OTHER_PLAYER_TITLES = new HashMap<>();
    public static final Set<String> UNLOCKED_ITEMS = new HashSet<>();
    public static String currentTitleId = "none";

    public static boolean isUnlocked(String id) {
        return UNLOCKED_ITEMS.contains(id) || id.equals("none");
    }

    public static void loadActiveCapeAndTitle(String capeId, String titleId) {
        currentCapeId = capeId;
        currentTitleId = titleId;

        if (capeId.equals("none")) {
            currentCapeTexture = null;
        } else {
            currentCapeTexture = Identifier.fromNamespaceAndPath("capesandtitls", capeId);
        }
    }

    public static ClientAsset.Texture getCapeTextureForPlayer(UUID uuid) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

        if (mc.player != null && uuid.equals(mc.player.getUUID())) {
            if (currentCapeTexture != null) {
                return new ClientAsset.ResourceTexture(currentCapeTexture);
            }
            return null;
        }

        String capeId = OTHER_PLAYER_CAPES.get(uuid);
        if (capeId != null && !capeId.equals("none")) {
            return new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("capesandtitls", capeId));
        }

        return null;
    }

    public static ClientAsset.Texture getActiveCapeTexture() {
        if (currentCapeTexture != null) {
            return new ClientAsset.ResourceTexture(currentCapeTexture);
        }
        return null;
    }
}