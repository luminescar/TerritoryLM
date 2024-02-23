package me.luminescar.territory;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class WorldEditCuboid {

    private final Location point1;
    private final Location point2;

    public WorldEditCuboid(Location point1, Location point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public void fillWithParticles(Particle particleEffect) {
        World world = point1.getWorld();

        if (world == null) {
            // Log an error or throw an exception, as it's necessary for the world to be non-null
            return;
        }

        double minX = Math.min(point1.getX(), point2.getX());
        double minY = Math.min(point1.getY(), point2.getY());
        double minZ = Math.min(point1.getZ(), point2.getZ());
        double maxX = Math.max(point1.getX(), point2.getX());
        double maxY = Math.max(point1.getY(), point2.getY());
        double maxZ = Math.max(point1.getZ(), point2.getZ());

        for (double x = minX; x <= maxX; x += 0.5) {
            for (double y = minY; y <= maxY; y += 0.5) {
                for (double z = minZ; z <= maxZ; z += 0.5) {
                    world.spawnParticle(particleEffect, x, y, z, 1);
                }
            }
        }
    }
}

