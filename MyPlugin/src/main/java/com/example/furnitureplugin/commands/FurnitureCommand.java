package com.example.furnitureplugin.commands;

import com.example.furnitureplugin.furniture.FurnitureCatalog;
import com.example.furnitureplugin.furniture.FurnitureDefinition;
import com.example.furnitureplugin.furniture.FurnitureItemFactory;
import com.example.furnitureplugin.gui.FurnitureGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class FurnitureCommand implements CommandExecutor {

    private final FurnitureGUI gui;
    private final FurnitureCatalog catalog;
    private final FurnitureItemFactory itemFactory;
    private final Runnable reloadCallback;

    public FurnitureCommand(FurnitureGUI gui, FurnitureCatalog catalog,
                             FurnitureItemFactory itemFactory, Runnable reloadCallback) {
        this.gui = gui;
        this.catalog = catalog;
        this.itemFactory = itemFactory;
        this.reloadCallback = reloadCallback;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            if (!player.hasPermission("furnitureplugin.use")) {
                player.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            gui.openMain(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("furnitureplugin.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }
                reloadCallback.run();
                sender.sendMessage(ChatColor.GREEN + "Furniture catalog reloaded.");
            }
            case "give" -> {
                if (!sender.hasPermission("furnitureplugin.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /furniture give <id> [player]");
                    return true;
                }
                FurnitureDefinition def = catalog.getById(args[1]);
                if (def == null) {
                    sender.sendMessage(ChatColor.RED + "Unknown furniture id: " + args[1]);
                    return true;
                }
                Player target = args.length >= 3
                        ? sender.getServer().getPlayer(args[2])
                        : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found / console must specify a player.");
                    return true;
                }
                target.getInventory().addItem(itemFactory.createItem(def));
                sender.sendMessage(ChatColor.GREEN + "Gave " + def.getDisplayName()
                        + ChatColor.GREEN + " to " + target.getName());
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /furniture [reload|give <id> [player]]");
        }
        return true;
    }
}
