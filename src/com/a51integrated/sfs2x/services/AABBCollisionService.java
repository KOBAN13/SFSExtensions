package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.AABBData;
import com.a51integrated.sfs2x.data.CollisionShapeData;
import com.a51integrated.sfs2x.data.PlayerCollider;

public class AABBCollisionService
{
    private final AABBData aabbData = new AABBData();

    public boolean collisionCapsuleWithBox(CollisionShapeData shape, PlayerCollider player)
    {
        var hx = halfScaled(shape.Size.x, shape.Scale.x);
        var hy = halfScaled(shape.Size.y, shape.Scale.y);
        var hz = halfScaled(shape.Size.z, shape.Scale.z);

        var cx = shape.Center.x * shape.Scale.x;
        var cy = shape.Center.y * shape.Scale.y;
        var cz = shape.Center.z * shape.Scale.z;

        aabbData.setCenterHalfExtents(cx, cy, cz, hx, hy, hz);

        return intersectsCapsuleAabbXZ(player);
    }

    public boolean collisionCapsuleWithSphere(CollisionShapeData shape, PlayerCollider player)
    {
        var sx = scaled(shape.Center.x, shape.Scale.x);
        var sy = scaled(shape.Center.y, shape.Scale.y);
        var sz = scaled(shape.Center.z, shape.Scale.z);

        var sphereRadius = scaled(shape.Radius, shape.Scale.x);

        var capsuleBottomY = player.y;
        var capsuleTopY = capsuleBottomY + player.height;

        var closestY = clamp(sy, capsuleBottomY, capsuleTopY);

        var dx = player.x - sx;
        var dy = closestY - sy;
        var dz = player.z - sz;

        var radius = player.radius + sphereRadius;
        return sqr(dx) + sqr(dy) + sqr(dz) <= sqr(radius);
    }

    public boolean collisionCapsuleWithCapsule(CollisionShapeData shape, PlayerCollider player)
    {
        var cx = scaled(shape.Center.x, shape.Scale.x);
        var cy = scaled(shape.Center.y, shape.Scale.y);
        var cz = scaled(shape.Center.z, shape.Scale.z);

        var envRadius = scaled(shape.Radius, shape.Scale.x);
        var envHeight = scaled(shape.Height, shape.Scale.y);

        var aBottom = player.y;
        var aTop = aBottom + player.height;

        var bBottom = cy;
        var bTop = bBottom + envHeight;

        if (aTop + player.radius < bBottom - envRadius || aBottom - player.radius > bTop + envRadius)
            return false;

        var dx = player.x - cx;
        var dz = player.z - cz;

        var r = player.radius + envRadius;
        return sqr(dx) + sqr(dz) <= sqr(r);
    }

    private boolean intersectsCapsuleAabbXZ(PlayerCollider player)
    {
        var capsuleBottomY = player.y;
        var capsuleTopY = capsuleBottomY + player.height;

        if (capsuleTopY + player.radius < aabbData.minY || capsuleBottomY - player.radius > aabbData.maxY)
            return false;

        var closestX = clamp(player.x, aabbData.minX, aabbData.maxX);
        var closestZ = clamp(player.z, aabbData.minZ, aabbData.maxZ);

        var dx = player.x - closestX;
        var dz = player.z - closestZ;

        return sqr(dx) + sqr(dz) <= sqr(player.radius);
    }

    private static float scaled(float value, float scale)
    {
        return value * scale;
    }

    private static float halfScaled(float value, float scale)
    {
        return value * scale * 0.5f;
    }

    private static float sqr(float value)
    {
        return value * value;
    }

    private static float clamp(float v, float min, float max)
    {
        return Math.max(min, Math.min(max, v));
    }
}
