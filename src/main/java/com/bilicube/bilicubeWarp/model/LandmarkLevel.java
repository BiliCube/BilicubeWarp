package com.bilicube.bilicubeWarp.model;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Optional;

public enum LandmarkLevel {

    FIREFLY(0, "萤火", "微光聚落", Material.TORCH, 0),
    LIGHTHOUSE(1, "灯塔", "航标重镇", Material.LANTERN, 1),
    MORNING_STAR(2, "晨星", "黎明之城", Material.NETHER_STAR, 2),
    AURORA(3, "极光", "传说之地", Material.END_CRYSTAL, 2);

    private final int id;
    private final String displayName;
    private final String subtitle;
    private final Material defaultIcon;
    private final int welcomeLineCount;

    LandmarkLevel(int id, String displayName, String subtitle, Material defaultIcon, int welcomeLineCount) {
        this.id = id;
        this.displayName = displayName;
        this.subtitle = subtitle;
        this.defaultIcon = defaultIcon;
        this.welcomeLineCount = welcomeLineCount;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getSubtitle() { return subtitle; }
    public Material getDefaultIcon() { return defaultIcon; }
    public int getWelcomeLineCount() { return welcomeLineCount; }

    public static Optional<LandmarkLevel> fromDisplayName(String name) {
        return Arrays.stream(values()).filter(l -> l.displayName.equals(name)).findFirst();
    }

    public static Optional<LandmarkLevel> fromId(int id) {
        return Arrays.stream(values()).filter(l -> l.id == id).findFirst();
    }
}
