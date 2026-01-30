package com.a51integrated.sfs2x.data;

import org.joml.Vector3f;

public class Ray
{
    public Vector3f origin = new Vector3f();
    public Vector3f direction = new Vector3f();
    public float maxDistance;
    public int layerMask;

    public void clear()
    {
        origin.set(0, 0, 0);
        direction.set(0, 0, 0);
        maxDistance = 0;
        layerMask = 0;
    }
}
