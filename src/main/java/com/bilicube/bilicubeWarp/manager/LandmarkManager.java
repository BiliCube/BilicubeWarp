package com.bilicube.bilicubeWarp.manager;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.model.LandmarkLevel;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LandmarkManager {

    private final BilicubeWarp plugin;
    private final File file;
    private final ConcurrentHashMap<UUID, Landmark> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Landmark> byName = new ConcurrentHashMap<>();

    public LandmarkManager(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "landmarks.yml");
    }


    public Landmark create(String name, LandmarkLevel level, Location location, UUID owner) {
        Landmark landmark = Landmark.create(name.toLowerCase(), level, location, owner);
        byId.put(landmark.getId(), landmark);
        byName.put(landmark.getName(), landmark);
        save();
        return landmark;
    }

    public boolean remove(String name) {
        Landmark lm = byName.remove(name.toLowerCase());
        if (lm == null) return false;
        byId.remove(lm.getId());
        save();
        return true;
    }

    public Landmark get(String name) { return byName.get(name.toLowerCase()); }
    public Landmark get(UUID id) { return byId.get(id); }
    public Collection<Landmark> all() { return Collections.unmodifiableCollection(byId.values()); }


    public void assignOwner(String name, UUID newOwner) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setOwnerUuid(newOwner);
        save();
    }

    public void setLevel(String name, LandmarkLevel level) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setLevel(level);
        save();
    }

    public void setDescription(String name, String desc) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setDescription(desc);
        save();
    }

    public void setDisplayName(String name, String displayName) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setDisplayName(displayName);
        save();
    }

    public void setIcon(String name, ItemStack icon) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setIcon(icon);
        save();
    }

    public void setWelcome(String name, int line, String text) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setWelcomeLine(line, text);
        save();
    }

    public void rename(String oldName, String newName) {
        Landmark lm = byName.remove(oldName.toLowerCase());
        if (lm == null) return;
        lm.setName(newName.toLowerCase());
        byName.put(lm.getName(), lm);
        save();
    }

    public void move(String name, Location location) {
        Landmark lm = get(name);
        if (lm == null) return;
        lm.setLocation(location);
        save();
    }


    public boolean addAdmin(String name, UUID uuid) {
        Landmark lm = get(name);
        if (lm == null) return false;
        boolean result = lm.addAdmin(uuid);
        if (result) save();
        return result;
    }

    public boolean removeAdmin(String name, UUID uuid) {
        Landmark lm = get(name);
        if (lm == null) return false;
        boolean result = lm.removeAdmin(uuid);
        if (result) save();
        return result;
    }

    public boolean addMember(String name, UUID uuid) {
        Landmark lm = get(name);
        if (lm == null) return false;
        boolean result = lm.addMember(uuid);
        if (result) save();
        return result;
    }

    public boolean removeMember(String name, UUID uuid) {
        Landmark lm = get(name);
        if (lm == null) return false;
        boolean result = lm.removeMember(uuid);
        if (result) save();
        return result;
    }


    @SuppressWarnings("unchecked")
    public void load() {
        byId.clear();
        byName.clear();
        if (!file.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<Map<String, Object>> list = (List<Map<String, Object>>) yaml.getList("landmarks");
        if (list == null) return;

        for (Map<String, Object> entry : list) {
            try {
                Landmark lm = Landmark.deserialize(entry);
                byId.put(lm.getId(), lm);
                byName.put(lm.getName(), lm);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load landmark: " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + byId.size() + " landmarks");
    }

    public void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Landmark lm : byId.values()) list.add(lm.serialize());
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("landmarks", list);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save landmarks: " + e.getMessage());
        }
    }
}
