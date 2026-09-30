package com.auslyn.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite storage for the pantry, the recipes and each recipe's ingredients.
 *
 * Tables:
 *   pantry(id, name, quantity, unit, category, expiry)
 *   recipes(id, name, instructions, video_url)
 *   recipe_ingredients(id, recipe_id -> recipes.id, name, quantity, unit)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "category TEXT, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "instructions TEXT NOT NULL, "
                + "video_url TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL REFERENCES " + TABLE_RECIPES + "(id) ON DELETE CASCADE, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL)");

        seedRecipes(db);
        seedPantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------------------------------------------------------------- Pantry CRUD

    public long addPantryItem(PantryItem item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item),
                "id = ?", new String[]{String.valueOf(item.getId())});
    }

    public void deletePantryItem(int id) {
        getWritableDatabase().delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    public void clearAllPantry() {
        getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    public PantryItem getPantryItem(int id) {
        Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? readPantry(c) : null;
        } finally {
            c.close();
        }
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) list.add(readPantry(c));
        } finally {
            c.close();
        }
        return list;
    }

    public int countPantryItems() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE_PANTRY, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    /** Wipes the pantry and puts the starter items back. */
    public void resetPantryToSample() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, null, null);
        seedPantry(db);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("category", item.getCategory());
        v.put("expiry", item.getExpiryDate());
        return v;
    }

    private PantryItem readPantry(Cursor c) {
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("category")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ---------------------------------------------------------------- Recipes

    /** Returns every recipe with its ingredient list already filled in. */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Integer, Recipe> byId = new LinkedHashMap<>();

        Cursor rc = db.query(TABLE_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC");
        try {
            while (rc.moveToNext()) {
                Recipe r = readRecipe(rc);
                byId.put(r.getId(), r);
            }
        } finally {
            rc.close();
        }

        Cursor ic = db.query(TABLE_INGREDIENTS, null, null, null, null, null, "id ASC");
        try {
            while (ic.moveToNext()) {
                Recipe owner = byId.get(ic.getInt(ic.getColumnIndexOrThrow("recipe_id")));
                if (owner != null) owner.getIngredients().add(readIngredient(ic));
            }
        } finally {
            ic.close();
        }
        return new ArrayList<>(byId.values());
    }

    public Recipe getRecipe(int id) {
        for (Recipe r : getAllRecipes()) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    private Recipe readRecipe(Cursor c) {
        return new Recipe(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("instructions")),
                c.getString(c.getColumnIndexOrThrow("video_url")));
    }

    private RecipeIngredient readIngredient(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")));
    }

    // ---------------------------------------------------------------- Seed data

    private void seedPantry(SQLiteDatabase db) {
        addSeedPantry(db, "Bread", 6, "pcs", "Bakery", 2);
        addSeedPantry(db, "Cheese", 250, "g", "Dairy", 9);
        addSeedPantry(db, "Eggs", 6, "pcs", "Dairy", 12);
        addSeedPantry(db, "Butter", 200, "g", "Dairy", 30);
        addSeedPantry(db, "Milk", 1, "l", "Dairy", 1);
        addSeedPantry(db, "Tomatoes", 4, "pcs", "Vegetables", 4);
        addSeedPantry(db, "Garlic", 5, "pcs", "Vegetables", 20);
    }

    private void addSeedPantry(SQLiteDatabase db, String name, double qty, String unit,
                               String category, int daysUntilExpiry) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysUntilExpiry);
        db.insert(TABLE_PANTRY, null,
                toValues(new PantryItem(name, qty, unit, category, ExpiryUtils.format(cal))));
    }

    /** Each ingredient is written as "name:quantity:unit". */
    private void seedRecipes(SQLiteDatabase db) {
        addSeedRecipe(db, "Cheese Toast",
                "Heat the grill to high.\nLay the bread on a tray and cover with sliced cheese.\nGrill for 2-3 minutes until bubbling and golden.",
                "bread:2:pcs", "cheese:50:g");
        addSeedRecipe(db, "Scrambled Eggs",
                "Whisk the eggs with the milk and a pinch of salt.\nMelt the butter in a pan over low heat.\nPour in the eggs and stir gently until just set.",
                "egg:3:pcs", "butter:10:g", "milk:30:ml");
        addSeedRecipe(db, "Cheese Omelette",
                "Beat the eggs well.\nMelt the butter and pour in the eggs.\nAdd the cheese, fold the omelette in half and cook for one more minute.",
                "egg:3:pcs", "cheese:50:g", "butter:10:g");
        addSeedRecipe(db, "French Toast",
                "Whisk the eggs and milk in a shallow dish.\nSoak each slice of bread for 20 seconds per side.\nFry in a dry pan until golden on both sides.",
                "bread:4:pcs", "egg:2:pcs", "milk:100:ml");
        addSeedRecipe(db, "Garlic Bread",
                "Mash the butter with finely chopped garlic.\nSpread over the bread slices.\nBake at 200 C for 8 minutes until crisp.",
                "bread:4:pcs", "butter:40:g", "garlic:3:pcs");
        addSeedRecipe(db, "Tomato Toastie",
                "Slice the tomato thinly.\nLayer tomato and cheese between two slices of bread.\nToast in a pan or sandwich press until the cheese melts.",
                "bread:2:pcs", "tomato:1:pcs", "cheese:60:g");
        addSeedRecipe(db, "Tomato Soup",
                "Chop the tomatoes, onion and garlic.\nSimmer everything with a cup of water for 20 minutes.\nBlend until smooth and season to taste.",
                "tomato:4:pcs", "onion:1:pcs", "garlic:2:pcs");
        addSeedRecipe(db, "Tomato Pasta",
                "Boil the pasta until al dente.\nFry the garlic, add chopped tomatoes and simmer for 10 minutes.\nToss the pasta through the sauce.",
                "pasta:200:g", "tomato:3:pcs", "garlic:2:pcs");
        addSeedRecipe(db, "Mac and Cheese",
                "Cook the pasta and drain.\nMelt the butter, stir in the milk and grated cheese until smooth.\nMix in the pasta and serve hot.",
                "pasta:200:g", "cheese:150:g", "milk:250:ml", "butter:20:g");
        addSeedRecipe(db, "Egg Fried Rice",
                "Cook the rice and let it cool.\nScramble the eggs in a hot pan and set aside.\nFry the onion and carrot, add the rice and eggs and stir well.",
                "rice:300:g", "egg:2:pcs", "onion:1:pcs", "carrot:1:pcs");
        addSeedRecipe(db, "Fluffy Pancakes",
                "Whisk the flour, eggs and milk into a smooth batter.\nMelt the butter and stir it in.\nCook ladles of batter in a pan until bubbles form, then flip.",
                "flour:200:g", "egg:2:pcs", "milk:300:ml", "butter:20:g");
        addSeedRecipe(db, "Banana Pancakes",
                "Mash the bananas in a bowl.\nBeat in the eggs and flour.\nFry small spoonfuls until golden on both sides.",
                "banana:2:pcs", "egg:2:pcs", "flour:100:g");
        addSeedRecipe(db, "Creamy Mash",
                "Peel and boil the potatoes until soft.\nDrain and mash with the butter.\nStir in the warm milk until creamy.",
                "potato:500:g", "butter:30:g", "milk:100:ml");
        addSeedRecipe(db, "Loaded Baked Potato",
                "Prick the potatoes and bake at 200 C for 1 hour.\nSplit open and mash in the butter.\nTop with grated cheese and return to the oven for 5 minutes.",
                "potato:2:pcs", "cheese:80:g", "butter:20:g");
        addSeedRecipe(db, "Veggie Rice Bowl",
                "Cook the rice.\nStir-fry the sliced carrots, onion and garlic until tender.\nServe the vegetables over the rice.",
                "rice:200:g", "carrot:2:pcs", "onion:1:pcs", "garlic:2:pcs");
        addSeedRecipe(db, "Chicken Rice Bowl",
                "Dice the chicken and fry with the sliced onion until cooked through.\nCook the rice separately.\nServe the chicken on top of the rice.",
                "chicken breast:300:g", "rice:250:g", "onion:1:pcs");
        addSeedRecipe(db, "Tuna Pasta",
                "Boil the pasta.\nMix the flaked tuna with chopped tomatoes.\nToss with the hot pasta and serve.",
                "pasta:200:g", "tuna:1:pcs", "tomato:2:pcs");
        addSeedRecipe(db, "Egg Noodle Stir Fry",
                "Cook the noodles and drain.\nScramble the eggs in a hot pan.\nAdd the carrot, garlic and noodles and toss for 3 minutes.",
                "noodles:200:g", "egg:2:pcs", "carrot:1:pcs", "garlic:2:pcs");
        addSeedRecipe(db, "Lentil Soup",
                "Fry the onion, carrots and garlic in a pot.\nAdd the lentils, chopped tomatoes and 1 litre of water.\nSimmer for 30 minutes until the lentils are soft.",
                "lentil:200:g", "onion:1:pcs", "carrot:2:pcs", "garlic:2:pcs", "tomato:2:pcs");
        addSeedRecipe(db, "Banana Porridge",
                "Heat the oats and milk together, stirring, for 5 minutes.\nSlice the banana on top.\nServe warm.",
                "oats:80:g", "milk:300:ml", "banana:1:pcs");
    }

    private void addSeedRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("instructions", steps);
        rv.put("video_url", videoSearchUrl(name));
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (String line : ingredients) {
            String[] p = line.split(":");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(TABLE_INGREDIENTS, null, iv);
        }
    }

    private String videoSearchUrl(String recipeName) {
        try {
            return "https://www.youtube.com/results?search_query="
                    + URLEncoder.encode(recipeName + " recipe", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "https://www.youtube.com";
        }
    }
}
