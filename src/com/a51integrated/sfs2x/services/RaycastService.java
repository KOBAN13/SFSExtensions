package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.data.*;
import org.joml.Vector3f;

import java.util.List;

public class RaycastService
{
    private final List<CollisionShapeData> shapes;
    private final LayerCategoryMapService layerCategoryMapService;
    private final GameExtension gameExtension;

    private RaycastShapeData raycastShapeData = new RaycastShapeData();

    private RaycastHit bestHit = new RaycastHit();
    private RaycastHit closestHit = new RaycastHit();

    public RaycastService(List<CollisionShapeData> shapes, LayerCategoryMapService layerCategoryMapService, GameExtension gameExtension)
    {
        this.shapes = shapes;
        this.layerCategoryMapService = layerCategoryMapService;
        this.gameExtension = gameExtension;
    }

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance, int layerMask)
    {
        gameExtension.trace("Distance: " + maxDistance);
        if (maxDistance < 0)
            return bestHit;

        bestHit.clear();

        var sqrX = sqr(direction.x);
        var sqrY = sqr(direction.y);
        var sqrZ = sqr(direction.z);

        var len = (float) Math.sqrt(sqrX + sqrY + sqrZ);

        gameExtension.trace("Len: " + len);

        if (len < Float.MIN_VALUE)
            return bestHit;

        var dx = direction.x / len;
        var dy = direction.y / len;
        var dz = direction.z / len;

        raycastShapeData.set(origin.x, origin.y, origin.z, dx, dy, dz);

        //TODO: Оптимизировать нет смылса кидать постоянно на каждый обьект рейкаст
        for (var shape : shapes)
        {
            var layerValid = layerCategoryMapService.layerInMask(shape.Layer, layerMask);

            if (!layerValid)
                continue;

            if (shape.Type != ECollisionShapeType.Box)
                continue;

            gameExtension.trace("shape: " + shape.Name);

            closestHit = raycastShape(maxDistance, shape);

            if (!closestHit.getHit())
                continue;

            if (!bestHit.getHit() || closestHit.getDistance() < bestHit.getDistance())
            {
                bestHit.setHit(true);
                bestHit.setDistance(closestHit.getDistance());
                bestHit.setPoint(new Vector3f(closestHit.getPoint()));
            }
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

        var cx = shape.Center.x * shape.Scale.x;
        var cy = shape.Center.y * shape.Scale.y;
        var cz = shape.Center.z * shape.Scale.z;

        var minX = cx - hx;
        var minY = cy - hy;
        var minZ = cz - hz;

        var maxX = cx + hx;
        var maxY = cy + hy;
        var maxZ = cz + hz;

        var tMin = 0f;
        var tMax = maxDistance;

        if (checkMinValue(raycastShapeData.dx))
        {
            if (raycastShapeData.ox < minX || raycastShapeData.ox > maxX)
            {
                gameExtension.trace("ox: " + raycastShapeData.ox + "min: " + minX + " max: " + maxX);

                gameExtension.trace("ox меньше minx || ox больше maxx");
                return closestHit;
            }
        }
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

            gameExtension.trace("X посчитан успешно");
        }

        if (checkMinValue(raycastShapeData.dy))
        {
            if (raycastShapeData.oy < minY || raycastShapeData.oy > maxY)
            {
                gameExtension.trace("oy: " + raycastShapeData.oy + "min: " + minY + " max: " + maxY);

                gameExtension.trace("oy меньше miny || oy больше maxy");
                return closestHit;
            }
        }
        else
        {
            var inv = 1f / raycastShapeData.dy;

            var t1 = (minY - raycastShapeData.oy) * inv;
            var t2 = (maxY - raycastShapeData.oy) * inv;

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

            gameExtension.trace("Y посчитан успешно");
        }

        if (checkMinValue(raycastShapeData.dz))
        {
            if (raycastShapeData.oz < minZ || raycastShapeData.oz > maxZ)
            {
                gameExtension.trace("oz меньше minx || oz больше maxx");
                return closestHit;
            }
        }
        else
        {
            var inv = 1f / raycastShapeData.dz;

            var t1 = (minZ - raycastShapeData.oz) * inv;
            var t2 = (maxZ - raycastShapeData.oz) * inv;

            if (t1 >t2)
            {
                var tmp = t1;
                t1 = t2;
                t2 = tmp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            gameExtension.trace("tMin: " + tMin + " tMax: " + tMax);
            gameExtension.trace("minZ: " + minZ + " maxZ: " + maxZ);
            gameExtension.trace("t1: " + t1 + " t2: " + t2);
            gameExtension.trace("inv: " + inv + " dz: " + raycastShapeData.dz);

            if (tMax < tMin)
                return closestHit;

            gameExtension.trace("Z посчитан успешно");
        }

        if (tMax < 0f)
            return closestHit;

        var hitT = (tMin >= 0f) ? tMin : tMax;

        gameExtension.trace("hitT: " + hitT);

        if (hitT > maxDistance)
            return closestHit;

        var pointVector = new Vector3f(
                raycastShapeData.ox + raycastShapeData.dx * hitT,
                raycastShapeData.oy + raycastShapeData.dy * hitT,
                raycastShapeData.oz + raycastShapeData.dz * hitT);

        closestHit.setHit(true);
        closestHit.setDistance(hitT);
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

    private boolean checkMinValue(float directionAxis)
    {
        return Math.abs(directionAxis) < Float.MIN_VALUE;
    }
}
