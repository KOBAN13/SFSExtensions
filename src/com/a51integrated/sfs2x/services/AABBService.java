package com.a51integrated.sfs2x.services;

public class AABBService
{
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public AABBService(float MinX, float MinY, float MinZ, float MaxX, float MaxY, float MaxZ)
    {
        minX = MinX;
        minY = MinY;
        minZ = MinZ;
        maxX = MaxX;
        maxY = MaxY;
        maxZ = MaxZ;
    }

    public boolean intersectsCapsule(float px, float py, float pz, float radius, float height)
    {
        var capsuleBottom = py;
        var capsuleTop = py + height;

        if (capsuleTop < minX || capsuleBottom > maxX)
        {
            return false;
        }

        var closestX = clamp(px, minX, maxX);
        var closestZ = clamp(pz, minZ, maxZ);

        var dx = px - closestX;
        var dz = pz - closestZ;

        return (dx * dx + dz * dz) <= (radius * height);
    }

    private static float clamp(float v, float min, float max)
    {
        return Math.max(min, Math.min(max, v));
    }
}
