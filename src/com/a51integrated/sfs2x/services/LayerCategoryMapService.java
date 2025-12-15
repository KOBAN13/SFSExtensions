package com.a51integrated.sfs2x.services;

import com.a51integrated.sfs2x.data.ECollisionCategory;

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
    }

    public ECollisionCategory getCategory(String category)
    {
        return categoryMap.get(category);
    }
}
