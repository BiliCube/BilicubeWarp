package com.bilicube.bilicubeWarp.manager;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void reload() { load(); }


    public int teleportDelaySeconds()   { return config.getInt("settings.teleport-delay-seconds", 3); }
    public boolean moveCancelsTeleport() { return config.getBoolean("settings.teleport-move-cancels", true); }
    public int maxLandmarksPerPlayer()   { return config.getInt("settings.max-landmarks-per-player", 3); }


    public String prefix()        { return config.getString("messages.prefix", "&8[&b地标&8] &r"); }
    public String msg(String key) { return config.getString("messages." + key, ""); }
}
