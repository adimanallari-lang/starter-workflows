package com.example.furnitureplugin.furniture;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;

import java.util.UUID;

/**
 * Spawns, removes and rotates furniture.
 *
 * Design note: every placed furniture piece is a REAL Bukkit entity
 * (ArmorStand for floor/tabletop pieces, ItemFrame for wall pieces) holding a
 * custom-model-data item. Minecraft's own chunk persistence saves these
 * entities to disk exactly like any player-placed armor stand or item frame,
 * so furniture survives restarts with no custom save/load code required.
 * Using only vanilla entity types (rather than 1.19.4+ Display entities,
 * which Bedrock/Geyser cannot render) is what guarantees Bedrock players see
 * the same furniture as Java players, at the cost of using Minecraft's
 * "held/framed item" rendering instead of free-form display-entity geometry.
 */
public final class FurnitureManager {

    private final Plugin plugin;
    private final FurnitureKeys keys;
    private final FurnitureItemFactory itemFactory;

    public FurnitureManager(Plugin plugin, FurnitureKeys keys, FurnitureItemFactory itemFactory) {
        this.plugin = plugin;
        this.keys = keys;
        this.itemFactory = itemFactory;
    }

    public Entity place(Player placer, FurnitureDefinition def, Location at, BlockFace against, float yaw) {
        if (def.getPlacement() == PlacementType.WALL) {
            return placeAsItemFrame(placer, def, at, against);
        }
        return placeAsArmorStand(placer, def, at, yaw);
    }

    private Entity placeAsArmorStand(Player placer, FurnitureDefinition def, Location at, float yaw) {
        Location loc = at.clone();
        loc.setYaw(yaw);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);          // hide the stand's own body; only the held item shows
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setBasePlate(false);
        stand.setArms(true);
        stand.setMarker(false);           // keep a hitbox so players can click it to rotate/remove
        stand.setSmall(def.getSizeY() <= 1);
        stand.setCollidable(false);
        stand.setCustomNameVisible(false);
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-10), 0, 0));

        ItemStack displayItem = itemFactory.createItem(def);
        stand.getEquipment().setItem(EquipmentSlot.HAND, displayItem);

        tagEntity(stand, def, placer);
        return stand;
    }

    private Entity placeAsItemFrame(Player placer, FurnitureDefinition def, Location at, BlockFace against) {
        ItemFrame frame = (ItemFrame) at.getWorld().spawn(at, ItemFrame.class, f -> {
            f.setFacingDirection(against == null ? BlockFace.SOUTH : against, true);
            f.setVisible(true); // keep frame border; set false for a borderless look if you prefer
            f.setFixed(false);
            f.setInvulnerable(true);
            f.setSilent(true);
        });
        ItemStack displayItem = itemFactory.createItem(def);
        frame.setItem(displayItem, false);
        tagEntity(frame, def, placer);
        return frame;
    }

    private void tagEntity(Entity entity, FurnitureDefinition def, Player placer) {
        entity.getPersistentDataContainer().set(keys.entityDefId, PersistentDataType.STRING, def.getId());
        entity.getPersistentDataContainer().set(keys.entityOwner, PersistentDataType.STRING,
                placer.getUniqueId().toString());
    }

    public boolean isFurniture(Entity entity) {
        return entity != null && entity.getPersistentDataContainer().has(keys.entityDefId, PersistentDataType.STRING);
    }

    public String getDefId(Entity entity) {
        return entity.getPersistentDataContainer().get(keys.entityDefId, PersistentDataType.STRING);
    }

    public UUID getOwner(Entity entity) {
        String s = entity.getPersistentDataContainer().get(keys.entityOwner, PersistentDataType.STRING);
        return s == null ? null : UUID.fromString(s);
    }

    /** Rotates a placed piece by 45 degrees (8-step) around its Y axis. */
    public void rotate(Entity entity) {
        if (entity instanceof ArmorStand stand) {
            Location loc = stand.getLocation();
            loc.setYaw(loc.getYaw() + 45f);
            stand.teleport(loc);
        } else if (entity instanceof ItemFrame frame) {
            frame.setRotation(frame.getRotation().rotateClockwise());
        }
    }

    /** Removes a placed piece, optionally returning its item to the given player's inventory. */
    public void remove(Entity entity, Player returnTo, boolean dropItem) {
        ItemStack heldItem = null;
        if (entity instanceof ArmorStand stand) {
            heldItem = stand.getEquipment().getItem(EquipmentSlot.HAND);
        } else if (entity instanceof ItemFrame frame) {
            heldItem = frame.getItem();
        }
        entity.remove();

        if (dropItem && heldItem != null && heldItem.getType() != org.bukkit.Material.AIR) {
            ItemStack single = heldItem.clone();
            single.setAmount(1);
            if (returnTo != null) {
                returnTo.getInventory().addItem(single);
            }
        }
    }
}
