package com.example.furnitureplugin.util;

import org.bukkit.ChatColor;

public final class ChatColorUtil {
    private ChatColorUtil() {}

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
