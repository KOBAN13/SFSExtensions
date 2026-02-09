package com.a51integrated.sfs2x.services.collision;

import com.a51integrated.sfs2x.data.collision.AABBData;
import com.a51integrated.sfs2x.data.collision.CollisionShapeData;
import com.a51integrated.sfs2x.data.collision.RaycastHit;
import com.a51integrated.sfs2x.data.collision.RaycastShapeData;
import com.a51integrated.sfs2x.data.math.Ray;
import com.a51integrated.sfs2x.extensions.GameExtension;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

//TODO: Need refactoring
public class RaycastService
{
    private static final float AXIS_EPSILON = 1e-6f;

    private final CollisionMapService collisionMapService;
    private final LayerCategoryMapService layerCategoryMapService;
    private final RewindSnapshotService rewindSnapshotService;

    private final GameExtension game;
    private final AABBData aabbData = new AABBData();
    private final RaycastShapeData raycastShapeData = new RaycastShapeData();
    private final AABBCollisionRotateService aabbCollisionRotateService;
    private final SlabRange slabRange = new SlabRange();
    private final RaycastHit bestHit = new RaycastHit();
    private final RaycastHit closestHit = new RaycastHit();

    private final List<CollisionShapeData> allShapes = new ArrayList<>();

    public RaycastService(
            CollisionMapService collisionMapService,
            LayerCategoryMapService layerCategoryMapService,
            RewindSnapshotService rewindSnapshotService,
            GameExtension game
    )
    {
        this.collisionMapService = collisionMapService;
        this.rewindSnapshotService = rewindSnapshotService;
        this.game = game;
        this.layerCategoryMapService = layerCategoryMapService;
        aabbCollisionRotateService = new AABBCollisionRotateService(aabbData);
    }

    public RaycastHit handleShot(int shooterId, long clientShotSnapshotId, long serverSnapshotId, int clientAlpha, Ray ray)
    {
        allShapes.clear();
        allShapes.addAll(collisionMapService.getShapes());

        for (var entry : collisionMapService.getPlayerShapeEntries())
        {
            var userId = entry.getKey();

            if (userId == shooterId)
                continue;

            var interpolatedState = rewindSnapshotService
                    .getInterpolatePlayerState(userId, clientShotSnapshotId, serverSnapshotId, clientAlpha);

            var shapeCopy = entry.getValue().copy();
            var center = shapeCopy.Center;
            var position = interpolatedState.interpolatedPosition;

            center.x = position.x;
            center.y = position.y;
            center.z = position.z;

            allShapes.add(shapeCopy);
        }

        return raycast(ray, allShapes);
    }

    private RaycastHit raycast(Ray ray, List<CollisionShapeData> shapes)
    {
        bestHit.clear();

        if (ray.maxDistance < 0f)
            return bestHit;

        var len = ray.direction.length();

        if (len < AXIS_EPSILON)
            return bestHit;

        var invLen = 1f / len;

        raycastShapeData.set(ray.origin.x, ray.origin.y, ray.origin.z,
                ray.direction.x * invLen,
                ray.direction.y * invLen,
                ray.direction.z * invLen);

        //TODO: Оптимизировать нет смылса кидать постоянно на каждый обьект рейкаст
        for (var shape : shapes)
        {
            var layerValid = layerCategoryMapService.layerInMask(shape.Layer, ray.layerMask);

            if (!layerValid)
                continue;

            if (!raycastShape(ray.maxDistance, shape, closestHit))
                continue;

            game.trace("Shape: " + shape.Name);

            if (!bestHit.getHit() || closestHit.getDistance() < bestHit.getDistance())
            {
                bestHit.copyFrom(closestHit);
                game.trace("Found: " + shape.Name);
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

        var hx = shape.Size.x * 0.5f;
        var hy = shape.Size.y * 0.5f;
        var hz = shape.Size.z * 0.5f;

        var cx = shape.Center.x;
        var cy = shape.Center.y;
        var cz = shape.Center.z;

        aabbCollisionRotateService.setAabbFromObb(cx, cy, cz, hx, hy, hz, shape.Rotation);

        slabRange.set(0f, maxDistance);

        if (!intersectSlab(aabbData.minX, aabbData.maxX, raycastShapeData.ox, raycastShapeData.dx, slabRange))
            return false;

        if (!intersectSlab(aabbData.minY, aabbData.maxY, raycastShapeData.oy, raycastShapeData.dy, slabRange))
            return false;

        if (!intersectSlab(aabbData.minZ, aabbData.maxZ, raycastShapeData.oz, raycastShapeData.dz, slabRange))
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

        var cx = shape.Center.x;
        var cy = shape.Center.y;
        var cz = shape.Center.z;

        var radius = shape.Radius * shape.Scale.x;

        var hitT = intersectRaySphere(cx, cy, cz, radius, maxDistance);

        if (hitT == Float.POSITIVE_INFINITY)
            return false;

        outHit.setHit(true);
        outHit.setDistance(hitT);
        outHit.setPoint(
                raycastShapeData.ox + raycastShapeData.dx * hitT,
                raycastShapeData.oy + raycastShapeData.dy * hitT,
                raycastShapeData.oz + raycastShapeData.dz * hitT);
        return true;
    }

    private boolean raycastToCapsule(float maxDistance, CollisionShapeData shape, RaycastHit outHit)
    {
        outHit.clear();

        var cx = shape.Center.x;
        var cy = shape.Center.y;
        var cz = shape.Center.z;

        var radius = shape.Radius;
        var height = shape.Height;

        var hitT = Float.POSITIVE_INFINITY;

        var dx = raycastShapeData.dx;
        var dz = raycastShapeData.dz;
        var a = dx * dx + dz * dz;

        if (a > AXIS_EPSILON)
        {
            var ox = raycastShapeData.ox;
            var oz = raycastShapeData.oz;
            var ocx = ox - cx;
            var ocz = oz - cz;

            var b = ocx * dx + ocz * dz;
            var c = ocx * ocx + ocz * ocz - radius * radius;

            var discriminant = b * b - a * c;

            if (discriminant >= 0f)
            {
                var sqrtDisc = (float)Math.sqrt(discriminant);
                var t1 = (-b - sqrtDisc) / a;
                var t2 = (-b + sqrtDisc) / a;

                hitT = pickCapsuleBodyHit(cy, height, maxDistance, hitT, t1);
                hitT = pickCapsuleBodyHit(cy, height, maxDistance, hitT, t2);
            }
        }

        var tBottom = intersectRaySphere(cx, cy, cz, radius, maxDistance);

        if (tBottom < hitT)
            hitT = tBottom;

        var tTop = intersectRaySphere(cx, cy + height, cz, radius, maxDistance);

        if (tTop < hitT)
            hitT = tTop;

        if (hitT == Float.POSITIVE_INFINITY)
            return false;

        outHit.setHit(true);
        outHit.setDistance(hitT);
        outHit.setPoint(
                raycastShapeData.ox + raycastShapeData.dx * hitT,
                raycastShapeData.oy + raycastShapeData.dy * hitT,
                raycastShapeData.oz + raycastShapeData.dz * hitT);
        return true;
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

    private float pickCapsuleBodyHit(float capsuleBottomY, float capsuleHeight, float maxDistance, float currentBestT, float t)
    {
        if (t < 0f || t > maxDistance || t >= currentBestT)
            return currentBestT;

        var y = raycastShapeData.oy + raycastShapeData.dy * t;
        var topY = capsuleBottomY + capsuleHeight;

        if (y < capsuleBottomY || y > topY)
            return currentBestT;

        return t;
    }

    private float intersectRaySphere(float cx, float cy, float cz, float radius, float maxDistance)
    {
        var ox = raycastShapeData.ox;
        var oy = raycastShapeData.oy;
        var oz = raycastShapeData.oz;
        var dx = raycastShapeData.dx;
        var dy = raycastShapeData.dy;
        var dz = raycastShapeData.dz;

        var mx = ox - cx;
        var my = oy - cy;
        var mz = oz - cz;

        var b = mx * dx + my * dy + mz * dz;
        var c = mx * mx + my * my + mz * mz - radius * radius;

        if (c > 0f && b > 0f)
            return Float.POSITIVE_INFINITY;

        var discriminant = b * b - c;

        if (discriminant < 0f)
            return Float.POSITIVE_INFINITY;

        var sqrtDisc = (float)Math.sqrt(discriminant);
        var t = -b - sqrtDisc;

        if (t < 0f)
            t = -b + sqrtDisc;
        if (t < 0f || t > maxDistance)
            return Float.POSITIVE_INFINITY;

        return t;
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
