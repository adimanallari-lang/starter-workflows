package com.example.furnitureplugin.furniture;

import com.example.furnitureplugin.util.ChatColorUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Builds the physical ItemStack a player holds/places for a given furniture
 *  definition. This same custom-model-data item is what the Java resource
 *  pack re-skins into a full 3D model, and what the Bedrock resource pack /
 *  Geyser custom-item mapping re-skins into its attachable geometry. */
public final class FurnitureItemFactory {

    private final FurnitureKeys keys;

    public FurnitureItemFactory(FurnitureKeys keys) {
        this.keys = keys;
    }

    public ItemStack createItem(FurnitureDefinition def) {
        ItemStack item = new ItemStack(def.getBaseItem());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColorUtil.color(def.getDisplayName()));
        meta.setCustomModelData(def.getCustomModelData());
        meta.getPersistentDataContainer().set(keys.itemDefId, PersistentDataType.STRING, def.getId());
        meta.setLore(java.util.List.of(
                ChatColorUtil.color("&7Right-click a " +
                        (def.getPlacement() == PlacementType.WALL ? "wall" : "block") + " to place"),
                ChatColorUtil.color("&7Category: &f" + def.getCategory().name())
        ));
        item.setItemMeta(meta);
        return item;
    }

    public String getDefIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(keys.itemDefId, PersistentDataType.STRING);
    }
}
