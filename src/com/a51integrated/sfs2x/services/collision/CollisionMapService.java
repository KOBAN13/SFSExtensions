package com.a51integrated.sfs2x.services.collision;

import com.a51integrated.sfs2x.data.math.Ray;
import com.a51integrated.sfs2x.extensions.GameExtension;
import com.a51integrated.sfs2x.data.collision.CollisionMapPayload;
import com.a51integrated.sfs2x.data.collision.CollisionShapeData;
import com.a51integrated.sfs2x.data.collision.ECollisionCategory;
import com.a51integrated.sfs2x.data.collision.PlayerCollider;
import com.a51integrated.sfs2x.data.math.Vector3;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CollisionMapService
{
    private final List<CollisionShapeData> shapes = new ArrayList<>();
    private final Map<Integer, CollisionShapeData> playerShapes = new ConcurrentHashMap<>();
    private final LayerCategoryMapService layerCategoryMapService = new LayerCategoryMapService();
    private final CollisionShapeData playerShapeTemplate;
    private final AABBCollisionService aabbService;
    private final PlayerCollider playerCollider = new PlayerCollider();
    private final RaycastService raycastService;
    private final GameExtension gameExtension;

    //TODO: SDK Parameters
    private final float playerRadius;
    private final float playerHeight;

    public CollisionMapService(
            String path,
            GameExtension game,
            RewindSnapshotService rewindSnapshotService
    )
    {
        aabbService = new AABBCollisionService(game);

        this.gameExtension = game;

        var collisionMapPayload = deserializeCollisionMap(path);

        assert collisionMapPayload != null;

        shapes.addAll(collisionMapPayload.Shapes);
        playerShapeTemplate = extractPlayerTemplate(shapes);

        playerRadius = playerShapeTemplate.Radius;
        playerHeight = playerShapeTemplate.Height;

        raycastService = new RaycastService(this, layerCategoryMapService, rewindSnapshotService, game);
    }

    public void clear() {
        shapes.clear();
        playerShapes.clear();
    }

    public RaycastService getRaycastService() {
        return raycastService;
    }

    public List<CollisionShapeData> getShapes() {
        return shapes;
    }

    public void registerPlayerShape(int userId, Vector3 center)
    {
        var shape = playerShapeTemplate.copy();
        shape.Name = buildPlayerShapeName(userId);
        shape.Center = new Vector3(center.x, center.y, center.z);
        playerShapes.put(userId, shape);
    }

    public void updatePlayerShapeCenter(int userId, float x, float y, float z)
    {
        var shape = playerShapes.get(userId);

        if (shape == null)
            return;

        if (shape.Center == null)
        {
            shape.Center = new Vector3(x, y, z);
            return;
        }

        shape.Center.x = x;
        shape.Center.y = y;
        shape.Center.z = z;
    }

    public void removePlayerShape(int userId) {
        playerShapes.remove(userId);
    }

    public List<CollisionShapeData> getPlayerShapes() {
        return new ArrayList<>(playerShapes.values());
    }

    public Iterable<Map.Entry<Integer, CollisionShapeData>> getPlayerShapeEntries() {
        return playerShapes.entrySet();
    }

    public boolean isGround(float px, float py, float pz)
    {
        playerCollider.set(px, py, pz, playerRadius, playerHeight);

        var groundLayerMask = layerCategoryMapService.getLayerMask(shapes, ECollisionCategory.Ground, ECollisionCategory.Obstacle);

        if (groundLayerMask == 0)
            return false;

        var ray = new Ray();
        ray.direction = new Vector3f(0f, 1f, 0f);
        ray.origin = new Vector3f(playerCollider.x, playerCollider.y, playerCollider.z);
        ray.maxDistance = 1f;
        ray.layerMask = groundLayerMask;

        var raycastHit = raycastService.raycast(ray, shapes);

        return raycastHit.getHit();
    }

    public boolean isColliding(int userId, float px, float py, float pz)
    {
        playerCollider.set(px, py, pz, playerRadius, playerHeight);

        //TODO: Оптимизировать нет смылса проверять коллизию с каждым обьектом на сцене
        for (var shape : shapes)
        {
            if (shape.LayerCategory == ECollisionCategory.Ground)
                continue;

            if (intersectsShape(shape))
            {
                return true;
            }
        }

        //TODO: Оптимизировать нет смылса проверять коллизию с каждым обьектом на сцене
        for (var entry : playerShapes.entrySet())
        {
            if (entry.getKey() == userId)
                continue;

            if (intersectsShape(entry.getValue()))
            {
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

    private CollisionMapPayload deserializeCollisionMap(String path)
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

    private CollisionShapeData extractPlayerTemplate(List<CollisionShapeData> shapeList)
    {
        var iterator = shapeList.iterator();

        while (iterator.hasNext())
        {
            var shape = iterator.next();

            if (shape.LayerCategory == ECollisionCategory.Player)
            {
                iterator.remove();
                return shape.copy();
            }
        }

        return null;
    }

    private String buildPlayerShapeName(int userId)
    {
        return "player:" + userId;
    }
}
