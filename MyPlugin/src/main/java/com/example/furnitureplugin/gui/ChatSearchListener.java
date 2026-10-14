package com.example.furnitureplugin.gui;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/** Captures the next chat message from a player who clicked "Search" in the
 *  GUI and uses it as a search query instead of letting it post publicly. */
public final class ChatSearchListener implements Listener {

    private final Plugin plugin;
    private final FurnitureGUI gui;
    private final GUIListener guiListener;

    public ChatSearchListener(Plugin plugin, FurnitureGUI gui, GUIListener guiListener) {
        this.plugin = plugin;
        this.gui = gui;
        this.guiListener = guiListener;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!guiListener.getAwaitingSearch().contains(player.getUniqueId())) return;

        event.setCancelled(true);
        guiListener.getAwaitingSearch().remove(player.getUniqueId());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage(org.bukkit.ChatColor.YELLOW + "Search cancelled.");
                return;
            }
            player.openInventory(gui.buildSearchPage(message.trim(), 0));
        });
    }
}
