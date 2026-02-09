package com.a51integrated.sfs2x.data.collision;

import com.a51integrated.sfs2x.data.math.Quaternion;
import com.a51integrated.sfs2x.services.collision.AABBCollisionRotateService;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class ServerCollisionData
{
    private static final float ROTATION_EPSILON = 1e-6f;
    private final String shapeId;
    private final boolean hasObb;
    private final float obbCx, obbCy, obbCz;
    private final float obbHx, obbHy, obbHz;
    private final Quaternion obbRotation;
    private final float minX, minY, minZ;
    private final float maxX, maxY, maxZ;

    public ServerCollisionData(String shapeId,
                               boolean hasObb,
                               float obbCx, float obbCy, float obbCz,
                               float obbHx, float obbHy, float obbHz,
                               Quaternion obbRotation,
                               float minX, float minY, float minZ,
                               float maxX, float maxY, float maxZ)
    {
        this.shapeId = shapeId;
        this.hasObb = hasObb;
        this.obbCx = obbCx;
        this.obbCy = obbCy;
        this.obbCz = obbCz;
        this.obbHx = obbHx;
        this.obbHy = obbHy;
        this.obbHz = obbHz;
        this.obbRotation = obbRotation;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static ServerCollisionData fromShape(CollisionShapeData shape)
    {
        var type = shape.Type;
        var rotation = shape.Rotation;
        var isRotated = !isIdentityRotation(rotation);

        var cx = shape.Center.x;
        var cy = shape.Center.y;
        var cz = shape.Center.z;

        var minX = 0f;
        var minY = 0f;
        var minZ = 0f;
        var maxX = 0f;
        var maxY = 0f;
        var maxZ = 0f;

        var hasObb = false;
        var obbCx = 0f;
        var obbCy = 0f;
        var obbCz = 0f;
        var obbHx = 0f;
        var obbHy = 0f;
        var obbHz = 0f;
        Quaternion obbRotation = null;

        switch (type)
        {
            case Box:
                obbHx = shape.Size.x * 0.5f;
                obbHy = shape.Size.y * 0.5f;
                obbHz = shape.Size.z * 0.5f;
                obbCx = cx;
                obbCy = cy;
                obbCz = cz;
                obbRotation = rotation;
                hasObb = true;

                if (isRotated)
                {
                    var aabbData = new AABBData();
                    var rotateService = new AABBCollisionRotateService(aabbData);
                    rotateService.setAabbFromObb(cx, cy, cz, obbHx, obbHy, obbHz, rotation);
                    minX = aabbData.minX;
                    minY = aabbData.minY;
                    minZ = aabbData.minZ;
                    maxX = aabbData.maxX;
                    maxY = aabbData.maxY;
                    maxZ = aabbData.maxZ;
                }
                else
                {
                    minX = cx - obbHx;
                    minY = cy - obbHy;
                    minZ = cz - obbHz;
                    maxX = cx + obbHx;
                    maxY = cy + obbHy;
                    maxZ = cz + obbHz;
                }
                break;

            case Sphere:
                var radius = shape.Radius * shape.Scale.x;
                minX = cx - radius;
                minY = cy - radius;
                minZ = cz - radius;
                maxX = cx + radius;
                maxY = cy + radius;
                maxZ = cz + radius;
                break;

            case Capsule:
                var capsuleRadius = shape.Radius;
                var capsuleHeight = shape.Height;
                minX = cx - capsuleRadius;
                minY = cy - capsuleRadius;
                minZ = cz - capsuleRadius;
                maxX = cx + capsuleRadius;
                maxY = cy + capsuleHeight + capsuleRadius;
                maxZ = cz + capsuleRadius;
                break;
        }

        return new ServerCollisionData(
                shape.Name,
                hasObb,
                obbCx, obbCy, obbCz,
                obbHx, obbHy, obbHz,
                obbRotation,
                minX, minY, minZ,
                maxX, maxY, maxZ);
    }

    public ISFSObject toSfs()
    {
        var object = new SFSObject();

        object.putUtfString("shapeId", shapeId);
        object.putBool("hasObb", hasObb);

        if (hasObb)
        {
            object.putFloat("obbCx", obbCx);
            object.putFloat("obbCy", obbCy);
            object.putFloat("obbCz", obbCz);

            object.putFloat("obbHx", obbHx);
            object.putFloat("obbHy", obbHy);
            object.putFloat("obbHz", obbHz);

            if (obbRotation != null)
            {
                object.putFloat("obbQx", obbRotation.x);
                object.putFloat("obbQy", obbRotation.y);
                object.putFloat("obbQz", obbRotation.z);
                object.putFloat("obbQw", obbRotation.w);
            }
        }

        object.putFloat("minX", minX);
        object.putFloat("minY", minY);
        object.putFloat("minZ", minZ);

        object.putFloat("maxX", maxX);
        object.putFloat("maxY", maxY);
        object.putFloat("maxZ", maxZ);

        return object;
    }

    private static boolean isIdentityRotation(Quaternion rotation)
    {
        if (rotation == null)
            return true;

        return Math.abs(rotation.x) < ROTATION_EPSILON
                && Math.abs(rotation.y) < ROTATION_EPSILON
                && Math.abs(rotation.z) < ROTATION_EPSILON
                && Math.abs(Math.abs(rotation.w) - 1f) < ROTATION_EPSILON;
    }
}
