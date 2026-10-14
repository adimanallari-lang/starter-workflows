package com.example.furnitureplugin.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A tiny embedded HTTP server that serves this plugin's Java resource pack
 * zip directly from the plugin, so no external file host (Dropbox, a CDN,
 * etc.) is required. The server must still be reachable from players'
 * clients on the configured port -- forward it through your router/firewall
 * exactly like the main Minecraft port if players are outside your LAN.
 */
public final class ResourcePackServer {

    private final Plugin plugin;
    private HttpServer server;
    private String sha1Hex;
    private byte[] packBytes;

    public ResourcePackServer(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start(File packZip, String bindAddress, int port) throws IOException {
        if (!packZip.exists()) {
            throw new IOException("Resource pack zip not found at " + packZip.getAbsolutePath());
        }
        packBytes = Files.readAllBytes(packZip.toPath());
        sha1Hex = sha1Hex(packBytes);

        server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        server.createContext("/pack.zip", this::handle);
        server.setExecutor(null);
        server.start();
        plugin.getLogger().info("Resource pack server listening on " + bindAddress + ":" + port
                + " (sha1=" + sha1Hex + ")");
    }

    private void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/zip");
        exchange.sendResponseHeaders(200, packBytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(packBytes);
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public String getSha1Hex() {
        return sha1Hex;
    }

    public byte[] getSha1Bytes() {
        byte[] out = new byte[20];
        for (int i = 0; i < 20; i++) {
            out[i] = (byte) Integer.parseInt(sha1Hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    private static String sha1Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
