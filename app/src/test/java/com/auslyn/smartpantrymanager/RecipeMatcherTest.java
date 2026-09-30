package com.auslyn.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule (brief section 2.3). */
public class RecipeMatcherTest {

    private Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe r = new Recipe(1, name, "step", "");
        r.getIngredients().addAll(Arrays.asList(ingredients));
        return r;
    }

    private PantryItem item(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, "Other", "");
    }

    private final Recipe cheeseToast = recipe("Cheese Toast",
            new RecipeIngredient("bread", 2, "pcs"),
            new RecipeIngredient("cheese", 50, "g"));

    @Test
    public void allIngredientsPresent_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(item("Bread", 4, "pcs"), item("Cheese", 100, "g"));
        assertTrue(RecipeMatcher.match(cheeseToast, pantry).isFullMatch());
    }

    @Test
    public void oneIngredientMissing_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(item("Bread", 4, "pcs"));
        assertFalse(RecipeMatcher.match(cheeseToast, pantry).isFullMatch());
        assertEquals(0, RecipeMatcher.strictMatches(Arrays.asList(cheeseToast), pantry).size());
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(item("Bread", 1, "pcs"), item("Cheese", 100, "g"));
        assertFalse(RecipeMatcher.match(cheeseToast, pantry).isFullMatch());
    }

    @Test
    public void singularAndPluralNamesMatch() {
        Recipe soup = recipe("Soup", new RecipeIngredient("tomato", 2, "pcs"));
        List<PantryItem> pantry = Arrays.asList(item("Tomatoes", 3, "pcs"));
        assertTrue(RecipeMatcher.match(soup, pantry).isFullMatch());
    }

    @Test
    public void differentUnitsAreConverted() {
        Recipe r = recipe("Mash", new RecipeIngredient("potato", 500, "g"));
        assertTrue(RecipeMatcher.match(r, Arrays.asList(item("Potato", 1, "kg"))).isFullMatch());
        assertFalse(RecipeMatcher.match(r, Arrays.asList(item("Potato", 0.25, "kg"))).isFullMatch());
    }

    @Test
    public void incompatibleUnitsDoNotMatch() {
        Recipe r = recipe("Milky", new RecipeIngredient("milk", 100, "ml"));
        assertFalse(RecipeMatcher.match(r, Arrays.asList(item("Milk", 2, "pcs"))).isFullMatch());
    }

    @Test
    public void duplicateStockIsAdded() {
        Recipe r = recipe("Rice", new RecipeIngredient("rice", 300, "g"));
        List<PantryItem> pantry = Arrays.asList(item("Rice", 200, "g"), item("rice", 150, "g"));
        assertTrue(RecipeMatcher.match(r, pantry).isFullMatch());
    }

    @Test
    public void almostThereListsOnlySingleMissingIngredient() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(cheeseToast);
        List<PantryItem> pantry = Arrays.asList(item("Bread", 4, "pcs"));
        List<MatchResult> almost = RecipeMatcher.almostThere(recipes, pantry);
        assertEquals(1, almost.size());
        assertEquals("cheese", almost.get(0).getMissing().get(0).getName());
    }

    @Test
    public void normalizeNameHandlesCommonPlurals() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName(" Eggs "));
        assertEquals("berry", RecipeMatcher.normalizeName("berries"));
        assertEquals("cheese", RecipeMatcher.normalizeName("Cheese"));
        assertEquals("hummus", RecipeMatcher.normalizeName("hummus"));
    }
}
