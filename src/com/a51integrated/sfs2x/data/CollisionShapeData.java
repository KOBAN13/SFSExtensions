package com.a51integrated.sfs2x.data;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CollisionShapeData
{
    public ECollisionShapeType Type;

    public Vector3f Position;
    public Quaternionf Rotation;
    public Vector3f Scale;

    public Vector3f Center;
    public Vector3f Size;
    public float Radius;
    public float Height;
    public int Direction;
}
