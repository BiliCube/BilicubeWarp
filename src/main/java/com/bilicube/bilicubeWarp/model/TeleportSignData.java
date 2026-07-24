package com.bilicube.bilicubeWarp.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportSignData {

    private final UUID id;
    private final UUID worldId;
    private final int x, y, z;
    private final UUID landmarkId;

    public TeleportSignData(UUID id, UUID worldId, int x, int y, int z, UUID landmarkId) {
        this.id = id;
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.landmarkId = landmarkId;
    }

    public static TeleportSignData create(Location location, UUID landmarkId) {
        return new TeleportSignData(
                UUID.randomUUID(),
                location.getWorld().getUID(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                landmarkId
        );
    }

    public UUID getId() { return id; }
    public UUID getWorldId() { return worldId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public UUID getLandmarkId() { return landmarkId; }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldId);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public boolean matches(Location loc) {
        return worldId.equals(loc.getWorld().getUID())
                && x == loc.getBlockX() && y == loc.getBlockY() && z == loc.getBlockZ();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id.toString());
        map.put("world", worldId.toString());
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("landmark", landmarkId.toString());
        return map;
    }

    public static TeleportSignData deserialize(Map<String, Object> map) {
        UUID id = UUID.fromString((String) map.get("id"));
        UUID worldId = UUID.fromString((String) map.get("world"));
        int x = ((Number) map.get("x")).intValue();
        int y = ((Number) map.get("y")).intValue();
        int z = ((Number) map.get("z")).intValue();
        UUID landmarkId = UUID.fromString((String) map.get("landmark"));
        return new TeleportSignData(id, worldId, x, y, z, landmarkId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeleportSignData that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
