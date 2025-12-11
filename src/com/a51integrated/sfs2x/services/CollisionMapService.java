package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.CollisionMapPayload;
import com.a51integrated.sfs2x.data.ECollisionShapeType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.List;

public class CollisionMapService
{
    private CollisionMapPayload collisionMapPayload;
    private Dictionary<ECollisionShapeType, AABB> staticBoundingBoxes;

    public CollisionMapService(String path)
    {
        collisionMapPayload = DeserializeCollisionMap(path);
    }

    private CollisionMapPayload DeserializeCollisionMap(String path)
    {
        var file = new File(path);
        var mapper = new ObjectMapper();

        try
        {
            return mapper.readValue(file, CollisionMapPayload.class);
        }
        catch (IOException e)
        {

        }

        return null;
    }
}
