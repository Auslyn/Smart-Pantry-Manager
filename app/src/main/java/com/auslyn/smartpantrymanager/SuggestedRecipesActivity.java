package com.auslyn.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows recipes the user can cook right now (strict match). A second tab
 * lists recipes missing exactly one ingredient, kept clearly separate.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private View emptyState;
    private TextView tvEmptyTitle, tvEmptyText, tvSubtitle;
    private MaterialButtonToggleGroup toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        toggle = findViewById(R.id.toggleMode);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), nav);
        Ui.setupBottomNav(this, nav, R.id.nav_recipes);

        RecyclerView recycler = findViewById(R.id.recyclerViewRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), result -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, result.getRecipe().getId());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        toggle.check(R.id.btnReady);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) refresh();
        });
    }

    // Re-run the matching every time, since the pantry may have changed.
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Recipe> recipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();
        boolean ready = toggle.getCheckedButtonId() != R.id.btnAlmost;

        List<MatchResult> results = ready
                ? RecipeMatcher.strictMatches(recipes, pantry)
                : RecipeMatcher.almostThere(recipes, pantry);

        adapter.updateData(results, !ready);
        tvSubtitle.setText(ready
                ? getResources().getQuantityString(R.plurals.ready_count, results.size(), results.size())
                : getResources().getQuantityString(R.plurals.almost_count, results.size(), results.size()));

        emptyState.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        if (results.isEmpty()) {
            tvEmptyTitle.setText(ready ? R.string.no_match_title : R.string.no_almost_title);
            tvEmptyText.setText(ready ? R.string.no_match_text : R.string.no_almost_text);
        }
    }
}
