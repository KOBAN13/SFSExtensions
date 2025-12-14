package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.data.CollisionMapPayload;
import com.a51integrated.sfs2x.data.CollisionShapeData;
import com.a51integrated.sfs2x.data.ECollisionCategory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollisionMapService
{
    private final List<CollisionShapeData> shapes = new ArrayList<>();
    private final GameExtension gameExtension;
    private final LayerCategoryMapService layerCategoryMapService = new LayerCategoryMapService();

    public CollisionMapService(String path, GameExtension gameExtension)
    {
        this.gameExtension = gameExtension;
        var collisionMapPayload = DeserializeCollisionMap(path);

        assert collisionMapPayload != null;

        shapes.addAll(collisionMapPayload.Shapes);

        gameExtension.trace("Collision map loaded from " + path + " with " + shapes.size() + " shapes");
    }

    public void clear()
    {
        shapes.clear();
    }

    public boolean isColliding(float px, float py, float pz)
    {
        //TODO: SDK Parameters
        var playerRadius = 0.5f;
        var playerHeight = 2f;

        for (var shape : shapes)
        {
            gameExtension.trace("Testing against shape type:" + shape.Type + " pos:" +
                    shape.Position.x + "," + shape.Position.y + "," + shape.Position.z +
                    " scale:" + shape.Scale.x + "," + shape.Scale.y + "," + shape.Scale.z);

            if (intersectsShape(shape, px, py, pz, playerRadius, playerHeight))
            {
                gameExtension.trace("Collision detected with shape type:" + shape.Type);
                return true;
            }

            gameExtension.trace("Collision name not detected " + shape.Name);
        }
        gameExtension.trace("No collision detected for position x:" + px + " y:" + py + " z:" + pz);
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

        var dx = px - sx;
        var dy = closetY - sy;
        var dz = pz - sz;

        var rSum = radius + sphereRadius;
        var collides = (dx*dx + dy*dy + dz*dz) <= (rSum * rSum);

        gameExtension.trace("Sphere collision check -> center:" + sx + "," + sy + "," + sz +
                " radius:" + sphereRadius + " result:" + collides);

        return collides;
    }

    private boolean collisionCapsuleWithBox(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
    {
        var boxAABB = buildAABBFromBox(shape);
        var collides = boxAABB.intersectsCapsule(px, py, pz, radius, height);

        gameExtension.trace("Box collision check -> min:" + boxAABB.minX + "," + boxAABB.minY + "," + boxAABB.minZ +
                " max:" + boxAABB.maxX + "," + boxAABB.maxY + "," + boxAABB.maxZ + " result:" + collides);

        return collides;
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
        var cz = shape.Position.z + shape.Center.z * shape.Scale.z;

        var minX = cx - hx;
        var maxX = cx + hx;
        var minY = cy - hy;
        var maxY = cy + hy;
        var minZ = cz - hz;
        var maxZ = cz + hz;

        return new AABBService(gameExtension, minX, minY, minZ, maxX, maxY, maxZ);
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
            var collisionMapPayload = mapper.readValue(file, CollisionMapPayload.class);

            for (var shape : collisionMapPayload.Shapes)
            {
                shape.LayerCategory = layerCategoryMapService.getCategory(shape.LayerName);

                gameExtension.trace("Testing against shape name " + shape.Name + " pos: " + shape.Position.x + ", " + shape.Position.y + ", " + shape.Position.z);
            }

            return collisionMapPayload;
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
