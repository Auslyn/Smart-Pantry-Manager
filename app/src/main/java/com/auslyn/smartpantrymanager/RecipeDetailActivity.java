package com.auslyn.smartpantrymanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

/** Full recipe: ingredients (ticked if in the pantry), method and a video link. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = db.getRecipe(getIntent().getIntExtra(EXTRA_RECIPE_ID, -1));
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), findViewById(R.id.bottomBar));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        MatchResult match = RecipeMatcher.match(recipe, db.getAllPantryItems());

        ((TextView) findViewById(R.id.tvRecipeName)).setText(recipe.getName());

        TextView status = findViewById(R.id.tvStatus);
        if (match.isFullMatch()) {
            status.setText(R.string.status_ready);
            status.setBackgroundResource(R.drawable.bg_badge_success);
            status.setTextColor(getColor(R.color.success));
        } else {
            status.setText(getResources().getQuantityString(
                    R.plurals.status_missing, match.getMissing().size(), match.getMissing().size()));
            status.setBackgroundResource(R.drawable.bg_badge_warning);
            status.setTextColor(getColor(R.color.warning));
        }

        LinearLayout ingredientList = findViewById(R.id.ingredientList);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecipeIngredient ing : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.row_ingredient, ingredientList, false);
            boolean missing = match.isMissing(ing);
            ((TextView) row.findViewById(R.id.tvIngName)).setText(RecipeAdapter.capitalise(ing.getName()));
            ((TextView) row.findViewById(R.id.tvIngAmount)).setText(
                    PantryAdapter.formatQuantity(ing.getQuantity()) + " " + ing.getUnit());
            TextView mark = row.findViewById(R.id.tvMark);
            mark.setText(missing ? "\u2717" : "\u2713");
            mark.setTextColor(getColor(missing ? R.color.danger : R.color.success));
            ingredientList.addView(row);
        }

        LinearLayout stepList = findViewById(R.id.stepList);
        String[] steps = recipe.getInstructions().split("\n");
        for (int i = 0; i < steps.length; i++) {
            View row = inflater.inflate(R.layout.row_step, stepList, false);
            ((TextView) row.findViewById(R.id.tvStepNumber)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.tvStepText)).setText(steps[i].trim());
            stepList.addView(row);
        }

        findViewById(R.id.btnWatchVideo).setOnClickListener(v -> openVideo(recipe.getVideoUrl()));
    }

    private void openVideo(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.video_error, Toast.LENGTH_SHORT).show();
        }
    }
}
