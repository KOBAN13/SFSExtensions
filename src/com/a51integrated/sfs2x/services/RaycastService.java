package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.*;
import org.joml.Vector3f;

import java.util.List;

public class RaycastService
{
    private final List<CollisionShapeData> shapes;
    private final LayerCategoryMapService layerCategoryMapService;

    private RaycastShapeData raycastShapeData = new RaycastShapeData();

    private RaycastHit bestHit = new RaycastHit();
    private RaycastHit closestHit = new RaycastHit();

    public RaycastService(List<CollisionShapeData> shapes, LayerCategoryMapService layerCategoryMapService)
    {
        this.shapes = shapes;
        this.layerCategoryMapService = layerCategoryMapService;
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
        for (var shape : shapes)
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
        closestHit.clear();

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

        if (checkMinValue())
        {
            return closestHit;
        }

        if (raycastShapeData.ox < minX || raycastShapeData.ox > maxX)
            return closestHit;
        else
        {
            var inv = 1f / raycastShapeData.dx;

            var t1 = (minX - raycastShapeData.ox) * inv;
            var t2 = (maxX - raycastShapeData.ox) * inv;

            if (t1 >t2)
            {
                var tmp = t1;
                t1 = t2;
                t2 = tmp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            if (tMax < tMin)
                return closestHit;
        }

        if (raycastShapeData.oy < minY || raycastShapeData.oy > maxY)
            return closestHit;
        else
        {
            var inv = 1f / raycastShapeData.dy;

            var t1 = (minX - raycastShapeData.oy) * inv;
            var t2 = (maxX - raycastShapeData.oy) * inv;

            if (t1 >t2)
            {
                var tmp = t1;
                t1 = t2;
                t2 = tmp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            if (tMax < tMin)
                return closestHit;
        }

        if (raycastShapeData.oz < minZ || raycastShapeData.oz > maxZ)
            return closestHit;
        else
        {
            var inv = 1f / raycastShapeData.dz;

            var t1 = (minX - raycastShapeData.oz) * inv;
            var t2 = (maxX - raycastShapeData.oz) * inv;

            if (t1 >t2)
            {
                var tmp = t1;
                t1 = t2;
                t2 = tmp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            if (tMax < tMin)
                return closestHit;
        }

        if (tMin < 0f || tMax > maxDistance)
            return closestHit;

        var pointVector = new Vector3f(raycastShapeData.ox + raycastShapeData.dx * tMin, raycastShapeData.oy + raycastShapeData.dy * tMin, raycastShapeData.oz + raycastShapeData.dz * tMin);

        closestHit.setHit(true);
        closestHit.setDistance(tMin);
        closestHit.setPoint(pointVector);
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

//    private void checkIntersectionAABBAlongAxis(float minAxis, float maxAxis, float originAxis, float directionAxis)
//    {
//        var inv = 1f / originAxis;
//
//        var t1 = (minAxis - directionAxis) * inv;
//        var t2 = (maxAxis - directionAxis) * inv;
//
//        if (t1 >t2)
//        {
//            var tmp = t1;
//            t1 = t2;
//            t2 = tmp;
//        }
//
//        tMin = Math.max(tMin, t1);
//        tMax = Math.min(tMax, t2);
//
//        if (tMax < tMin)
//            return closestHit;
//    }

    private static float sqr(float value)
    {
        return value * value;
    }

    private boolean checkMinValue()
    {
        return Math.abs(raycastShapeData.dx) < Float.MIN_VALUE
                || Math.abs(raycastShapeData.dy) < Float.MIN_VALUE
                || Math.abs(raycastShapeData.dz) < Float.MIN_VALUE;
    }
}
