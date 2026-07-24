package com.bilicube.bilicubeWarp.model;

import com.bilicube.bilicubeWarp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Landmark {

    public static final int MAX_DESC_LENGTH = 256;
    public static final String NAME_PATTERN = "[a-z0-9_]+";

    private final UUID id;
    private String name;
    private String displayName;
    private LandmarkLevel level;
    private String description;
    private UUID worldId;
    private double x, y, z;
    private float yaw, pitch;
    private UUID ownerUuid;
    private final Set<UUID> admins = new HashSet<>();
    private final Set<UUID> members = new HashSet<>();
    private long creationDate;
    private List<String> welcomeLines;
    private ItemStack icon;

    @SuppressWarnings("unchecked")
    public Landmark(UUID id, String name, String displayName, LandmarkLevel level,
                    String description, UUID worldId, double x, double y, double z,
                    float yaw, float pitch, UUID ownerUuid, Set<UUID> admins,
                    Set<UUID> members, long creationDate, List<String> welcomeLines,
                    ItemStack icon) {
        this.id = id;
        this.name = name;
        this.displayName = displayName != null ? displayName : name;
        this.level = level;
        this.description = description != null ? description : "";
        this.worldId = worldId;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.ownerUuid = ownerUuid;
        if (admins != null) this.admins.addAll(admins);
        if (members != null) this.members.addAll(members);
        this.creationDate = creationDate;
        this.welcomeLines = welcomeLines != null ? new ArrayList<>(welcomeLines) : new ArrayList<>();
        this.icon = icon;
    }

    public static Landmark create(String name, LandmarkLevel level, Location location, UUID ownerUuid) {
        return new Landmark(UUID.randomUUID(), name.toLowerCase(), name, level, "",
                location.getWorld().getUID(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(), ownerUuid,
                new HashSet<>(), new HashSet<>(), System.currentTimeMillis(),
                new ArrayList<>(), null);
    }

    public static boolean isValidName(String n) { return n != null && n.matches(NAME_PATTERN); }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public LandmarkLevel getLevel() { return level; }
    public String getDescription() { return description; }
    public UUID getWorldId() { return worldId; }
    public double getX() { return x; } public double getY() { return y; } public double getZ() { return z; }
    public float getYaw() { return yaw; } public float getPitch() { return pitch; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public Set<UUID> getAdmins() { return Collections.unmodifiableSet(admins); }
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public long getCreationDate() { return creationDate; }
    public List<String> getWelcomeLines() { return Collections.unmodifiableList(welcomeLines); }

    public ItemStack getIcon() {
        if (icon != null) return icon.clone();
        return new ItemStack(level.getDefaultIcon());
    }

    public String getPermissionNode() { return "warp.landmark." + name; }

    public void setName(String n) { this.name = n; }
    public void setDisplayName(String n) { this.displayName = n; }
    public void setLevel(LandmarkLevel l) { this.level = l; }
    public void setDescription(String d) { this.description = d != null ? d : ""; }
    public void setOwnerUuid(UUID u) { this.ownerUuid = u; }
    public void setIcon(ItemStack i) { this.icon = i != null ? i.clone() : null; }

    public void setLocation(Location loc) {
        this.worldId = loc.getWorld().getUID();
        this.x = loc.getX(); this.y = loc.getY(); this.z = loc.getZ();
        this.yaw = loc.getYaw(); this.pitch = loc.getPitch();
    }

    public void setWelcomeLine(int idx, String text) {
        while (welcomeLines.size() <= idx) welcomeLines.add("");
        welcomeLines.set(idx, text);
    }

    public boolean isOwner(UUID u) { return ownerUuid.equals(u); }
    public boolean isAdmin(UUID u) { return isOwner(u) || admins.contains(u); }
    public boolean addAdmin(UUID u) { return admins.add(u); }
    public boolean removeAdmin(UUID u) { return admins.remove(u); }
    public boolean addMember(UUID u) { return members.add(u); }
    public boolean removeMember(UUID u) { return members.remove(u); }

    public Location getLocation() {
        World w = Bukkit.getWorld(worldId);
        return w == null ? null : new Location(w, x, y, z, yaw, pitch);
    }

    public Component getWelcomeComponent() {
        int n = level.getWelcomeLineCount();
        if (n <= 0 || welcomeLines.isEmpty()) return Component.empty();
        Component c = MessageUtil.component(welcomeLines.get(0) != null ? welcomeLines.get(0) : "");
        if (n >= 2 && welcomeLines.size() >= 2)
            c = c.append(Component.newline()).append(MessageUtil.component(welcomeLines.get(1) != null ? welcomeLines.get(1) : ""));
        return c;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.toString());
        m.put("name", name);
        m.put("display_name", displayName);
        m.put("level", level.name());
        m.put("description", description);
        m.put("world", worldId.toString());
        m.put("x", x); m.put("y", y); m.put("z", z);
        m.put("yaw", (double) yaw); m.put("pitch", (double) pitch);
        m.put("owner", ownerUuid.toString());
        m.put("admins", new ArrayList<>(admins.stream().map(UUID::toString).toList()));
        m.put("members", new ArrayList<>(members.stream().map(UUID::toString).toList()));
        m.put("created", creationDate);
        m.put("welcome", new ArrayList<>(welcomeLines));
        if (icon != null) m.put("icon", icon.serialize());
        return m;
    }

    @SuppressWarnings("unchecked")
    public static Landmark deserialize(Map<String, Object> m) {
        UUID id = UUID.fromString((String) m.get("id"));
        String name = (String) m.get("name");
        String dn = (String) m.getOrDefault("display_name", name);
        LandmarkLevel lv = LandmarkLevel.valueOf((String) m.get("level"));
        String desc = (String) m.getOrDefault("description", "");
        UUID wid = UUID.fromString((String) m.get("world"));
        double x = ((Number) m.get("x")).doubleValue();
        double y = ((Number) m.get("y")).doubleValue();
        double z = ((Number) m.get("z")).doubleValue();
        float yaw = ((Number) m.getOrDefault("yaw", 0.0)).floatValue();
        float pitch = ((Number) m.getOrDefault("pitch", 0.0)).floatValue();
        UUID owner = UUID.fromString((String) m.get("owner"));
        Set<UUID> ads = new HashSet<>();
        for (String s : (List<String>) m.getOrDefault("admins", new ArrayList<>())) ads.add(UUID.fromString(s));
        Set<UUID> mems = new HashSet<>();
        for (String s : (List<String>) m.getOrDefault("members", new ArrayList<>())) mems.add(UUID.fromString(s));
        long created = ((Number) m.get("created")).longValue();
        List<String> welcome = (List<String>) m.getOrDefault("welcome", new ArrayList<>());
        ItemStack icon = null;
        if (m.containsKey("icon")) { try { icon = ItemStack.deserialize((Map<String, Object>) m.get("icon")); } catch (Exception ignored) {} }
        return new Landmark(id, name, dn, lv, desc, wid, x, y, z, yaw, pitch, owner, ads, mems, created, welcome, icon);
    }

    @Override public boolean equals(Object o) { return o instanceof Landmark l && id.equals(l.id); }
    @Override public int hashCode() { return id.hashCode(); }
}
