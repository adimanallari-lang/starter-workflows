package com.example.furnitureplugin.gui;

import com.example.furnitureplugin.furniture.FurnitureCatalog;
import com.example.furnitureplugin.furniture.FurnitureCategory;
import com.example.furnitureplugin.furniture.FurnitureDefinition;
import com.example.furnitureplugin.furniture.FurnitureItemFactory;
import com.example.furnitureplugin.util.ChatColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Builds every inventory screen of /furniture: the category picker, the
 *  paginated per-category item list, and the search-results list. */
public final class FurnitureGUI {

    private static final int ITEMS_PER_PAGE = 36; // rows 1-4 of a 6-row GUI; row 5 = nav, row 6 = border/search

    private final FurnitureCatalog catalog;
    private final FurnitureItemFactory itemFactory;

    public FurnitureGUI(FurnitureCatalog catalog, FurnitureItemFactory itemFactory) {
        this.catalog = catalog;
        this.itemFactory = itemFactory;
    }

    public Inventory buildMainMenu() {
        GUIHolder holder = new GUIHolder(GUIHolder.ViewType.MAIN, null, null, 0, List.of());
        Inventory inv = Bukkit.createInventory(holder, 45, ChatColorUtil.color("&8&lFurniture Catalog"));
        holder.setInventory(inv);

        int slot = 0;
        for (FurnitureCategory cat : FurnitureCategory.values()) {
            ItemStack icon = new ItemStack(Material.CHEST);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColorUtil.color(catalog.getCategoryDisplayName(cat)));
            int count = catalog.getByCategory(cat).size();
            meta.setLore(List.of(
                    ChatColorUtil.color("&7" + count + " item" + (count == 1 ? "" : "s")),
                    ChatColorUtil.color(count == 0 ? "&8(coming in a future batch)" : "&eClick to browse")
            ));
            icon.setItemMeta(meta);
            inv.setItem(slot, icon);
            slot++;
        }

        inv.setItem(40, searchButton());
        return inv;
    }

    public Inventory buildCategoryPage(FurnitureCategory category, int page) {
        List<FurnitureDefinition> all = catalog.getByCategory(category);
        return buildListPage(GUIHolder.ViewType.CATEGORY, category, null, page, all);
    }

    public Inventory buildSearchPage(String query, int page) {
        List<FurnitureDefinition> results = catalog.search(query);
        return buildListPage(GUIHolder.ViewType.SEARCH, null, query, page, results);
    }

    private Inventory buildListPage(GUIHolder.ViewType type, FurnitureCategory category, String query,
                                     int page, List<FurnitureDefinition> all) {
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        int from = page * ITEMS_PER_PAGE;
        int to = Math.min(all.size(), from + ITEMS_PER_PAGE);
        List<FurnitureDefinition> shown = new ArrayList<>(all.subList(from, to));

        GUIHolder holder = new GUIHolder(type, category, query, page, shown);
        String title = type == GUIHolder.ViewType.SEARCH
                ? "&8&lSearch: " + query
                : "&8&l" + catalog.getCategoryDisplayName(category);
        Inventory inv = Bukkit.createInventory(holder, 54, ChatColorUtil.color(title));
        holder.setInventory(inv);

        for (int i = 0; i < shown.size(); i++) {
            inv.setItem(i, buildCatalogItem(shown.get(i)));
        }

        if (page > 0) inv.setItem(45, navItem(Material.ARROW, "&ePrevious Page"));
        inv.setItem(49, navItem(Material.BARRIER, "&cBack to Categories"));
        if (page < totalPages - 1) inv.setItem(53, navItem(Material.ARROW, "&eNext Page"));

        return inv;
    }

    private ItemStack buildCatalogItem(FurnitureDefinition def) {
        ItemStack item = itemFactory.createItem(def);
        // createItem already sets name/lore/PDC tag; the GUI copy and the
        // in-world placement item are identical so clicking in the GUI
        // simply gives the player the real placeable item.
        return item;
    }

    private ItemStack searchButton() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColorUtil.color("&bSearch furniture"));
        meta.setLore(List.of(ChatColorUtil.color("&7Click, then type a search term in chat")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColorUtil.color(name));
        item.setItemMeta(meta);
        return item;
    }

    public void openMain(Player player) {
        player.openInventory(buildMainMenu());
    }
}
