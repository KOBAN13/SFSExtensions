package com.a51integrated.sfs2x.data;

public class CollisionShapeData
{
    public String Name;
    public ECollisionShapeType Type;

    public int Layer;
    public String LayerName;
    public ECollisionCategory LayerCategory = ECollisionCategory.Default;
    public Quaternion Rotation;
    public Vector3 Scale;

    public Vector3 Center;
    public Vector3 Size;
    public float Radius;
    public float Height;

    public CollisionShapeData copy()
    {
        var copy = new CollisionShapeData();
        copy.Name = Name;
        copy.Type = Type;
        copy.Layer = Layer;
        copy.LayerName = LayerName;
        copy.LayerCategory = LayerCategory;
        copy.Rotation = Rotation == null ? null : new Quaternion(Rotation.x, Rotation.y, Rotation.z, Rotation.w);
        copy.Scale = Scale == null ? null : new Vector3(Scale.x, Scale.y, Scale.z);
        copy.Center = Center == null ? null : new Vector3(Center.x, Center.y, Center.z);
        copy.Size = Size == null ? null : new Vector3(Size.x, Size.y, Size.z);
        copy.Radius = Radius;
        copy.Height = Height;
        return copy;
    }
}
