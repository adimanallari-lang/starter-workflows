package com.example.furnitureplugin.resourcepack;

import com.example.furnitureplugin.util.ChatColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import java.net.URI;

/** Sends the Java resource pack to every joining Java player automatically.
 *  Bedrock (Floodgate) players never receive this -- Geyser serves Bedrock
 *  players its own resource pack from its packs/ folder, see BEDROCK_SETUP.md. */
public final class PackDeliveryListener implements Listener {

    private final Plugin plugin;
    private final ResourcePackServer server;
    private final String publicUrl;
    private final boolean forceAccept;
    private final String promptMessage;

    public PackDeliveryListener(Plugin plugin, ResourcePackServer server, String publicUrl,
                                 boolean forceAccept, String promptMessage) {
        this.plugin = plugin;
        this.server = server;
        this.publicUrl = publicUrl;
        this.forceAccept = forceAccept;
        this.promptMessage = promptMessage;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (com.example.furnitureplugin.util.BedrockUtil.isBedrockPlayer(player)) {
            return; // Bedrock players get their pack from Geyser, not this listener
        }

        player.setResourcePack(
                URI.create(publicUrl),
                server.getSha1Bytes(),
                Component.text(ChatColorUtil.color(promptMessage)),
                forceAccept
        );
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        if (!forceAccept) return;
        switch (event.getStatus()) {
            case DECLINED, FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL -> {
                event.getPlayer().kick(Component.text(ChatColorUtil.color(
                        "&cThis server requires the custom furniture resource pack. Please rejoin and accept it.")));
            }
            default -> { /* ACCEPTED / DOWNLOADED / SUCCESSFULLY_LOADED - nothing to do */ }
        }
    }
}
