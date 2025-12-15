package com.a51integrated.sfs2x.data;

public final class AABBData
{
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public void setCenterHalfExtents(float cx, float cy, float cz, float hx, float hy, float hz)
    {
        minX = cx - hx;
        maxX = cx + hx;
        minY = cy - hy;
        maxY = cy + hy;
        minZ = cz - hz;
        maxZ = cz + hz;
    }
}
