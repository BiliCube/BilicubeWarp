package com.bilicube.bilicubeWarp.command;

import com.bilicube.bilicubeWarp.BilicubeWarp;
import com.bilicube.bilicubeWarp.manager.LandmarkManager;
import com.bilicube.bilicubeWarp.model.Landmark;
import com.bilicube.bilicubeWarp.model.LandmarkLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class WarpTabCompleter implements TabCompleter {

    private final BilicubeWarp plugin;
    private final LandmarkManager lm;
    private static final List<String> SUB = List.of("menu","tp","list","info","set","remove","edit","owner","level","member","admin","open","sign","reload","help");
    private static final List<String> EDIT_OPS = List.of("displayname","desc","welcome","icon");
    private static final List<String> ADD_REM = List.of("add","remove","list");
    private static final List<String> LEVELS = Arrays.stream(LandmarkLevel.values()).map(LandmarkLevel::getDisplayName).toList();
    private static final List<String> LINES = List.of("1","2");

    public WarpTabCompleter(BilicubeWarp plugin) {
        this.plugin = plugin;
        this.lm = plugin.getLandmarkManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String l, String[] a) {
        List<String> c = new ArrayList<>();
        if (a.length == 1) { c.addAll(m(a[0], SUB)); return c; }
        switch (a[0].toLowerCase()) {
            case "tp","info" -> { if (a.length == 2) c.addAll(m(a[1], names())); }
            case "remove","sign" -> { if (a.length == 2 && s.hasPermission("warp.admin")) c.addAll(m(a[1], names())); }
            case "open" -> { if (a.length == 2) c.addAll(m(a[1], players())); }
            case "set" -> { if (a.length == 3) c.addAll(m(a[2], LEVELS)); }
            case "owner" -> { if (a.length == 2 && s.hasPermission("warp.admin")) c.addAll(m(a[1], names())); if (a.length == 3) c.addAll(m(a[2], players())); }
            case "level" -> { if (a.length == 2 && s.hasPermission("warp.admin")) c.addAll(m(a[1], names())); if (a.length == 3) c.addAll(m(a[2], LEVELS)); }
            case "edit","member" -> {
                if (a.length == 2) c.addAll(m(a[1], managedNames(s)));
                else if (a[0].equalsIgnoreCase("edit") && a.length == 3) c.addAll(m(a[2], EDIT_OPS));
                else if (a[0].equalsIgnoreCase("edit") && a.length == 4 && a[2].equalsIgnoreCase("welcome")) c.addAll(m(a[3], LINES));
                else if (a[0].equalsIgnoreCase("member") && a.length == 3) c.addAll(m(a[2], ADD_REM));
                else if (a[0].equalsIgnoreCase("member") && a.length == 4) c.addAll(m(a[3], players()));
            }
            case "admin" -> {
                if (a.length == 2) c.addAll(m(a[1], ADD_REM));
                if (a.length == 3) c.addAll(m(a[2], ownedNames(s)));
                if (a.length == 4) c.addAll(m(a[3], players()));
            }
        }
        return c;
    }

    private List<String> names() { return lm.all().stream().map(Landmark::getName).toList(); }
    private List<String> managedNames(CommandSender s) {
        if (s.hasPermission("warp.admin")) return names();
        if (s instanceof Player p) return lm.all().stream().filter(x -> x.isAdmin(p.getUniqueId())).map(Landmark::getName).toList();
        return List.of();
    }
    private List<String> ownedNames(CommandSender s) {
        if (s.hasPermission("warp.admin")) return names();
        if (s instanceof Player p) return lm.all().stream().filter(x -> x.isOwner(p.getUniqueId())).map(Landmark::getName).toList();
        return List.of();
    }
    private List<String> players() { return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(); }
    private List<String> m(String in, List<String> opts) {
        String lo = in.toLowerCase();
        return opts.stream().filter(x -> x.toLowerCase().startsWith(lo)).toList();
    }
}
