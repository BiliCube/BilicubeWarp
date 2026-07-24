package com.bilicube.bilicubeWarp.task;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.manager.ConfigManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportTask extends BukkitRunnable {

    private final BilicubeWarp plugin;
    private final ConfigManager cfg;
    private final Player player;
    private final Landmark landmark;
    private final Location target;
    private final Location startLocation;
    private int remaining;

    public TeleportTask(BilicubeWarp plugin, Player player, Landmark landmark) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
        this.player = player;
        this.landmark = landmark;
        this.target = landmark.getLocation();
        this.startLocation = player.getLocation().clone();
        this.remaining = cfg.teleportDelaySeconds();
    }

    public void start() {
        runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        if (!player.isOnline()) { cancel(); return; }

        if (cfg.moveCancelsTeleport() && hasMoved()) {
            player.sendActionBar(MessageUtil.component(
                    cfg.msg("teleport-cancelled").replace('&', '§')));
            cancel();
            return;
        }

        if (remaining <= 0) {
            player.teleportAsync(target).thenAccept(success -> {
                if (success) {
                    Component welcome = landmark.getWelcomeComponent();
                    if (welcome != Component.empty()) {
                        player.sendMessage(welcome);
                    }
                }
            });
            cancel();
            return;
        }

        player.sendActionBar(MessageUtil.format(
                cfg.msg("teleport-start"), null,
                "{seconds}", String.valueOf(remaining)));
        remaining--;
    }

    private boolean hasMoved() {
        Location now = player.getLocation();
        if (!startLocation.getWorld().equals(now.getWorld())) return true;
        return startLocation.distanceSquared(now) > 0.25;
    }
}
