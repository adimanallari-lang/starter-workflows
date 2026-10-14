package com.example.furnitureplugin.furniture;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/** Central registry of the PersistentDataContainer keys used to tag
 *  both placed furniture entities and the "furniture item" players hold
 *  before placing it. Tagging is how we tell real furniture apart from
 *  any other armor stand / item frame a player places normally. */
public final class FurnitureKeys {

    public final NamespacedKey entityDefId;   // on placed entities: which definition this is
    public final NamespacedKey entityOwner;   // on placed entities: placer's UUID string
    public final NamespacedKey itemDefId;     // on hand-held "furniture items": which definition

    public FurnitureKeys(Plugin plugin) {
        this.entityDefId = new NamespacedKey(plugin, "furniture_def_id");
        this.entityOwner = new NamespacedKey(plugin, "furniture_owner");
        this.itemDefId = new NamespacedKey(plugin, "furniture_item_def_id");
    }
}
