package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.AABBData;
import com.a51integrated.sfs2x.data.PlayerCollider;
import com.a51integrated.sfs2x.data.Quaternion;

public class AABBCollisionRotateService
{
    private static final float ROTATION_EPSILON = 1e-6f;
    private final AABBData aabbData;

    public AABBCollisionRotateService(AABBData aabbData) {
        this.aabbData = aabbData;
    }

    public void setAabbFromObb(float cx, float cy, float cz, float hx, float hy, float hz, Quaternion rotation)
    {
        var qx = rotation.x;
        var qy = rotation.y;
        var qz = rotation.z;
        var qw = rotation.w;

        var lenSqr = qx * qx + qy * qy + qz * qz + qw * qw;

        if (lenSqr < ROTATION_EPSILON)
        {
            aabbData.setCenterHalfExtents(cx, cy, cz, hx, hy, hz);
            return;
        }

        var invLen = 1f / (float)Math.sqrt(lenSqr);
        qx *= invLen;
        qy *= invLen;
        qz *= invLen;
        qw *= invLen;

        var xx = qx * qx;
        var yy = qy * qy;
        var zz = qz * qz;
        var xy = qx * qy;
        var xz = qx * qz;
        var yz = qy * qz;
        var wx = qw * qx;
        var wy = qw * qy;
        var wz = qw * qz;

        var m00 = 1f - 2f * (yy + zz);
        var m01 = 2f * (xy - wz);
        var m02 = 2f * (xz + wy);

        var m10 = 2f * (xy + wz);
        var m11 = 1f - 2f * (xx + zz);
        var m12 = 2f * (yz - wx);

        var m20 = 2f * (xz - wy);
        var m21 = 2f * (yz + wx);
        var m22 = 1f - 2f * (xx + yy);

        var ax = Math.abs(m00) * hx + Math.abs(m01) * hy + Math.abs(m02) * hz;
        var ay = Math.abs(m10) * hx + Math.abs(m11) * hy + Math.abs(m12) * hz;
        var az = Math.abs(m20) * hx + Math.abs(m21) * hy + Math.abs(m22) * hz;

        aabbData.setCenterHalfExtents(cx, cy, cz, ax, ay, az);
    }

    public boolean intersectsCapsuleObb(PlayerCollider player, float cx, float cy, float cz,
                                         float hx, float hy, float hz, Quaternion rotation)
    {
        var bottomX = player.x - cx;
        var bottomY = player.y - cy;
        var bottomZ = player.z - cz;

        var topX = bottomX;
        var topY = bottomY + player.height;
        var topZ = bottomZ;

        var localBottom = rotateVectorByQuaternionInverse(bottomX, bottomY, bottomZ, rotation);
        var localTop = rotateVectorByQuaternionInverse(topX, topY, topZ, rotation);

        var expand = player.radius;
        var minX = -hx - expand;
        var minY = -hy - expand;
        var minZ = -hz - expand;
        var maxX = hx + expand;
        var maxY = hy + expand;
        var maxZ = hz + expand;

        return segmentIntersectsAabb(localBottom[0], localBottom[1], localBottom[2],
                localTop[0], localTop[1], localTop[2],
                minX, maxX, minY, maxY, minZ, maxZ);
    }

    private float[] rotateVectorByQuaternionInverse(float vx, float vy, float vz, Quaternion rotation)
    {
        var qx = -rotation.x;
        var qy = -rotation.y;
        var qz = -rotation.z;
        var qw = rotation.w;

        var lenSqr = qx * qx + qy * qy + qz * qz + qw * qw;
        if (lenSqr < ROTATION_EPSILON)
        {
            return new float[] { vx, vy, vz };
        }

        var invLen = 1f / (float)Math.sqrt(lenSqr);
        qx = qx * invLen;
        qy = qy * invLen;
        qz = qz * invLen;
        qw = qw * invLen;

        var tx = 2f * (qy * vz - qz * vy);
        var ty = 2f * (qz * vx - qx * vz);
        var tz = 2f * (qx * vy - qy * vx);

        return new float[]
        {
                vx + qw * tx + (qy * tz - qz * ty),
                vy + qw * ty + (qz * tx - qx * tz),
                vz + qw * tz + (qx * ty - qy * tx)
        };
    }

    private boolean segmentIntersectsAabb(float ax, float ay, float az, float bx, float by, float bz,
                                          float minX, float maxX, float minY, float maxY, float minZ, float maxZ)
    {
        var dx = bx - ax;
        var dy = by - ay;
        var dz = bz - az;

        var range = new SegmentRange();
        range.set(0f, 1f);

        if (!intersectSegmentSlab(minX, maxX, ax, dx, range))
            return false;

        if (!intersectSegmentSlab(minY, maxY, ay, dy, range))
            return false;

        if (!intersectSegmentSlab(minZ, maxZ, az, dz, range))
            return false;

        return range.tMax >= range.tMin;
    }

    private boolean intersectSegmentSlab(float minAxis, float maxAxis, float originAxis,
                                         float directionAxis, SegmentRange range)
    {
        if (Math.abs(directionAxis) < ROTATION_EPSILON)
        {
            return originAxis >= minAxis && originAxis <= maxAxis;
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

    private static final class SegmentRange
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
