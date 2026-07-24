package com.bilicube.bilicubeWarp.listener;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.gui.LandmarkMenu;
import com.bilicube.bilicubeWarp.manager.ConfigManager;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.task.TeleportTask;
import com.bilicube.bilicubeWarp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MenuClickListener implements Listener {

    private final BilicubeWarp plugin;
    private final LandmarkManager lm;
    private final LandmarkMenu menu;

    public MenuClickListener(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.lm = plugin.getLandmarkManager();
        this.menu = plugin.getLandmarkMenu();
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!isWarpMenu(e.getView().title())) return;
        e.setCancelled(true);

        ItemStack cur = e.getCurrentItem();
        if (cur == null) return;

        String action = menu.getAction(cur);
        if (action != null) { handleAction(p, action, e.getClick()); return; }

        String lmId = menu.getLandmarkId(cur);
        if (lmId != null) handleLandmark(p, lmId, e.getClick());
    }


    private void handleAction(Player p, String action, ClickType click) {
        switch (action) {
            case LandmarkMenu.A_CLOSE -> p.closeInventory();
            case LandmarkMenu.A_VIEW -> menu.cycleView(p, click.isLeftClick());
            case LandmarkMenu.A_FILTER -> menu.toggleFilter(p);
            case LandmarkMenu.A_PREV -> {
                int pg = menu.page(p); if (pg > 0) menu.open(p, pg - 1);
            }
            case LandmarkMenu.A_NEXT -> menu.open(p, menu.page(p) + 1);
        }
    }


    private void handleLandmark(Player p, String idStr, ClickType click) {
        UUID id = UUID.fromString(idStr);
        Landmark l = lm.get(id);
        if (l == null) { p.sendActionBar(MessageUtil.component("&c地标不存在")); return; }

        if (click.isLeftClick()) {
            if (!p.hasPermission("warp.tp.menu")) {
                p.sendActionBar(MessageUtil.component("&c你没有菜单传送权限")); return;
            }
            if (l.getLocation() == null) {
                p.sendActionBar(MessageUtil.component("&c目标世界未加载")); return;
            }
            p.closeInventory();
            new TeleportTask(plugin, p, l).start();
        } else if (click.isRightClick()) {
            showInfo(p, l);
        }
    }


    private void showInfo(Player p, Landmark l) {
        p.sendMessage(MessageUtil.component("&b======== &e" + l.getDisplayName() + " &b========"));
        if (!l.getDescription().isEmpty()) {
            for (String line : l.getDescription().split("</br>"))
                p.sendMessage(MessageUtil.component("&f" + line));
        }
        p.sendMessage(MessageUtil.component("&7等级: &b" + l.getLevel().getDisplayName() + " &7— &b" + l.getLevel().getSubtitle()));
        String owner = plugin.getServer().getOfflinePlayer(l.getOwnerUuid()).getName();
        p.sendMessage(MessageUtil.component("&7所有者: &b" + (owner != null ? owner : "未知")));
        if (l.getLocation() != null) {
            var loc = l.getLocation();
            p.sendMessage(MessageUtil.component("&7位置: &b" + loc.getWorld().getName() + " &7(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
        }
        if (!l.getWelcomeLines().isEmpty()) {
            p.sendMessage(MessageUtil.component("&7欢迎语:"));
            for (int i = 0; i < l.getWelcomeLines().size(); i++)
                p.sendMessage(MessageUtil.component("  &f" + (i + 1) + ". " + l.getWelcomeLines().get(i)));
        }
    }

    private boolean isWarpMenu(Component title) {
        return LegacyComponentSerializer.legacySection().serialize(title).contains("Bilicube 地标");
    }
}
