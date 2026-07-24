package com.bilicube.bilicubeWarp.manager;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.model.TeleportSignData;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignManager {

    private final BilicubeWarp plugin;
    private final File file;
    private final ConcurrentHashMap<UUID, TeleportSignData> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TeleportSignData> byLocation = new ConcurrentHashMap<>();

    public SignManager(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "signs.yml");
    }

    public TeleportSignData create(Location location, UUID landmarkId) {
        TeleportSignData sign = TeleportSignData.create(location, landmarkId);
        byId.put(sign.getId(), sign);
        byLocation.put(locKey(location), sign);
        save();
        return sign;
    }

    public boolean remove(Location location) {
        TeleportSignData sign = byLocation.remove(locKey(location));
        if (sign == null) return false;
        byId.remove(sign.getId());
        save();
        return true;
    }

    public TeleportSignData get(Location location) {
        return byLocation.get(locKey(location));
    }

    public Collection<TeleportSignData> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public void removeAllForLandmark(UUID landmarkId) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, TeleportSignData> e : byLocation.entrySet()) {
            if (e.getValue().getLandmarkId().equals(landmarkId)) toRemove.add(e.getKey());
        }
        for (String key : toRemove) {
            TeleportSignData sign = byLocation.remove(key);
            if (sign != null) byId.remove(sign.getId());
        }
        if (!toRemove.isEmpty()) save();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        byId.clear();
        byLocation.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<Map<String, Object>> list = (List<Map<String, Object>>) yaml.getList("signs");
        if (list == null) return;
        for (Map<String, Object> entry : list) {
            try {
                TeleportSignData sign = TeleportSignData.deserialize(entry);
                byId.put(sign.getId(), sign);
                byLocation.put(locKey(sign.getWorldId(), sign.getX(), sign.getY(), sign.getZ()), sign);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load sign: " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + byId.size() + " teleport signs");
    }

    public void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TeleportSignData sign : byId.values()) list.add(sign.serialize());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs", list);
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().severe("Failed to save signs: " + e.getMessage()); }
    }

    private static String locKey(Location loc) {
        return locKey(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static String locKey(UUID worldId, int x, int y, int z) {
        return worldId + ":" + x + ":" + y + ":" + z;
    }
}
