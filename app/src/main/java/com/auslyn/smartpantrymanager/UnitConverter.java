package com.auslyn.smartpantrymanager;

import java.util.Locale;

/**
 * Converts the units the app supports into a base unit so that, for example,
 * 1 kg in the pantry can satisfy a recipe that asks for 250 g.
 */
public class UnitConverter {

    public static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    public static final int COUNT = 0;
    public static final int MASS = 1;
    public static final int VOLUME = 2;

    /** Which kind of measurement a unit is (count, mass or volume). */
    public static int dimensionOf(String unit) {
        switch (clean(unit)) {
            case "g":
            case "kg":
                return MASS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return VOLUME;
            default:
                return COUNT;
        }
    }

    /** Converts a quantity to grams, millilitres or pieces. */
    public static double toBase(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg":
            case "l":
                return quantity * 1000;
            case "tsp":
                return quantity * 5;
            case "tbsp":
                return quantity * 15;
            case "cup":
                return quantity * 250;
            default:
                return quantity;
        }
    }

    private static String clean(String unit) {
        return unit == null ? "pcs" : unit.trim().toLowerCase(Locale.ROOT);
    }
}
