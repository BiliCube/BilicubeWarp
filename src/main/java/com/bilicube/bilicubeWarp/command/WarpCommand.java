package com.bilicube.bilicubeWarp.command;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.manager.ConfigManager;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.manager.SignManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.model.LandmarkLevel;
import com.bilicube.bilicubeWarp.task.TeleportTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.bilicube.bilicubeWarp.util.MessageUtil.*;

public class WarpCommand implements CommandExecutor {

    private final BilicubeWarp plugin;
    private final ConfigManager cfg;
    private final LandmarkManager lm;
    private final SignManager sm;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public WarpCommand(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
        this.lm = plugin.getLandmarkManager();
        this.sm = plugin.getSignManager();
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String l, String[] a) {
        if (a.length == 0) return menu(s);
        return switch (a[0].toLowerCase()) {
            case "menu"   -> menu(s);
            case "tp"     -> tp(s, a);
            case "list"   -> list(s);
            case "info"   -> info(s, a);
            case "set"    -> set(s, a);
            case "remove" -> remove(s, a);
            case "edit"   -> edit(s, a);
            case "owner"  -> owner(s, a);
            case "level"  -> levelCmd(s, a);
            case "member" -> member(s, a);
            case "admin"  -> adminCmd(s, a);
            case "open"   -> open(s, a);
            case "sign"   -> createSign(s, a);
            case "reload" -> reload(s);
            case "help"   -> help(s);
            default       -> menu(s);
        };
    }


    private boolean help(CommandSender s) {
        if (!hasPerm(s, "warp.use")) return true;
        String p = cfg.prefix();
        s.sendMessage(format(cfg.msg("help-header"), p));
        h(s, "menu", "打开地标菜单");
        h(s, "list", "列出所有地标");
        h(s, "info <内部名>", "查看详情");
        h(s, "tp <内部名>", "传送到地标（需 warp.tp）");
        h(s, "edit <内部名> displayname <显示名>", "设置显示名");
        h(s, "edit <内部名> desc <简介>", "设置简介（</br> 换行）");
        h(s, "edit <内部名> welcome <1|2> <文本>", "设置欢迎语");
        h(s, "edit <内部名> icon", "设置图标（手持物品）");
        h(s, "member add|remove|list <内部名> [玩家]", "管理成员");
        h(s, "admin add|remove|list <内部名> [玩家]", "管理地标管理员");
        if (hasPerm(s, "warp.admin")) {
            h(s, "set <内部名> <萤火|灯塔|晨星|极光>", "创建地标");
            h(s, "remove <内部名>", "删除地标");
            h(s, "level <内部名> <等级>", "修改等级");
            h(s, "owner <内部名> <玩家>", "分配所有者");
            h(s, "open <玩家>", "为玩家打开菜单");
            h(s, "sign <内部名>", "创建传送牌（需看着木牌）");
            h(s, "reload", "重载配置");
        }
        return true;
    }
    private void h(CommandSender s, String c, String d) {
        s.sendMessage(format(cfg.msg("help-item"), cfg.prefix(), "{cmd}", c, "{desc}", d));
    }


    private boolean menu(CommandSender s) {
        if (!requirePlayer(s) || !hasPerm(s, "warp.menu")) return true;
        plugin.getLandmarkMenu().open((Player) s);
        return true;
    }

    private boolean open(CommandSender s, String[] a) {
        if (!hasPerm(s, "warp.admin")) return true;
        if (a.length < 2) { raw(s, "&7用法: /warp open <玩家名>"); return true; }
        Player t = Bukkit.getPlayer(a[1]);
        if (t == null) { raw(s, "&c玩家不在线"); return true; }
        plugin.getLandmarkMenu().open(t);
        raw(s, "&a已为 &b" + t.getName() + " &a打开地标菜单");
        return true;
    }

    private boolean tp(CommandSender s, String[] a) {
        if (!requirePlayer(s) || !hasPerm(s, "warp.tp")) return true;
        if (a.length < 2) { raw(s, "&7用法: /warp tp <内部名>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        if (lm.getLocation() == null) { raw(s, "&c目标世界未加载"); return true; }
        new TeleportTask(plugin, (Player) s, lm).start();
        return true;
    }

    private boolean list(CommandSender s) {
        if (!hasPerm(s, "warp.list")) return true;
        var all = lm.all();
        String p = cfg.prefix();
        if (all.isEmpty()) { s.sendMessage(format("&7暂无地标", p)); return true; }
        s.sendMessage(format("&b===== 地标列表 (&e" + all.size() + "&b) =====", p));
        for (Landmark lm : all) {
            String owner = plugin.getServer().getOfflinePlayer(lm.getOwnerUuid()).getName();
            Location loc = lm.getLocation();
            s.sendMessage(component("  &e" + lm.getDisplayName() + " &7[&b" + lm.getLevel().getDisplayName()
                    + "&7] &8by " + owner + (loc != null ? " &8@ " + loc.getWorld().getName() : "")));
        }
        return true;
    }

    private boolean info(CommandSender s, String[] a) {
        if (!hasPerm(s, "warp.info")) return true;
        if (a.length < 2) { raw(s, "&7用法: /warp info <内部名>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        s.sendMessage(component("&b===== &e" + lm.getDisplayName() + " &b====="));
        s.sendMessage(component("&7内部名: &f" + lm.getName()));
        s.sendMessage(component("&7等级: &b" + lm.getLevel().getDisplayName() + " &7(" + lm.getLevel().getSubtitle() + ")"));
        String owner = plugin.getServer().getOfflinePlayer(lm.getOwnerUuid()).getName();
        s.sendMessage(component("&7所有者: &b" + (owner != null ? owner : "未知")));
        s.sendMessage(component("&7创建于: &b" + df.format(new Date(lm.getCreationDate()))));
        if (!lm.getDescription().isEmpty()) {
            s.sendMessage(component("&7简介:"));
            for (String line : lm.getDescription().split("</br>"))
                s.sendMessage(component("  &f" + line));
        }
        if (lm.getLocation() != null) {
            Location loc = lm.getLocation();
            s.sendMessage(component("&7位置: &b" + loc.getWorld().getName() + " &7(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
        }
        if (!lm.getAdmins().isEmpty()) {
            var names = lm.getAdmins().stream().map(u -> plugin.getServer().getOfflinePlayer(u).getName()).toList();
            s.sendMessage(component("&7地标管理员: &b" + String.join("&7, &b", names)));
        }
        if (!lm.getWelcomeLines().isEmpty()) {
            s.sendMessage(component("&7欢迎语:"));
            for (int i = 0; i < lm.getWelcomeLines().size(); i++)
                s.sendMessage(component("  &f" + (i + 1) + ". " + lm.getWelcomeLines().get(i)));
        }
        return true;
    }


    private boolean set(CommandSender s, String[] a) {
        if (!requirePlayer(s) || !hasPerm(s, "warp.admin")) return true;
        if (a.length < 3) { raw(s, "&7用法: /warp set <内部名> <萤火|灯塔|晨星|极光>"); return true; }
        String name = a[1].toLowerCase();
        if (!Landmark.isValidName(name)) { raw(s, "&c内部名只能包含小写字母、数字和下划线 (a-z0-9_)"); return true; }
        var lvl = LandmarkLevel.fromDisplayName(a[2]);
        if (lvl.isEmpty()) { msg(s, "invalid-level"); return true; }
        if (lm.get(name) != null) { msg(s, "landmark-exists", "{name}", name); return true; }
        lm.create(name, lvl.get(), ((Player) s).getLocation(), ((Player) s).getUniqueId());
        msg(s, "landmark-created", "{name}", name, "{level}", lvl.get().getDisplayName());
        return true;
    }

    private boolean remove(CommandSender s, String[] a) {
        if (!hasPerm(s, "warp.admin")) return true;
        if (a.length < 2) { raw(s, "&7用法: /warp remove <内部名>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        sm.removeAllForLandmark(lm.getId());
        this.lm.remove(a[1]);
        msg(s, "landmark-removed", "{name}", a[1]);
        return true;
    }


    private boolean edit(CommandSender s, String[] a) {
        if (a.length < 3) { raw(s, "&7用法: /warp edit <内部名> <displayname|desc|welcome|icon>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        return switch (a[2].toLowerCase()) {
            case "displayname" -> editDisplayName(s, lm, a);
            case "desc"        -> editDesc(s, lm, a);
            case "welcome"     -> editWelcome(s, lm, a);
            case "icon"        -> editIcon(s, lm);
            default -> { raw(s, "&c未知操作: " + a[2] + "，可选: displayname, desc, welcome, icon"); yield true; }
        };
    }

    private boolean editDisplayName(CommandSender s, Landmark lm, String[] a) {
        if (!requirePlayer(s) || !canManage(s, lm)) { msg(s, "no-permission"); return true; }
        if (a.length < 4) { raw(s, "&7用法: /warp edit " + lm.getName() + " displayname <显示名>"); return true; }
        StringBuilder sb = new StringBuilder(a[3]);
        for (int i = 4; i < a.length; i++) sb.append(" ").append(a[i]);
        this.lm.setDisplayName(lm.getName(), sb.toString());
        raw(s, "&a显示名已更新");
        return true;
    }

    private boolean editDesc(CommandSender s, Landmark lm, String[] a) {
        if (!requirePlayer(s) || !canManage(s, lm)) { msg(s, "no-permission"); return true; }
        if (a.length < 4) { raw(s, "&7用法: /warp edit " + lm.getName() + " desc <简介>"); return true; }
        StringBuilder sb = new StringBuilder(a[3]);
        for (int i = 4; i < a.length; i++) sb.append(" ").append(a[i]);
        String text = color(sb.toString());
        if (text.length() > Landmark.MAX_DESC_LENGTH)
            raw(s, "&e简介过长（" + text.length() + "/" + Landmark.MAX_DESC_LENGTH + "），已截断");
        this.lm.setDescription(lm.getName(), text);
        raw(s, "&a简介已更新");
        return true;
    }

    private boolean editWelcome(CommandSender s, Landmark lm, String[] a) {
        if (!requirePlayer(s) || !canManage(s, lm)) { msg(s, "no-permission"); return true; }
        int maxLines = lm.getLevel().getWelcomeLineCount();
        if (maxLines == 0) { raw(s, "&c萤火等级不支持欢迎语"); return true; }
        String hint = maxLines == 1 ? "1" : "1|" + maxLines;
        if (a.length < 5) { raw(s, "&7用法: /warp edit " + lm.getName() + " welcome <" + hint + "> <文本>"); return true; }
        int line;
        try { line = Integer.parseInt(a[3]); } catch (NumberFormatException e) { raw(s, "&c请输入 " + hint); return true; }
        if (line < 1 || line > maxLines) { raw(s, "&c该等级仅支持 " + maxLines + " 行欢迎语"); return true; }
        StringBuilder sb = new StringBuilder(a[4]);
        for (int i = 5; i < a.length; i++) sb.append(" ").append(a[i]);
        this.lm.setWelcome(lm.getName(), line - 1, sb.toString());
        msg(s, "welcome-set", "{name}", lm.getName(), "{line}", String.valueOf(line));
        return true;
    }

    private boolean editIcon(CommandSender s, Landmark lm) {
        if (!requirePlayer(s) || !canManage(s, lm)) { msg(s, "no-permission"); return true; }
        var held = ((Player) s).getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) { raw(s, "&7请手持一个物品"); return true; }
        this.lm.setIcon(lm.getName(), held.clone()); // 保留完整 NBT（含 CustomModelData）
        raw(s, "&a图标已更新为 &e" + held.getType().name());
        return true;
    }


    private boolean levelCmd(CommandSender s, String[] a) {
        if (!hasPerm(s, "warp.admin")) return true;
        if (a.length < 3) { raw(s, "&7用法: /warp level <内部名> <萤火|灯塔|晨星|极光>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        var lvl = LandmarkLevel.fromDisplayName(a[2]);
        if (lvl.isEmpty()) { msg(s, "invalid-level"); return true; }
        this.lm.setLevel(lm.getName(), lvl.get());
        msg(s, "landmark-level-changed", "{name}", lm.getName(), "{level}", lvl.get().getDisplayName());
        return true;
    }


    private boolean owner(CommandSender s, String[] a) {
        if (!hasPerm(s, "warp.admin")) return true;
        if (a.length < 3) { raw(s, "&7用法: /warp owner <内部名> <玩家>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        OfflinePlayer op = Bukkit.getOfflinePlayer(a[2]);
        if (!op.hasPlayedBefore() && !op.isOnline()) { raw(s, "&c玩家不存在"); return true; }
        this.lm.assignOwner(lm.getName(), op.getUniqueId());
        if (op.isOnline()) plugin.updateLandmarkPermission(op.getPlayer(), lm.getName());
        raw(s, "&a所有者已分配给 &b" + op.getName());
        return true;
    }


    private boolean member(CommandSender s, String[] a) {
        if (!requirePlayer(s)) return true;
        if (a.length < 3) { raw(s, "&7用法: /warp member <add|remove|list> <内部名> [玩家]"); return true; }
        return roleOp(s, a, false);
    }


    private boolean adminCmd(CommandSender s, String[] a) {
        if (!requirePlayer(s)) return true;
        if (a.length < 3) { raw(s, "&7用法: /warp admin <add|remove|list> <内部名> [玩家]"); return true; }
        return roleOp(s, a, true);
    }

    private boolean roleOp(CommandSender s, String[] a, boolean isAdmin) {
        String op = a[1].toLowerCase();
        Landmark lm = this.lm.get(a[2]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[2]); return true; }
        String role = isAdmin ? "管理员" : "成员";

        if (op.equals("list")) {
            if (!canManage(s, lm)) { msg(s, "no-permission"); return true; }
            var uuids = isAdmin ? lm.getAdmins() : lm.getMembers();
            if (uuids.isEmpty()) { raw(s, "&7该地标暂无" + role); return true; }
            raw(s, "&b" + role + "列表 (&e" + uuids.size() + "&b):");
            for (UUID uid : uuids) {
                String name = plugin.getServer().getOfflinePlayer(uid).getName();
                s.sendMessage(component("  &7- &f" + (name != null ? name : uid.toString())));
            }
            return true;
        }

        if (!op.equals("add") && !op.equals("remove")) { raw(s, "&7请使用 add、remove 或 list"); return true; }
        if (a.length < 4) { raw(s, "&7用法: /warp " + (isAdmin ? "admin" : "member") + " " + op + " <内部名> <玩家>"); return true; }

        if (isAdmin) {
            if (!lm.isOwner(((Player) s).getUniqueId()) && !hasPermRaw(s, "warp.admin")) { msg(s, "no-permission"); return true; }
        } else {
            if (!canManage(s, lm)) { msg(s, "no-permission"); return true; }
        }

        OfflinePlayer tp = Bukkit.getOfflinePlayer(a[3]);
        if (!tp.hasPlayedBefore() && !tp.isOnline()) { raw(s, "&c玩家不存在"); return true; }
        if (op.equals("add")) {
            if (isAdmin) { this.lm.addAdmin(lm.getName(), tp.getUniqueId()); if (tp.isOnline()) plugin.updateLandmarkPermission(tp.getPlayer(), lm.getName()); }
            else this.lm.addMember(lm.getName(), tp.getUniqueId());
        } else {
            if (isAdmin) { this.lm.removeAdmin(lm.getName(), tp.getUniqueId()); if (tp.isOnline()) plugin.updateLandmarkPermission(tp.getPlayer(), lm.getName()); }
            else this.lm.removeMember(lm.getName(), tp.getUniqueId());
        }
        raw(s, "&a已" + (op.equals("add") ? "添加" : "移除") + role + " &b" + tp.getName());
        return true;
    }


    private boolean createSign(CommandSender s, String[] a) {
        if (!requirePlayer(s) || !hasPerm(s, "warp.admin")) return true;
        if (a.length < 2) { raw(s, "&7用法: /warp sign <内部名>"); return true; }
        Landmark lm = this.lm.get(a[1]);
        if (lm == null) { msg(s, "landmark-not-found", "{name}", a[1]); return true; }
        var block = ((Player) s).getTargetBlockExact(5);
        if (block == null || !(block.getState() instanceof Sign sign)) { msg(s, "no-sign-target"); return true; }
        if (sm.get(block.getLocation()) != null) { msg(s, "sign-already-exists"); return true; }
        sm.create(block.getLocation(), lm.getId());
        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(0, Component.empty());
        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(1, component("&1&l【地标传送】"));
        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(2, component("&e" + lm.getDisplayName()));
        sign.getSide(org.bukkit.block.sign.Side.FRONT).line(3, Component.empty());
        sign.update();
        msg(s, "sign-created", "{name}", lm.getDisplayName());
        return true;
    }


    private boolean reload(CommandSender s) {
        if (!hasPerm(s, "warp.admin")) return true;
        cfg.reload();
        msg(s, "config-reloaded");
        return true;
    }


    private boolean canManage(CommandSender s, Landmark lm) {
        if (hasPermRaw(s, "warp.admin")) return true;
        return s instanceof Player p && lm.isAdmin(p.getUniqueId());
    }

    private boolean requirePlayer(CommandSender s) {
        if (!(s instanceof Player)) { s.sendMessage(format(cfg.msg("player-only"), cfg.prefix())); return false; }
        return true;
    }
    private boolean hasPerm(CommandSender s, String perm) {
        if (!s.hasPermission(perm)) { s.sendMessage(format(cfg.msg("no-permission"), cfg.prefix())); return false; }
        return true;
    }
    private boolean hasPermRaw(CommandSender s, String perm) { return s.hasPermission(perm); }
    private void msg(CommandSender s, String key, String... kvs) {
        s.sendMessage(format(cfg.msg(key), cfg.prefix(), kvs));
    }
    private void raw(CommandSender s, String text) {
        s.sendMessage(format(text, cfg.prefix()));
    }
}
