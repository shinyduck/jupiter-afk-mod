package me.jupiter.afk;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class AFKManager {

    public static AFKConfig config = AFKConfig.load();

    // Tracks the last time each player performed an action (epoch ms)
    private static final HashMap<UUID, Long> lastAction = new HashMap<>();

    // Set of UUIDs currently marked as AFK
    private static final HashSet<UUID> afkPlayers = new HashSet<>();

    // -------------------------------------------------------------------------
    // Player lifecycle
    // -------------------------------------------------------------------------

    /**
     * Called when a player joins the server.
     * Seeds their last-action time so they don't immediately time out.
     */
    public static void addPlayer(ServerPlayer player) {
        lastAction.put(player.getUUID(), System.currentTimeMillis());
    }

    /**
     * Called when a player leaves the server.
     * Cleans up both maps to prevent memory leaks on long-running servers.
     */
    public static void removePlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        lastAction.remove(uuid);
        afkPlayers.remove(uuid);
    }

    // -------------------------------------------------------------------------
    // AFK state
    // -------------------------------------------------------------------------

    /**
     * Records activity for a player, clearing AFK status if they were AFK.
     * Called by movement, chat, and interaction mixins/events.
     */
    public static void updateActivity(ServerPlayer player) {
        if (afkPlayers.contains(player.getUUID())) {
            setAfk(player, false);
        }
        lastAction.put(player.getUUID(), System.currentTimeMillis());
    }

    /**
     * Explicitly sets a player's AFK state.
     * Broadcasts a message and refreshes the tab list when state changes.
     */
    public static void setAfk(ServerPlayer player, boolean state) {
        if (state) {
            if (afkPlayers.add(player.getUUID())) {
                broadcast(player, config.wentAfkMessage);
                refreshTabList(player);
            }
        } else {
            if (afkPlayers.remove(player.getUUID())) {
                lastAction.put(player.getUUID(), System.currentTimeMillis());
                broadcast(player, config.returnedMessage);
                refreshTabList(player);
            }
        }
    }

    /** Returns true if the given player is currently AFK. */
    public static boolean isAfk(ServerPlayer player) {
        return afkPlayers.contains(player.getUUID());
    }

    /** Returns the number of players currently marked as AFK. */
    public static int getAfkCount() {
        return afkPlayers.size();
    }

    /**
     * Called every tick (via PlayerMixin) to check if a player has exceeded
     * the configured idle timeout and should be marked AFK.
     */
    public static void checkTimeout(ServerPlayer player) {
        if (isAfk(player)) return;
        long last = lastAction.getOrDefault(player.getUUID(), System.currentTimeMillis());
        if (System.currentTimeMillis() - last > (config.timeoutSeconds * 1000L)) {
            setAfk(player, true);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Broadcasts a chat message to all players on the server,
     * replacing %player% with the AFK player's name.
     * Does nothing if enableChatMessages is false in config.
     */
    private static void broadcast(ServerPlayer player, String msg) {
        if (!config.enableChatMessages) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = serverLevel.getServer();
        if (server == null) return;

        String text = msg.replace("%player%", player.getScoreboardName());
        server.getPlayerList().broadcastSystemMessage(Component.literal(text), false);
    }

    /**
     * Sends a tab-list display-name update packet to all connected players,
     * causing them to re-query the given player's display name immediately.
     * This is what makes the [AFK] prefix appear/disappear without delay.
     */
    private static void refreshTabList(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        MinecraftServer server = serverLevel.getServer();
        if (server == null) return;

        server.getPlayerList().broadcastAll(
                new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        player
                )
        );
    }
}
