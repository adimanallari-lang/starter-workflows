package com.example.furnitureplugin.furniture;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads furniture-defs.yml into memory and exposes lookups by id / category.
 * Re-run {@link #load()} to hot-reload the catalog (e.g. via /furniture reload).
 */
public final class FurnitureCatalog {

    private final Plugin plugin;
    private final Map<String, FurnitureDefinition> byId = new LinkedHashMap<>();
    private final Map<FurnitureCategory, List<FurnitureDefinition>> byCategory = new LinkedHashMap<>();
    private final Map<FurnitureCategory, String> categoryDisplayNames = new LinkedHashMap<>();

    public FurnitureCatalog(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        byId.clear();
        byCategory.clear();
        categoryDisplayNames.clear();
        for (FurnitureCategory cat : FurnitureCategory.values()) {
            byCategory.put(cat, new ArrayList<>());
        }

        File file = new File(plugin.getDataFolder(),
                plugin.getConfig().getString("furniture.definitions-file", "furniture-defs.yml"));
        if (!file.exists()) {
            plugin.saveResource("furniture-defs.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection categories = yaml.getConfigurationSection("categories");
        if (categories == null) {
            plugin.getLogger().warning("furniture-defs.yml has no 'categories' section - catalog will be empty.");
            return;
        }

        for (String catKey : categories.getKeys(false)) {
            FurnitureCategory category;
            try {
                category = FurnitureCategory.valueOf(catKey);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown furniture category in config: " + catKey);
                continue;
            }
            ConfigurationSection catSection = categories.getConfigurationSection(catKey);
            if (catSection == null) continue;

            categoryDisplayNames.put(category, catSection.getString("display-name", catKey));

            List<?> items = catSection.getList("items");
            if (items == null) continue;

            for (Object itemObj : items) {
                if (!(itemObj instanceof Map<?, ?> rawMap)) continue;
                try {
                    FurnitureDefinition def = parseDefinition(category, rawMap);
                    byId.put(def.getId(), def);
                    byCategory.get(category).add(def);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING,
                            "Failed to parse a furniture entry in category " + catKey, ex);
                }
            }
        }

        plugin.getLogger().info("Loaded " + byId.size() + " furniture definitions across "
                + categories.getKeys(false).size() + " categories.");
    }

    @SuppressWarnings("unchecked")
    private FurnitureDefinition parseDefinition(FurnitureCategory category, Map<?, ?> raw) {
        Map<String, Object> m = (Map<String, Object>) raw;
        String id = String.valueOf(m.get("id"));
        String displayName = String.valueOf(m.getOrDefault("display-name", id));
        Material baseItem = Material.matchMaterial(String.valueOf(m.getOrDefault("base-item", "PAPER")));
        if (baseItem == null) baseItem = Material.PAPER;
        int cmd = ((Number) m.getOrDefault("custom-model-data", 0)).intValue();
        String model = String.valueOf(m.get("model"));
        String bedrockGeo = String.valueOf(m.getOrDefault("bedrock-geometry", "geo.furnitureplugin." + id));
        PlacementType placement = PlacementType.valueOf(
                String.valueOf(m.getOrDefault("placement", "FLOOR")));
        List<?> size = (List<?>) m.getOrDefault("size", List.of(1, 1, 1));
        int sx = ((Number) size.get(0)).intValue();
        int sy = ((Number) size.get(1)).intValue();
        int sz = ((Number) size.get(2)).intValue();
        boolean rotatable = Boolean.TRUE.equals(m.getOrDefault("rotatable", true));

        return new FurnitureDefinition(id, displayName, category, baseItem, cmd, model,
                bedrockGeo, placement, sx, sy, sz, rotatable);
    }

    public FurnitureDefinition getById(String id) {
        return byId.get(id);
    }

    public List<FurnitureDefinition> getByCategory(FurnitureCategory category) {
        return byCategory.getOrDefault(category, List.of());
    }

    public List<FurnitureDefinition> search(String query) {
        String q = query.toLowerCase();
        List<FurnitureDefinition> out = new ArrayList<>();
        for (FurnitureDefinition def : byId.values()) {
            if (def.getId().toLowerCase().contains(q)
                    || stripColor(def.getDisplayName()).toLowerCase().contains(q)) {
                out.add(def);
            }
        }
        return out;
    }

    public String getCategoryDisplayName(FurnitureCategory category) {
        return categoryDisplayNames.getOrDefault(category, category.name());
    }

    public List<FurnitureDefinition> all() {
        return new ArrayList<>(byId.values());
    }

    private String stripColor(String s) {
        return s.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }
}
