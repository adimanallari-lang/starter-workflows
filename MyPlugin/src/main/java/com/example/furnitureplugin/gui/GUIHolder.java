package com.example.furnitureplugin.gui;

import com.example.furnitureplugin.furniture.FurnitureCategory;
import com.example.furnitureplugin.furniture.FurnitureDefinition;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

/** Marks our inventories so GUIListener can safely ignore clicks in any
 *  other inventory (chests, other plugins' GUIs, etc). Also carries the
 *  state needed to interpret a click: which page/category/search we're on,
 *  and the exact list of definitions currently shown slot-by-slot. */
public final class GUIHolder implements InventoryHolder {

    public enum ViewType { MAIN, CATEGORY, SEARCH }

    private Inventory inventory;
    private final ViewType viewType;
    private final FurnitureCategory category; // null for MAIN/SEARCH
    private final String searchQuery;         // null unless SEARCH
    private final int page;
    private final List<FurnitureDefinition> shown; // slot -> definition, in display order

    public GUIHolder(ViewType viewType, FurnitureCategory category, String searchQuery,
                      int page, List<FurnitureDefinition> shown) {
        this.viewType = viewType;
        this.category = category;
        this.searchQuery = searchQuery;
        this.page = page;
        this.shown = shown;
    }

    @Override
    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public ViewType getViewType() { return viewType; }
    public FurnitureCategory getCategory() { return category; }
    public String getSearchQuery() { return searchQuery; }
    public int getPage() { return page; }
    public List<FurnitureDefinition> getShown() { return shown; }
}
