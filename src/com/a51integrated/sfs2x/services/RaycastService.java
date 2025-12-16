package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.*;
import org.joml.Vector3f;

import java.util.List;

public class RaycastService
{
    private final List<CollisionShapeData> collisionMapPayloads;
    private final LayerCategoryMapService layerCategoryMapService;

    private RaycastShapeData raycastShapeData;

    private RaycastHit bestHit = new RaycastHit();
    private RaycastHit closestHit = new RaycastHit();

    private AABBData aabbData = new AABBData();

    public RaycastService(List<CollisionShapeData> collisionMapPayloads, LayerCategoryMapService layerCategoryMapService, RaycastShapeData raycastShapeData)
    {
        this.collisionMapPayloads = collisionMapPayloads;
        this.layerCategoryMapService = layerCategoryMapService;
        this.raycastShapeData = raycastShapeData;
    }

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance, ECollisionCategory collisionCategory, int layerMask)
    {
        if (maxDistance < 0)
            return bestHit;

        bestHit.clear();

        var sqrX = sqr(direction.x);
        var sqrY = sqr(direction.y);
        var sqrZ = sqr(direction.z);

        var len = (float) Math.sqrt(sqrX + sqrY + sqrZ);

        if (len < Float.MIN_VALUE)
            return bestHit;

        var dx = direction.x / len;
        var dy = direction.y / len;
        var dz = direction.z / len;

        raycastShapeData.set(origin.x, origin.y, origin.z, dx, dy, dz);

        //TODO: Оптимизировать нет смылса кидать постоянно на каждый обьект рейкаст
        for (var shape : collisionMapPayloads)
        {
            if (shape.LayerCategory != collisionCategory)
                continue;

            if (layerCategoryMapService.layerInMask(shape.Layer, layerMask))
                continue;

            closestHit = raycastShape(maxDistance, shape);

            if (closestHit.getHit() && closestHit.getDistance() < bestHit.getDistance())
                bestHit = closestHit;
        }

        return bestHit;
    }

    private RaycastHit raycastShape(float maxDistance, CollisionShapeData collisionShapeData)
    {
        switch (collisionShapeData.Type)
        {
            case Box:
                return raycastToBox(maxDistance, collisionShapeData);

            case Sphere:
                return raycastToSphere(maxDistance, collisionShapeData);

            case Capsule:
                return raycastToCapsule(maxDistance, collisionShapeData);

            default:
                closestHit.clear();
                return closestHit;
        }
    }

    private RaycastHit raycastToBox(float maxDistance, CollisionShapeData shape)
    {
        var hx = shape.Size.x * shape.Scale.x * 0.5f;
        var hy = shape.Size.y * shape.Scale.y * 0.5f;
        var hz = shape.Size.z * shape.Scale.z * 0.5f;

        var cx = shape.Size.x * shape.Scale.x;
        var cy = shape.Size.y * shape.Scale.y;
        var cz = shape.Size.z * shape.Scale.z;

        var minX = cx - hx;
        var minY = cy - hy;
        var minZ = cz - hz;

        var maxX = cx + hx;
        var maxY = cy + hy;
        var maxZ = cz + hz;

        var tMin = 0f;
        var tMax = maxDistance;

        if (Math.abs(raycastShapeData.dx) < Float.MIN_VALUE
                || Math.abs(raycastShapeData.dy) < Float.MIN_VALUE
                || Math.abs(raycastShapeData.dz) < Float.MIN_VALUE
        )
        {
            closestHit.clear();
            return closestHit;
        }





        return closestHit;
    }

    private RaycastHit raycastToSphere(float maxDistance, CollisionShapeData shape)
    {
        return closestHit;
    }

    private RaycastHit raycastToCapsule(float maxDistance, CollisionShapeData shape)
    {
        return closestHit;
    }

    private static float sqr(float value)
    {
        return value * value;
    }
}
