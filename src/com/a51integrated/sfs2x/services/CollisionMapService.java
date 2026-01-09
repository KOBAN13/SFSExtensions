package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.GameExtension;
import com.a51integrated.sfs2x.data.CollisionMapPayload;
import com.a51integrated.sfs2x.data.CollisionShapeData;
import com.a51integrated.sfs2x.data.ECollisionCategory;
import com.a51integrated.sfs2x.data.PlayerCollider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollisionMapService
{
    private final List<CollisionShapeData> shapes = new ArrayList<>();
    private final LayerCategoryMapService layerCategoryMapService = new LayerCategoryMapService();
    private final AABBCollisionService aabbService;
    private final PlayerCollider playerCollider = new PlayerCollider();
    private final RaycastService raycastService;
    private final GameExtension gameExtension;

    //TODO: SDK Parameters
    private float playerRadius = 0.5f;
    private float playerHeight = 3.5f;

    public CollisionMapService(String path, GameExtension game)
    {
        aabbService = new AABBCollisionService(game);

        this.gameExtension = game;

        var collisionMapPayload = DeserializeCollisionMap(path);

        assert collisionMapPayload != null;

        shapes.addAll(collisionMapPayload.Shapes);

        raycastService = new RaycastService(shapes, layerCategoryMapService, game);
    }

    public void clear()
    {
        shapes.clear();
    }

    public RaycastService getRaycastService() {
        return raycastService;
    }

    public List<CollisionShapeData> getShapes() {
        return shapes;
    }

    public boolean isColliding(float px, float py, float pz)
    {
        playerCollider.set(px, py, pz, playerRadius, playerHeight);

        //TODO: Оптимизировать нет смылса проверять коллизию с каждым обьектом на сцене
        for (var shape : shapes)
        {
            if (shape.LayerCategory == ECollisionCategory.Ground)
                continue;

            if (intersectsShape(shape))
            {
                gameExtension.trace("Collision Shape " + shape.Name + " is colliding");
                return true;
            }
        }

        return false;
    }

    private boolean intersectsShape(CollisionShapeData shape)
    {
        switch (shape.Type)
        {
            case Capsule:
                return aabbService.collisionCapsuleWithCapsule(shape, playerCollider);

            case Sphere:
                return aabbService.collisionCapsuleWithSphere(shape, playerCollider);

            case Box:
                return aabbService.collisionCapsuleWithBox(shape, playerCollider);
        }

        return aabbService.collisionCapsuleWithBox(shape, playerCollider);
    }

    private CollisionMapPayload DeserializeCollisionMap(String path)
    {
        var file = new File(path);
        var mapper = new ObjectMapper();

        try
        {
            var collisionMapPayload = mapper.readValue(file, CollisionMapPayload.class);

            for (var shape : collisionMapPayload.Shapes)
            {
                shape.LayerCategory = layerCategoryMapService.getCategory(shape.LayerName);
            }

            return collisionMapPayload;
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
