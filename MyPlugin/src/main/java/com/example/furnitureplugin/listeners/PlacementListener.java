package com.example.furnitureplugin.listeners;

import com.example.furnitureplugin.furniture.FurnitureCatalog;
import com.example.furnitureplugin.furniture.FurnitureDefinition;
import com.example.furnitureplugin.furniture.FurnitureItemFactory;
import com.example.furnitureplugin.furniture.FurnitureManager;
import com.example.furnitureplugin.furniture.PlacementType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Turns a held furniture item into a placed entity, and lets players
 *  rotate (right-click, not sneaking) or remove (left-click / attack,
 *  while sneaking) furniture that's already placed. */
public final class PlacementListener implements org.bukkit.event.Listener {

    private final FurnitureCatalog catalog;
    private final FurnitureItemFactory itemFactory;
    private final FurnitureManager manager;

    public PlacementListener(FurnitureCatalog catalog, FurnitureItemFactory itemFactory, FurnitureManager manager) {
        this.catalog = catalog;
        this.itemFactory = itemFactory;
        this.manager = manager;
    }

    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack hand = event.getItem();
        String defId = itemFactory.getDefIdFromItem(hand);
        if (defId == null) return;

        FurnitureDefinition def = catalog.getById(defId);
        if (def == null) return;

        event.setCancelled(true);
        Block clicked = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        if (clicked == null || face == null) return;

        Location placeLoc;
        if (def.getPlacement() == PlacementType.WALL) {
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                player.sendMessage(org.bukkit.ChatColor.RED + def.getDisplayName()
                        + org.bukkit.ChatColor.RED + " must be placed on a wall.");
                return;
            }
            placeLoc = clicked.getRelative(face).getLocation().add(0.5, 0, 0.5);
        } else {
            placeLoc = clicked.getRelative(face).getLocation().add(0.5, 0, 0.5);
        }

        float yaw = def.getPlacement() == PlacementType.WALL ? 0f : player.getLocation().getYaw();
        manager.place(player, def, placeLoc, face, yaw);

        if (player.getGameMode() != GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
        }
    }

    @EventHandler
    public void onRotate(PlayerInteractEntityEvent event) {
        if (!manager.isFurniture(event.getRightClicked())) return;
        if (!(event.getPlayer()).isSneaking()) {
            event.setCancelled(true);
            manager.rotate(event.getRightClicked());
        }
    }

    @EventHandler
    public void onRemove(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!manager.isFurniture(event.getEntity())) return;

        event.setCancelled(true);
        if (!player.isSneaking()) {
            player.sendMessage(org.bukkit.ChatColor.YELLOW + "Sneak + hit to remove this piece.");
            return;
        }
        manager.remove(event.getEntity(), player, true);
    }
}
