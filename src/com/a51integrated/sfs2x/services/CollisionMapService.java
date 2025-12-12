package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.CollisionMapPayload;
import com.a51integrated.sfs2x.data.CollisionShapeData;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollisionMapService
{
    private final List<CollisionShapeData> shapes = new ArrayList<>();

    public CollisionMapService(String path)
    {
        var collisionMapPayload = DeserializeCollisionMap(path);

        assert collisionMapPayload != null;

        shapes.addAll(collisionMapPayload.Shapes);
    }

    public void clear()
    {
        shapes.clear();
    }

    public boolean isColliding(float px, float py, float pz)
    {
        for (var shape : shapes)
        {
            //TODO: SDK Parameters
            float playerRadius = 0.5f;
            float playerHeight = 2f;

            if (intersectsShape(shape, px, py, pz, playerRadius, playerHeight))
                return true;
        }

        return false;
    }

    private boolean intersectsShape(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
    {
        switch (shape.Type)
        {
            case Capsule:
                return collisionCapsuleWithCapsule(shape, px, py, pz, radius, height);

            case Sphere:
                return collisionCapsuleWithSphere(shape, px, py, pz, radius, height);

            default:
                return collisionCapsuleWithBox(shape, px, py, pz, radius, height);
        }
    }

    private boolean collisionCapsuleWithSphere(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
    {
        var sx = shape.Position.x + shape.Center.x * shape.Scale.x;
        var sy = shape.Position.y + shape.Center.y * shape.Scale.y;
        var sz = shape.Position.z + shape.Center.z * shape.Scale.z;

        var sphereRadius = shape.Radius * shape.Scale.x;

        var capsuleBottomY = py;
        var capsuleTopY = py + height;

        var closetY = clamp(sy, capsuleBottomY, capsuleTopY);

        float dx = px - sx;
        float dy = closetY - sy;
        float dz = pz - sz;

        float rSum = radius + sphereRadius;
        return (dx*dx + dy*dy + dz*dz) <= (rSum * rSum);
    }

    private boolean collisionCapsuleWithBox(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
    {
        var boxAABB = buildAABBFromBox(shape);
        return boxAABB.intersectsCapsule(px, py, pz, radius, height);
    }

    private boolean collisionCapsuleWithCapsule(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
    {
        var cx = shape.Position.x + shape.Center.x * shape.Scale.x;
        var cy = shape.Position.y + shape.Center.y * shape.Scale.y;
        var cz = shape.Position.z + shape.Center.z * shape.Scale.z;

        var envRadius = shape.Radius * shape.Scale.x;
        var envHeight = shape.Height * shape.Scale.y;

        var aBottom = py;
        var aTop = py + height;

        var bBottom = cy;
        var bTop = cy + envHeight;

        if (aTop < bBottom || aBottom > bTop)
            return false;

        var dx = px - cx;
        var dz = pz - cz;

        var rSum = radius + envRadius;
        return (dx*dx + dz*dz) <= (rSum * rSum);
    }

    private AABBService buildAABBFromBox(CollisionShapeData shape)
    {
        var hx = shape.Size.x * shape.Scale.x * 0.5f;
        var hy = shape.Size.y * shape.Scale.y * 0.5f;
        var hz = shape.Size.z * shape.Scale.z * 0.5f;

        var cx = shape.Position.x + shape.Center.x * shape.Scale.x;
        var cy = shape.Position.y + shape.Center.y * shape.Scale.y;
        var cz = shape.Position.x + shape.Size.x * shape.Scale.x;

        var minX = cx - hx;
        var maxX = cx + hx;
        var minY = cy - hy;
        var maxY = cy + hy;
        var minZ = cz - hz;
        var maxZ = cz + hz;

        return new AABBService(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private float clamp(float v, float min, float max)
    {
        return Math.max(min, Math.min(max, v));
    }

    private CollisionMapPayload DeserializeCollisionMap(String path)
    {
        var file = new File(path);
        var mapper = new ObjectMapper();

        try
        {
            return mapper.readValue(file, CollisionMapPayload.class);
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
