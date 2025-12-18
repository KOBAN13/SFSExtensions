package com.a51integrated.sfs2x.data;

import org.joml.Vector3f;

public final class RaycastHit
{
    private boolean hit;
    private float distance;
    private Vector3f point;

    public void clear()
    {
        hit = false;
        distance = 0;
        point = point.zero();
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
        this.point = point;
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
