package com.a51integrated.sfs2x.services.collision;

import com.a51integrated.sfs2x.data.collision.ECollisionCategory;

import java.util.Dictionary;
import java.util.Hashtable;

public class LayerCategoryMapService
{
    private final Dictionary<String, ECollisionCategory> categoryMap = new Hashtable<>();

    public LayerCategoryMapService()
    {
        //TODO: Отдавать json с именем и категорией чтоб каждый раз не заполнять
        categoryMap.put("Default", ECollisionCategory.Default);
        categoryMap.put("TransparentFX", ECollisionCategory.TransparentFX);
        categoryMap.put("Ignore Raycast", ECollisionCategory.IgnoreRaycast);
        categoryMap.put("Ground", ECollisionCategory.Ground);
        categoryMap.put("Water", ECollisionCategory.Water);
        categoryMap.put("Obstacle", ECollisionCategory.Obstacle);
        categoryMap.put("Player", ECollisionCategory.Player);
        categoryMap.put("RemotePlayer", ECollisionCategory.RemotePlayer);
    }

    public boolean layerInMask(int unityLayer, int layerMask)
    {
        if (unityLayer < 0 || unityLayer > 31)
            return false;

        return (layerMask & (1 << unityLayer)) != 0;
    }

    public ECollisionCategory getCategory(String category)
    {
        return categoryMap.get(category);
    }
}
