package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.*;
import org.joml.Vector3f;

import java.util.List;

public class RaycastService
{
    private final List<CollisionShapeData> shapes;
    private final LayerCategoryMapService layerCategoryMapService;

    private static final float AXIS_EPSILON = 1e-6f;
    private final RaycastShapeData raycastShapeData = new RaycastShapeData();
    private final SlabRange slabRange = new SlabRange();

    public RaycastService(List<CollisionShapeData> shapes, LayerCategoryMapService layerCategoryMapService)
    {
        this.shapes = shapes;
        this.layerCategoryMapService = layerCategoryMapService;
    }

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance, int layerMask)
    {
        var bestHit = new RaycastHit();

        if (maxDistance < 0f)
            return bestHit;

        var len = direction.length();

        if (len < AXIS_EPSILON)
            return bestHit;

        var invLen = 1f / len;

        raycastShapeData.set(origin.x, origin.y, origin.z,
                direction.x * invLen,
                direction.y * invLen,
                direction.z * invLen);

        var closestHit = new RaycastHit();

        //TODO: Оптимизировать нет смылса кидать постоянно на каждый обьект рейкаст
        for (var shape : shapes)
        {
            var layerValid = layerCategoryMapService.layerInMask(shape.Layer, layerMask);

            if (!layerValid)
                continue;

            if (!raycastShape(maxDistance, shape, closestHit))
                continue;

            if (!bestHit.getHit() || closestHit.getDistance() < bestHit.getDistance())
            {
                bestHit.copyFrom(closestHit);
            }
        }

        return bestHit;
    }

    private boolean raycastShape(float maxDistance, CollisionShapeData collisionShapeData, RaycastHit outHit)
    {
        switch (collisionShapeData.Type)
        {
            case Box:
                return raycastToBox(maxDistance, collisionShapeData, outHit);

            case Sphere:
                return raycastToSphere(maxDistance, collisionShapeData, outHit);

            case Capsule:
                return raycastToCapsule(maxDistance, collisionShapeData, outHit);

            default:
                outHit.clear();
                return false;
        }
    }

    private boolean raycastToBox(float maxDistance, CollisionShapeData shape, RaycastHit outHit)
    {
        outHit.clear();

        var hx = shape.Size.x * shape.Scale.x * 0.5f;
        var hy = shape.Size.y * shape.Scale.y * 0.5f;
        var hz = shape.Size.z * shape.Scale.z * 0.5f;

        var cx = shape.Center.x * shape.Scale.x;
        var cy = shape.Center.y * shape.Scale.y;
        var cz = shape.Center.z * shape.Scale.z;

        var minX = cx - hx;
        var minY = cy - hy;
        var minZ = cz - hz;

        var maxX = cx + hx;
        var maxY = cy + hy;
        var maxZ = cz + hz;

        slabRange.set(0f, maxDistance);

        if (!intersectSlab(minX, maxX, raycastShapeData.ox, raycastShapeData.dx, slabRange))
            return false;

        if (!intersectSlab(minY, maxY, raycastShapeData.oy, raycastShapeData.dy, slabRange))
            return false;

        if (!intersectSlab(minZ, maxZ, raycastShapeData.oz, raycastShapeData.dz, slabRange))
            return false;

        var tMin = slabRange.tMin;
        var tMax = slabRange.tMax;

        if (tMax < 0f)
            return false;

        var hitT = (tMin >= 0f) ? tMin : tMax;

        if (hitT > maxDistance)
            return false;

        outHit.setHit(true);
        outHit.setDistance(hitT);
        outHit.setPoint(
                raycastShapeData.ox + raycastShapeData.dx * hitT,
                raycastShapeData.oy + raycastShapeData.dy * hitT,
                raycastShapeData.oz + raycastShapeData.dz * hitT);
        return true;
    }

    private boolean raycastToSphere(float maxDistance, CollisionShapeData shape, RaycastHit outHit)
    {
        outHit.clear();
        return false;
    }

    private boolean raycastToCapsule(float maxDistance, CollisionShapeData shape, RaycastHit outHit)
    {
        outHit.clear();
        return false;
    }

    private boolean intersectSlab(float minAxis, float maxAxis, float originAxis, float directionAxis, SlabRange range)
    {
        if (isNearlyZero(directionAxis))
        {
            if (originAxis < minAxis || originAxis > maxAxis)
                return false;

            return true;
        }

        var inv = 1f / directionAxis;
        var t1 = (minAxis - originAxis) * inv;
        var t2 = (maxAxis - originAxis) * inv;

        if (t1 > t2)
        {
            var tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        range.tMin = Math.max(range.tMin, t1);
        range.tMax = Math.min(range.tMax, t2);

        return range.tMax >= range.tMin;
    }

    private boolean isNearlyZero(float directionAxis)
    {
        return Math.abs(directionAxis) < AXIS_EPSILON;
    }

    private static final class SlabRange
    {
        private float tMin;
        private float tMax;

        private void set(float tMin, float tMax)
        {
            this.tMin = tMin;
            this.tMax = tMax;
        }
    }
}
