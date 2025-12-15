package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.data.CollisionMapPayload;
import com.a51integrated.sfs2x.data.CollisionShapeData;
import com.a51integrated.sfs2x.data.PlayerCollider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollisionMapService
{
    private final List<CollisionShapeData> shapes = new ArrayList<>();
    private final LayerCategoryMapService layerCategoryMapService = new LayerCategoryMapService();
    private final AABBCollisionService aabbService = new AABBCollisionService();
    private final PlayerCollider playerCollider = new PlayerCollider();

    //TODO: SDK Parameters
    private float playerRadius = 0.6f;
    private float playerHeight = 2f;

    public CollisionMapService(String path, GameExtension gameExtension)
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
        playerCollider.set(px, py, pz, playerRadius, playerHeight);

        for (var shape : shapes)
        {
            if (intersectsShape(shape))
            {
                return true;
            }
        }

        return false;
    }

    private boolean intersectsShape(CollisionShapeData shape)
    {
        switch (shape.Type)
        {
            case Capsule:
                return aabbService.collisionCapsuleWithCapsule(shape, playerCollider);

            case Sphere:
                return aabbService.collisionCapsuleWithSphere(shape, playerCollider);

            case Box:
                return aabbService.collisionCapsuleWithBox(shape, playerCollider);
        }

        return aabbService.collisionCapsuleWithBox(shape, playerCollider);
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
            }

            return collisionMapPayload;
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

//    private boolean collisionCapsuleWithSphere(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
//    {
//        var sx = shape.Center.x * shape.Scale.x;
//        var sy = shape.Center.y * shape.Scale.y;
//        var sz = shape.Center.z * shape.Scale.z;
//
//        var sphereRadius = shape.Radius * shape.Scale.x;
//
//        var capsuleBottomY = py;
//        var capsuleTopY = py + height;
//
//        var closetY = clamp(sy, capsuleBottomY, capsuleTopY);
//
//        var dx = px - sx;
//        var dy = closetY - sy;
//        var dz = pz - sz;
//
//        var rSum = radius + sphereRadius;
//        return (dx*dx + dy*dy + dz*dz) <= (rSum * rSum);
//    }
//
//    private boolean collisionCapsuleWithBox(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
//    {
//        var boxAABB = buildAABBFromBox(shape);
//        return boxAABB.intersectsCapsule(px, py, pz, radius, height);
//    }
//
//    private boolean collisionCapsuleWithCapsule(CollisionShapeData shape, float px, float py, float pz, float radius, float height)
//    {
//        var cx = shape.Center.x * shape.Scale.x;
//        var cy = shape.Center.y * shape.Scale.y;
//        var cz = shape.Center.z * shape.Scale.z;
//
//        var envRadius = shape.Radius * shape.Scale.x;
//        var envHeight = shape.Height * shape.Scale.y;
//
//        var aBottom = py;
//        var aTop = py + height;
//
//        var bBottom = cy;
//        var bTop = cy + envHeight;
//
//        if (aTop + radius < bBottom - envRadius || aBottom - radius > bTop + envRadius)
//            return false;
//
//        var dx = px - cx;
//        var dz = pz - cz;
//
//        var rSum = radius + envRadius;
//        return (dx*dx + dz*dz) <= (rSum * rSum);
//    }
//
//    private float clamp(float v, float min, float max)
//    {
//        return Math.max(min, Math.min(max, v));
//    }
}
