package com.bilicube.bilicubeWarp.listener;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.manager.SignManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.model.TeleportSignData;
import com.bilicube.bilicubeWarp.task.TeleportTask;
import com.bilicube.bilicubeWarp.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignInteractionListener implements Listener {

    private final BilicubeWarp plugin;
    private final LandmarkManager lm;
    private final SignManager sm;

    public SignInteractionListener(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.lm = plugin.getLandmarkManager();
        this.sm = plugin.getSignManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;

        TeleportSignData sd = sm.get(e.getClickedBlock().getLocation());
        if (sd == null) return;

        if (!e.getPlayer().hasPermission("warp.tp.sign")) {
            e.getPlayer().sendActionBar(MessageUtil.component("&c你没有木牌传送权限"));
            return;
        }

        Landmark l = lm.get(sd.getLandmarkId());
        if (l == null) { e.getPlayer().sendActionBar(MessageUtil.component("&c地标不存在")); return; }
        if (l.getLocation() == null) { e.getPlayer().sendActionBar(MessageUtil.component("&c目标世界未加载")); return; }

        e.setCancelled(true);
        new TeleportTask(plugin, e.getPlayer(), l).start();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Location broken = e.getBlock().getLocation();

        if (e.getBlock().getState() instanceof Sign && sm.get(broken) != null) {
            if (!e.getPlayer().hasPermission("warp.admin")) {
                e.setCancelled(true);
                e.getPlayer().sendActionBar(MessageUtil.component("&c你不能破坏传送牌"));
                return;
            }
            sm.remove(broken);
            return;
        }

        for (TeleportSignData sd : sm.all()) {
            Location sl = sd.getLocation();
            if (sl == null || !sl.getWorld().equals(broken.getWorld())) continue;
            Location support = getSupport(sl);
            if (support != null && support.equals(broken)) {
                if (!e.getPlayer().hasPermission("warp.admin")) {
                    e.setCancelled(true);
                    e.getPlayer().sendActionBar(MessageUtil.component("&c你不能破坏传送牌的附着方块"));
                    return;
                }
                sm.remove(sl);
            }
        }
    }

    private Location getSupport(Location signLoc) {
        if (!(signLoc.getBlock().getState() instanceof Sign)) return null;
        if (signLoc.getBlock().getBlockData() instanceof WallSign ws)
            return signLoc.clone().add(ws.getFacing().getOppositeFace().getDirection());
        return signLoc.clone().subtract(0, 1, 0);
    }
}
