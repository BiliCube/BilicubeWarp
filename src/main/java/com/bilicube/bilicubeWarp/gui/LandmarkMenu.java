package com.bilicube.bilicubeWarp.gui;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.manager.ConfigManager;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.model.LandmarkLevel;
import com.bilicube.bilicubeWarp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class LandmarkMenu {

    private final BilicubeWarp plugin;
    private final ConfigManager cfg;
    private final LandmarkManager lm;
    private final NamespacedKey landmarkKey;
    private final NamespacedKey actionKey;

    public static final String A_CLOSE = "close";
    public static final String A_VIEW = "view_mode";
    public static final String A_FILTER = "filter";
    public static final String A_PREV = "prev";
    public static final String A_NEXT = "next";

    public static final int V_ALL = 0;
    public static final int V_FIREFLY = 1;
    public static final int V_LIGHTHOUSE = 2;
    public static final int V_MORNING_STAR = 3;
    public static final int V_AURORA = 4;
    private static final int V_COUNT = 5;

    private static final int ROWS = 6;
    private static final int PER_PAGE = 21;
    private static final int MAX_DESC_LINES = 5;

    private final Map<UUID, Integer> viewModes = new HashMap<>();
    private final Map<UUID, Integer> pages = new HashMap<>();
    private final Map<UUID, Boolean> localOnly = new HashMap<>();

    public LandmarkMenu(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
        this.lm = plugin.getLandmarkManager();
        this.landmarkKey = new NamespacedKey(plugin, "landmark-uuid");
        this.actionKey = new NamespacedKey(plugin, "action");
    }

    public void open(Player p) { open(p, 0); }

    public void open(Player p, int page) { pages.put(p.getUniqueId(), page); show(p); }

    public void cycleView(Player p, boolean fwd) {
        int cur = viewModes.getOrDefault(p.getUniqueId(), V_ALL);
        viewModes.put(p.getUniqueId(), fwd ? (cur + 1) % V_COUNT : (cur - 1 + V_COUNT) % V_COUNT);
        pages.put(p.getUniqueId(), 0);
        show(p);
    }

    public void toggleFilter(Player p) {
        localOnly.put(p.getUniqueId(), !localOnly.getOrDefault(p.getUniqueId(), false));
        pages.put(p.getUniqueId(), 0);
        show(p);
    }

    public int page(Player p) { return pages.getOrDefault(p.getUniqueId(), 0); }

    private void show(Player p) {
        UUID id = p.getUniqueId();
        int mode = viewModes.getOrDefault(id, V_ALL);
        int pg = pages.getOrDefault(id, 0);
        boolean local = localOnly.getOrDefault(id, false);

        Inventory inv = Bukkit.createInventory(null, ROWS * 9,
                MessageUtil.component("&8Bilicube 地标传送"));

        List<Landmark> list = lm.all().stream()
                .filter(l -> {
                    if (mode == V_FIREFLY && l.getLevel() != LandmarkLevel.FIREFLY) return false;
                    if (mode == V_LIGHTHOUSE && l.getLevel() != LandmarkLevel.LIGHTHOUSE) return false;
                    if (mode == V_MORNING_STAR && l.getLevel() != LandmarkLevel.MORNING_STAR) return false;
                    if (mode == V_AURORA && l.getLevel() != LandmarkLevel.AURORA) return false;
                    if (local) { Location loc = l.getLocation(); if (loc == null || !loc.getWorld().equals(p.getWorld())) return false; }
                    return true;
                })
                .sorted(Comparator.comparingInt(l -> l.getLevel().getId()))
                .collect(Collectors.toList());

        int total = Math.max(1, (int) Math.ceil((double) list.size() / PER_PAGE));
        if (pg >= total) pg = total - 1;
        if (pg < 0) pg = 0;
        pages.put(id, pg);

        int start = pg * PER_PAGE;
        int end = Math.min(start + PER_PAGE, list.size());

        ItemStack border = border();
        for (int s = 9; s <= 17; s++) inv.setItem(s, border);
        for (int r : new int[]{18, 26, 27, 35, 36, 44}) inv.setItem(r, border);
        for (int s = 45; s <= 53; s++) inv.setItem(s, border);

        int[] slots = {19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        for (int i = start, si = 0; i < end && si < slots.length; i++, si++)
            inv.setItem(slots[si], landmarkItem(list.get(i)));

        inv.setItem(0, ctrl(Material.NETHER_STAR, "&c关闭", A_CLOSE));
        inv.setItem(2, viewItem(mode));
        inv.setItem(3, filterItem(local));

        inv.setItem(6, nav(pg > 0, "上一页", A_PREV));
        inv.setItem(7, pageInfo(pg, total));
        inv.setItem(8, nav(pg < total - 1, "下一页", A_NEXT));

        p.openInventory(inv);
    }

    private ItemStack landmarkItem(Landmark l) {
        ItemStack item = l.getIcon().clone();
        ItemMeta m = item.getItemMeta();

        m.displayName(MessageUtil.component("&e" + l.getDisplayName()
                + " &7[&b" + l.getLevel().getDisplayName() + "&7]"));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (!l.getDescription().isEmpty()) {
            List<String> w = wrap(l.getDescription());
            int n = Math.min(w.size(), MAX_DESC_LINES);
            for (int i = 0; i < n; i++) lore.add(MessageUtil.component("&f" + w.get(i)));
            if (w.size() > MAX_DESC_LINES)
                lore.add(MessageUtil.component("&8... 更多 /warp info"));
            lore.add(Component.empty());
        }

        Location loc = l.getLocation();
        if (loc != null)
            lore.add(MessageUtil.component("&8" + loc.getWorld().getName() + " "
                    + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()));

        String owner = plugin.getServer().getOfflinePlayer(l.getOwnerUuid()).getName();
        lore.add(MessageUtil.component("&7所有者: &b" + (owner != null ? owner : "未知")));

        lore.add(MessageUtil.component("&7等级: &b" + l.getLevel().getDisplayName()
                + " &7— &b" + l.getLevel().getSubtitle()));

        lore.add(Component.empty());
        lore.add(MessageUtil.component("&7左键传送 | 右键详情"));

        m.lore(lore);
        m.getPersistentDataContainer().set(landmarkKey, PersistentDataType.STRING, l.getId().toString());
        item.setItemMeta(m);
        return item;
    }

    private ItemStack viewItem(int mode) {
        return switch (mode) {
            case V_ALL -> ctrl(Material.SUGAR, "&a全部地标", A_VIEW, "&7左键/右键切换视图");
            case V_FIREFLY -> ctrl(Material.TORCH, "&e仅萤火", A_VIEW, "&7左键/右键切换视图");
            case V_LIGHTHOUSE -> ctrl(Material.LANTERN, "&e仅灯塔", A_VIEW, "&7左键/右键切换视图");
            case V_MORNING_STAR -> ctrl(Material.NETHER_STAR, "&e仅晨星", A_VIEW, "&7左键/右键切换视图");
            default -> ctrl(Material.END_CRYSTAL, "&e仅极光", A_VIEW, "&7左键/右键切换视图");
        };
    }

    private ItemStack filterItem(boolean local) {
        return local
                ? ctrl(Material.BUCKET, "&a仅当前世界", A_FILTER, "&7点击显示全部世界")
                : ctrl(Material.WATER_BUCKET, "&a全部世界", A_FILTER, "&7点击仅显示当前世界");
    }

    private ItemStack nav(boolean enabled, String label, String action) {
        return ctrl(enabled ? Material.SPECTRAL_ARROW : Material.ARROW,
                (enabled ? "&a" : "&8") + label, action);
    }

    private ItemStack pageInfo(int pg, int total) {
        return ctrl(Material.PAPER, "&e第 " + (pg + 1) + " 页 / 共 " + total + " 页", null);
    }

    private ItemStack border() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta();
        m.displayName(Component.text(" "));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack ctrl(Material mat, String name, String action) {
        return ctrl(mat, name, action, null);
    }

    private ItemStack ctrl(Material mat, String name, String action, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.displayName(MessageUtil.component(name));
        if (lore != null) m.lore(List.of(MessageUtil.component(lore)));
        if (action != null) m.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        item.setItemMeta(m);
        return item;
    }

    public String getLandmarkId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(landmarkKey, PersistentDataType.STRING);
    }

    public String getAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
    }

    private List<String> wrap(String text) {
        List<String> out = new ArrayList<>();
        for (String para : text.split("</br>")) {
            if (!para.isEmpty()) out.add(para);
        }
        return out.isEmpty() ? List.of(text) : out;
    }
}
