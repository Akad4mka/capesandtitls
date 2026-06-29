package net.arm.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.arm.util.ModMessages;
import net.arm.util.SyncUnlockedItemsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File DATA_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), "capes_data");

    static {
        if (!DATA_DIR.exists()) DATA_DIR.mkdirs();
    }

    private static class PlayerData {
        Set<String> unlocked = new HashSet<>();
        String activeCape = "none";
        String activeTitle = "none";

        PlayerData() {
            unlocked.add("none");
        }
    }

    private static PlayerData loadData(UUID uuid) {
        File file = new File(DATA_DIR, uuid.toString() + ".json");
        if (!file.exists()) {
            return new PlayerData();
        }
        try (FileReader reader = new FileReader(file)) {
            PlayerData data = GSON.fromJson(reader, PlayerData.class);
            if (data == null) data = new PlayerData();
            if (data.unlocked == null) data.unlocked = new HashSet<>();
            data.unlocked.add("none");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayerData();
        }
    }

    private static void saveData(UUID uuid, PlayerData data) {
        File file = new File(DATA_DIR, uuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void equipItem(ServerPlayer player, int section, String itemId) {
        PlayerData data = loadData(player.getUUID());
        if (data.unlocked.contains(itemId) || itemId.equals("none")) {
            if (section == 0) data.activeCape = itemId;
            else if (section == 1) data.activeTitle = itemId;
            saveData(player.getUUID(), data);
        }

        syncToAll(player, (net.minecraft.server.level.ServerLevel) player.level());

        net.minecraft.server.MinecraftServer currentServer = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            var packet = new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                    java.util.EnumSet.of(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                    java.util.List.of(player)
            );

            currentServer.getPlayerList().broadcastAll(packet);
        }
    }

    public static void unlockItem(ServerPlayer player, String itemId) {
        PlayerData data = loadData(player.getUUID());
        data.unlocked.add(itemId);
        saveData(player.getUUID(), data);
        syncToClient(player);
    }

    public static void lockItem(ServerPlayer player, String itemId) {
        PlayerData data = loadData(player.getUUID());
        data.unlocked.remove(itemId);
        if (data.activeCape.equals(itemId)) data.activeCape = "none";
        if (data.activeTitle.equals(itemId)) data.activeTitle = "none";
        saveData(player.getUUID(), data);
        syncToAll(player, (net.minecraft.server.level.ServerLevel) player.level());
    }

    public static void syncToClient(ServerPlayer loggedInPlayer) {
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            PlayerData data = loadData(onlinePlayer.getUUID());
            ModMessages.sendToPlayer(new SyncUnlockedItemsPacket(
                    onlinePlayer.getUUID(),
                    data.unlocked,
                    data.activeCape,
                    data.activeTitle
            ), loggedInPlayer);
        }

        syncToAll(loggedInPlayer, (net.minecraft.server.level.ServerLevel) loggedInPlayer.level());
    }
    public static String getActiveTitle(UUID uuid) {
        PlayerData data = loadData(uuid);
        return data != null ? data.activeTitle : "none";
    }
    public static void syncToAll(ServerPlayer targetPlayer, net.minecraft.server.level.ServerLevel serverLevel) {
        PlayerData data = loadData(targetPlayer.getUUID());

        if (!data.unlocked.contains(data.activeCape)) data.activeCape = "none";
        if (!data.unlocked.contains(data.activeTitle)) data.activeTitle = "none";
        saveData(targetPlayer.getUUID(), data);

        SyncUnlockedItemsPacket syncPacket = new SyncUnlockedItemsPacket(targetPlayer.getUUID(), data.unlocked, data.activeCape, data.activeTitle);

        net.minecraft.server.MinecraftServer mcServer = serverLevel.getServer();
        if (mcServer != null) {
            for (ServerPlayer onlinePlayer : mcServer.getPlayerList().getPlayers()) {
                ModMessages.sendToPlayer(syncPacket, onlinePlayer);
            }
        }
    }
}