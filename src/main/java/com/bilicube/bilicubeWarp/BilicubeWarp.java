package com.bilicube.bilicubeWarp;

import com.bilicube.bilicubeWarp.command.WarpCommand;
import com.bilicube.bilicubeWarp.command.WarpTabCompleter;
import com.bilicube.bilicubeWarp.gui.LandmarkMenu;
import com.bilicube.bilicubeWarp.listener.MenuClickListener;
import com.bilicube.bilicubeWarp.listener.SignInteractionListener;
import com.bilicube.bilicubeWarp.manager.ConfigManager;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.manager.SignManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class BilicubeWarp extends JavaPlugin {

    private ConfigManager configManager;
    private LandmarkManager landmarkManager;
    private SignManager signManager;
    private LandmarkMenu landmarkMenu;
    private final Map<UUID, Map<String, PermissionAttachment>> attachments = new HashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs())
            getLogger().severe("Failed to create data folder");

        configManager = new ConfigManager(this);
        configManager.load();

        landmarkManager = new LandmarkManager(this);
        landmarkManager.load();

        signManager = new SignManager(this);
        signManager.load();

        landmarkMenu = new LandmarkMenu(this);

        WarpCommand wc = new WarpCommand(this);
        Objects.requireNonNull(getCommand("warp")).setExecutor(wc);
        Objects.requireNonNull(getCommand("warp")).setTabCompleter(new WarpTabCompleter(this));

        Bukkit.getPluginManager().registerEvents(new SignInteractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuClickListener(this), this);

        for (Player p : Bukkit.getOnlinePlayers()) refreshAllLandmarkPermissions(p);

        getLogger().info("BilicubeWarp enabled");
    }

    @Override
    public void onDisable() {
        if (landmarkManager != null) landmarkManager.save();
        if (signManager != null) signManager.save();
        Bukkit.getScheduler().cancelTasks(this);
        attachments.values().forEach(m -> m.values().forEach(PermissionAttachment::remove));
        attachments.clear();
        getLogger().info("BilicubeWarp disabled");
    }

    public void refreshAllLandmarkPermissions(Player player) {
        Map<String, PermissionAttachment> existing = attachments.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        for (var lm : landmarkManager.all()) {
            String perm = lm.getPermissionNode();
            boolean shouldHave = lm.isAdmin(player.getUniqueId());
            if (shouldHave && !existing.containsKey(lm.getName())) {
                PermissionAttachment att = player.addAttachment(this);
                att.setPermission(perm, true);
                existing.put(lm.getName(), att);
            } else if (!shouldHave) {
                PermissionAttachment att = existing.remove(lm.getName());
                if (att != null) att.remove();
            }
        }
    }

    public void updateLandmarkPermission(Player player, String landmarkName) {
        var lm = landmarkManager.get(landmarkName);
        Map<String, PermissionAttachment> existing = attachments.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        if (lm == null) { PermissionAttachment att = existing.remove(landmarkName); if (att != null) att.remove(); return; }
        boolean shouldHave = lm.isAdmin(player.getUniqueId());
        if (shouldHave && !existing.containsKey(landmarkName)) {
            PermissionAttachment att = player.addAttachment(this);
            att.setPermission(lm.getPermissionNode(), true);
            existing.put(landmarkName, att);
        } else if (!shouldHave) {
            PermissionAttachment att = existing.remove(landmarkName);
            if (att != null) att.remove();
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public LandmarkManager getLandmarkManager() { return landmarkManager; }
    public SignManager getSignManager() { return signManager; }
    public LandmarkMenu getLandmarkMenu() { return landmarkMenu; }
}
