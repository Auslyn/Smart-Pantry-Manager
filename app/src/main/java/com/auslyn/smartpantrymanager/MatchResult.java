package com.auslyn.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

/** Outcome of checking one recipe against the pantry. */
public class MatchResult {
    private final Recipe recipe;
    private final List<RecipeIngredient> missing = new ArrayList<>();

    public MatchResult(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() { return recipe; }
    public List<RecipeIngredient> getMissing() { return missing; }

    /** True only when nothing at all is missing (the strict rule). */
    public boolean isFullMatch() { return missing.isEmpty(); }

    public boolean isMissing(RecipeIngredient ingredient) {
        return missing.contains(ingredient);
    }
}
