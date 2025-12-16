package com.a51integrated.sfs2x.data;

public final class RaycastShapeData
{
    public float ox, oy, oz;
    public float dx, dy, dz;

    public RaycastShapeData set(float ox, float oy, float oz, float dx, float dy, float dz)
    {
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        return this;
    }
}
