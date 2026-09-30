package com.auslyn.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private final int id;
    private final String name;
    private final String instructions; // one step per line
    private final String videoUrl;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(int id, String name, String instructions, String videoUrl) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.videoUrl = videoUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public String getVideoUrl() { return videoUrl; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
