package com.example.furnitureplugin;

import com.example.furnitureplugin.commands.FurnitureCommand;
import com.example.furnitureplugin.furniture.FurnitureCatalog;
import com.example.furnitureplugin.furniture.FurnitureItemFactory;
import com.example.furnitureplugin.furniture.FurnitureKeys;
import com.example.furnitureplugin.furniture.FurnitureManager;
import com.example.furnitureplugin.gui.ChatSearchListener;
import com.example.furnitureplugin.gui.FurnitureGUI;
import com.example.furnitureplugin.gui.GUIListener;
import com.example.furnitureplugin.listeners.PlacementListener;
import com.example.furnitureplugin.resourcepack.PackDeliveryListener;
import com.example.furnitureplugin.resourcepack.ResourcePackServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public final class FurniturePlugin extends JavaPlugin {

    private FurnitureCatalog catalog;
    private ResourcePackServer packServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), "furniture-defs.yml").exists()) {
            saveResource("furniture-defs.yml", false);
        }

        FurnitureKeys keys = new FurnitureKeys(this);
        FurnitureItemFactory itemFactory = new FurnitureItemFactory(keys);
        FurnitureManager manager = new FurnitureManager(this, keys, itemFactory);

        catalog = new FurnitureCatalog(this);
        catalog.load();

        FurnitureGUI gui = new FurnitureGUI(catalog, itemFactory);
        GUIListener guiListener = new GUIListener(this, gui);

        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(new ChatSearchListener(this, gui, guiListener), this);
        getServer().getPluginManager().registerEvents(new PlacementListener(catalog, itemFactory, manager), this);

        FurnitureCommand command = new FurnitureCommand(gui, catalog, itemFactory, catalog::load);
        getCommand("furniture").setExecutor(command);

        setupResourcePackDelivery();

        getLogger().info("FurniturePlugin enabled. " + catalog.all().size() + " furniture pieces loaded.");
        if (com.example.furnitureplugin.util.BedrockUtil.isFloodgateInstalled()) {
            getLogger().info("Floodgate detected - Bedrock players will be recognised automatically.");
        } else {
            getLogger().info("Floodgate not detected - running in Java-only mode. " +
                    "Install Geyser + Floodgate for Bedrock cross-play.");
        }
    }

    @Override
    public void onDisable() {
        if (packServer != null) packServer.stop();
    }

    private void setupResourcePackDelivery() {
        if (!getConfig().getBoolean("resource-pack.enabled", true)) return;

        File packZip = new File(getDataFolder(), "java-resourcepack.zip");
        if (!packZip.exists()) {
            try {
                saveResource("java-resourcepack.zip", false);
            } catch (IllegalArgumentException ex) {
                getLogger().warning("No bundled java-resourcepack.zip found in the jar yet. " +
                        "Place a built resource pack at " + packZip.getAbsolutePath() + " and restart.");
                return;
            }
        }

        packServer = new ResourcePackServer(this);
        String bind = getConfig().getString("resource-pack.bind-address", "0.0.0.0");
        int port = getConfig().getInt("resource-pack.port", 25566);
        try {
            packServer.start(packZip, bind, port);
        } catch (IOException e) {
            getLogger().severe("Could not start the resource pack HTTP server: " + e.getMessage());
            return;
        }

        String publicUrl = getConfig().getString("resource-pack.public-url", "");
        if (publicUrl == null || publicUrl.isBlank()) {
            publicUrl = "http://" + detectPublicIp() + ":" + port + "/pack.zip";
            getLogger().warning("resource-pack.public-url not set in config.yml; guessed " + publicUrl
                    + " -- set this explicitly if players are outside your LAN / behind NAT.");
        }

        boolean forceAccept = getConfig().getBoolean("resource-pack.force-accept", true);
        String prompt = getConfig().getString("resource-pack.prompt-message", "&6Please accept the resource pack.");
        getServer().getPluginManager().registerEvents(
                new PackDeliveryListener(this, packServer, publicUrl, forceAccept, prompt), this);
    }

    private String detectPublicIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                for (InetAddress addr : java.util.Collections.list(interfaces.nextElement().getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) { }
        return "127.0.0.1";
    }

    public FurnitureCatalog getCatalog() {
        return catalog;
    }
}
