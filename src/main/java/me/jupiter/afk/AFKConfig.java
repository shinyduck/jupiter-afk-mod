package me.jupiter.afk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class AFKConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Points to config/jupiter_afk.json inside the Minecraft instance folder. */
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("jupiter_afk.json");

    // -------------------------------------------------------------------------
    // Timeout
    // -------------------------------------------------------------------------

    /** Seconds of inactivity before a player is marked AFK. */
    public int timeoutSeconds = 300;

    // -------------------------------------------------------------------------
    // Activity reset triggers
    // -------------------------------------------------------------------------

    /** Whether physical movement resets the AFK timer. */
    public boolean resetOnMovement = true;

    /** Whether looking around (rotating the camera) resets the AFK timer. */
    public boolean resetOnLook = true;

    /** Whether sending a chat message resets the AFK timer. */
    public boolean resetOnChat = false;

    /** Whether right-clicking a block resets the AFK timer. */
    public boolean resetOnBlockInteractions = true;

    // -------------------------------------------------------------------------
    // Messages
    // -------------------------------------------------------------------------

    /** Whether to broadcast chat messages when AFK state changes. */
    public boolean enableChatMessages = true;

    /** Message broadcast when a player becomes AFK. Use %player% for the player's name. */
    public String wentAfkMessage = "§e%player% is now AFK";

    /** Message broadcast when a player returns from AFK. Use %player% for the player's name. */
    public String returnedMessage = "§e%player% is no longer AFK";

    // -------------------------------------------------------------------------
    // Tab list
    // -------------------------------------------------------------------------

    /**
     * Format for the player's name in the tab list while AFK.
     * Use %player% for the player's name.
     * Supports § color codes.
     */
    public String afkPlayerName = "§7[AFK] §r%player%";

    // -------------------------------------------------------------------------
    // Protections
    // -------------------------------------------------------------------------

    /** Whether to stop hunger drain while a player is AFK. */
    public boolean freezeHunger = true;

    /** Whether to prevent damage while a player is AFK. */
    public boolean freezeDamage = true;

    // -------------------------------------------------------------------------
    // Sleep
    // -------------------------------------------------------------------------

    /** Whether AFK players are excluded from the sleep percentage calculation. */
    public boolean bypassSleep = true;

    // -------------------------------------------------------------------------
    // Load / save
    // -------------------------------------------------------------------------

    /**
     * Loads config from disk, or creates a default config file if none exists.
     */
    public static AFKConfig load() {
        try {
            if (Files.exists(PATH)) {
                return GSON.fromJson(Files.readString(PATH), AFKConfig.class);
            }
            Files.createDirectories(PATH.getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        AFKConfig config = new AFKConfig();
        config.save();
        return config;
    }

    /** Writes the current config to disk as pretty-printed JSON. */
    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
