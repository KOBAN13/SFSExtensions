package com.a51integrated.sfs2x.data;

public class CollisionShapeData
{
    public String Name;
    public ECollisionShapeType Type;

    public int Layer;
    public String LayerName;
    public ECollisionCategory LayerCategory = ECollisionCategory.Default;
    public Vector3 Position;
    public Quaternion Rotation;
    public Vector3 Scale;

    public Vector3 Center;
    public Vector3 Size;
    public float Radius;
    public float Height;
    public int Direction;
}
