package com.a51integrated.sfs2x.data.collision;

public final class PlayerCollider
{
    public float x, y, z;
    public float radius, height;

    public PlayerCollider set(float x, float y, float z, float radius, float height)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.height = height;

        return this;
    }
}
