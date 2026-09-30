package com.auslyn.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic of the app: the strict-matching rule.
 *
 * A recipe is only suggested when EVERY ingredient is in the pantry in at
 * least the required amount. Names are compared after normalising case and
 * plurals ("Tomatoes" == "tomato") and amounts after converting units
 * (1 kg covers 250 g).
 */
public class RecipeMatcher {

    /** Lower-cases, trims and turns a simple plural into its singular form. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (s.length() <= 3) return s;

        if (s.endsWith("ies")) {
            return s.substring(0, s.length() - 3) + "y";      // berries -> berry
        }
        if (s.endsWith("oes") || s.endsWith("ches") || s.endsWith("shes") || s.endsWith("xes")) {
            return s.substring(0, s.length() - 2);            // tomatoes -> tomato
        }
        if (s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("us")) {
            return s.substring(0, s.length() - 1);            // eggs -> egg
        }
        return s;
    }

    /** Total pantry stock per ingredient, keyed by "name|dimension" and held in base units. */
    static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = stockKey(item.getName(), item.getUnit());
            double amount = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Double existing = stock.get(key);
            stock.put(key, existing == null ? amount : existing + amount);
        }
        return stock;
    }

    private static String stockKey(String name, String unit) {
        return normalizeName(name) + "|" + UnitConverter.dimensionOf(unit);
    }

    public static MatchResult match(Recipe recipe, List<PantryItem> pantry) {
        return match(recipe, buildStock(pantry));
    }

    private static MatchResult match(Recipe recipe, Map<String, Double> stock) {
        MatchResult result = new MatchResult(recipe);
        for (RecipeIngredient needed : recipe.getIngredients()) {
            Double have = stock.get(stockKey(needed.getName(), needed.getUnit()));
            double required = UnitConverter.toBase(needed.getQuantity(), needed.getUnit());
            if (have == null || have + 0.0001 < required) {
                result.getMissing().add(needed);
            }
        }
        return result;
    }

    /** Recipes the user can cook right now: nothing missing. */
    public static List<MatchResult> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> stock = buildStock(pantry);
        List<MatchResult> out = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult r = match(recipe, stock);
            if (r.isFullMatch()) out.add(r);
        }
        return out;
    }

    /** Optional bonus list: recipes missing exactly one ingredient. */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> stock = buildStock(pantry);
        List<MatchResult> out = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult r = match(recipe, stock);
            if (r.getMissing().size() == 1) out.add(r);
        }
        return out;
    }
}
