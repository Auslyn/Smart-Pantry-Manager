package com.auslyn.smartpantrymanager;

/** A single line in a recipe, e.g. "Pasta - 200 g". */
public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
