package com.a51integrated.sfs2x.data;

import org.joml.Vector3f;

public final class RaycastHit
{
    private boolean hit;
    private float distance;
    private final Vector3f point = new Vector3f();

    public Vector3f velocity = new Vector3f();

    public void clear()
    {
        hit = false;
        distance = 0;
        point.zero();
    }

    public void setHit(boolean hit)
    {
        this.hit = hit;
    }

    public void setDistance(float distance)
    {
        if (distance < 0)
            return;

        this.distance = distance;
    }

    public void setPoint(Vector3f point)
    {
        this.point.set(point);
    }

    public void setPoint(float x, float y, float z)
    {
        point.set(x, y, z);
    }

    public void copyFrom(RaycastHit other)
    {
        hit = other.hit;
        distance = other.distance;
        point.set(other.point);
    }

    public boolean getHit()
    {
        return hit;
    }

    public float getDistance()
    {
        return distance;
    }

    public Vector3f getPoint()
    {
        return point;
    }
}
