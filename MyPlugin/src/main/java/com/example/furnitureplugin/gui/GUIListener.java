package com.example.furnitureplugin.gui;

import com.example.furnitureplugin.furniture.FurnitureCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Handles every click inside a FurniturePlugin-owned inventory. */
public final class GUIListener implements Listener {

    private final Plugin plugin;
    private final FurnitureGUI gui;
    /** Players who clicked "Search" and are expected to type a query in chat next. */
    private final Set<java.util.UUID> awaitingSearch = ConcurrentHashMap.newKeySet();

    public GUIListener(Plugin plugin, FurnitureGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    public Set<java.util.UUID> getAwaitingSearch() {
        return awaitingSearch;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;
        event.setCancelled(true); // this is a read-only catalog GUI; nothing should move in/out directly

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        switch (holder.getViewType()) {
            case MAIN -> handleMainClick(player, holder, slot);
            case CATEGORY, SEARCH -> handleListClick(player, holder, slot);
        }
    }

    private void handleMainClick(Player player, GUIHolder holder, int slot) {
        FurnitureCategory[] categories = FurnitureCategory.values();
        if (slot == 40) {
            awaitingSearch.add(player.getUniqueId());
            player.closeInventory();
            player.sendMessage(org.bukkit.ChatColor.AQUA +
                    "Type a search term in chat (or 'cancel' to abort):");
            return;
        }
        if (slot >= 0 && slot < categories.length) {
            player.openInventory(gui.buildCategoryPage(categories[slot], 0));
        }
    }

    private void handleListClick(Player player, GUIHolder holder, int slot) {
        if (slot == 49) {
            gui.openMain(player);
            return;
        }
        if (slot == 45 && holder.getPage() > 0) {
            reopen(player, holder, holder.getPage() - 1);
            return;
        }
        if (slot == 53) {
            reopen(player, holder, holder.getPage() + 1);
            return;
        }
        if (slot < holder.getShown().size()) {
            ItemStack toGive = holder.getInventory().getItem(slot);
            if (toGive == null) return;
            var leftover = player.getInventory().addItem(toGive.clone());
            if (!leftover.isEmpty()) {
                player.sendMessage(org.bukkit.ChatColor.RED + "Your inventory is full!");
                return;
            }
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Got " +
                    toGive.getItemMeta().getDisplayName() + org.bukkit.ChatColor.GREEN +
                    "! Right-click a block to place it.");
        }
    }

    private void reopen(Player player, GUIHolder holder, int newPage) {
        if (holder.getViewType() == GUIHolder.ViewType.CATEGORY) {
            player.openInventory(gui.buildCategoryPage(holder.getCategory(), newPage));
        } else {
            player.openInventory(gui.buildSearchPage(holder.getSearchQuery(), newPage));
        }
    }
}
