package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.GameExtension;

public class AABBService
{
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    private GameExtension gameExtension;

    public AABBService(GameExtension gameExtension, float MinX, float MinY, float MinZ, float MaxX, float MaxY, float MaxZ)
    {
        minX = MinX;
        minY = MinY;
        minZ = MinZ;
        maxX = MaxX;
        maxY = MaxY;
        maxZ = MaxZ;

        this.gameExtension = gameExtension;
    }

    public boolean intersectsCapsule(float px, float py, float pz, float radius, float height)
    {
        var capsuleBottom = py;
        var capsuleTop = py + height;

        if (capsuleTop + radius < minY || capsuleBottom - radius > maxY)
        {
            gameExtension.trace("AABB collision skipped: capsule outside vertical bounds minY:" + minY + " maxY:" + maxY +
                    " bottom:" + capsuleBottom + " top:" + capsuleTop);
            return false;
        }

        var closestX = clamp(px, minX, maxX);
        var closestZ = clamp(pz, minZ, maxZ);

        var dx = px - closestX;
        var dz = pz - closestZ;

        gameExtension.trace("Squart: ", (dx * dx + dz * dz) + " and " + (radius * radius));

        var collides = (dx * dx + dz * dz) <= (radius * radius);

        gameExtension.trace("AABB collision check -> capsulePos:" + px + "," + py + "," + pz +
                " radius:" + radius + " height:" + height +
                " clampX:" + closestX + " clampZ:" + closestZ +
                " bounds min:" + minX + "," + minY + "," + minZ +
                " max:" + maxX + "," + maxY + "," + maxZ +
                " result:" + collides);

        return collides;
    }

    private static float clamp(float v, float min, float max)
    {
        return Math.max(min, Math.min(max, v));
    }
}
