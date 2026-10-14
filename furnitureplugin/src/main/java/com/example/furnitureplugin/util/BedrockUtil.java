package com.example.furnitureplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.logging.Logger;

/**
 * Detects whether a given player is connecting through Geyser/Floodgate
 * (i.e. a Bedrock Edition player), so the plugin can render a different,
 * Bedrock-safe representation of each furniture piece for them.
 *
 * Floodgate is a soft-dependency: if it is not installed, isBedrockPlayer()
 * always returns false and every player is treated as Java, which is the
 * correct and safe fallback.
 */
public final class BedrockUtil {

    private static Boolean floodgatePresent = null;

    private BedrockUtil() {}

    public static boolean isFloodgateInstalled() {
        if (floodgatePresent == null) {
            floodgatePresent = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        }
        return floodgatePresent;
    }

    public static boolean isBedrockPlayer(Player player) {
        if (!isFloodgateInstalled()) {
            return false;
        }
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Throwable t) {
            // Floodgate plugin present but API mismatched/not ready yet - fail safe to Java.
            Logger.getLogger("FurniturePlugin").warning(
                    "Floodgate detected but API call failed (" + t + "); treating player as Java.");
            return false;
        }
    }
}
