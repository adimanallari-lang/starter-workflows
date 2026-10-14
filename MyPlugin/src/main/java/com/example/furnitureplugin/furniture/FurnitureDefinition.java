package com.example.furnitureplugin.furniture;

import org.bukkit.Material;

public final class FurnitureDefinition {

    private final String id;
    private final String displayName;
    private final FurnitureCategory category;
    private final Material baseItem;
    private final int customModelData;
    private final String modelPath;
    private final String bedrockGeometryId;
    private final PlacementType placement;
    private final int sizeX, sizeY, sizeZ;
    private final boolean rotatable;

    public FurnitureDefinition(String id, String displayName, FurnitureCategory category,
                               Material baseItem, int customModelData, String modelPath,
                               String bedrockGeometryId, PlacementType placement,
                               int sizeX, int sizeY, int sizeZ, boolean rotatable) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.baseItem = baseItem;
        this.customModelData = customModelData;
        this.modelPath = modelPath;
        this.bedrockGeometryId = bedrockGeometryId;
        this.placement = placement;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.rotatable = rotatable;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public FurnitureCategory getCategory() { return category; }
    public Material getBaseItem() { return baseItem; }
    public int getCustomModelData() { return customModelData; }
    public String getModelPath() { return modelPath; }
    public String getBedrockGeometryId() { return bedrockGeometryId; }
    public PlacementType getPlacement() { return placement; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
    public boolean isRotatable() { return rotatable; }
}
