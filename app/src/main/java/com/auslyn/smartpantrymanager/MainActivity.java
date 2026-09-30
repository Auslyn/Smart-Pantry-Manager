package com.auslyn.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/** Home dashboard: quick stats and shortcuts into the rest of the app. */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView tvStatItems, tvStatExpiring, tvStatCook, tvExpiryBanner;
    private View expiryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        tvStatItems = findViewById(R.id.tvStatItems);
        tvStatExpiring = findViewById(R.id.tvStatExpiring);
        tvStatCook = findViewById(R.id.tvStatCook);
        tvExpiryBanner = findViewById(R.id.tvExpiryBanner);
        expiryCard = findViewById(R.id.cardExpiry);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), nav);
        Ui.setupBottomNav(this, nav, R.id.nav_home);

        findViewById(R.id.cardPantry).setOnClickListener(v ->
                startActivity(new Intent(this, PantryActivity.class)));
        findViewById(R.id.cardCook).setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        findViewById(R.id.cardAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPantryActivity.class)));
    }

    // Numbers are recalculated every time the screen comes back into view.
    @Override
    protected void onResume() {
        super.onResume();

        List<PantryItem> pantry = db.getAllPantryItems();
        int expiring = 0;
        for (PantryItem item : pantry) {
            Integer days = ExpiryUtils.daysUntil(item.getExpiryDate());
            if (days != null && days <= ExpiryUtils.SOON_DAYS) expiring++;
        }
        int cookable = RecipeMatcher.strictMatches(db.getAllRecipes(), pantry).size();

        tvStatItems.setText(String.valueOf(pantry.size()));
        tvStatExpiring.setText(String.valueOf(expiring));
        tvStatCook.setText(String.valueOf(cookable));

        boolean showAlert = Prefs.expiryAlertsOn(this) && expiring > 0;
        expiryCard.setVisibility(showAlert ? View.VISIBLE : View.GONE);
        if (showAlert) {
            tvExpiryBanner.setText(getResources().getQuantityString(
                    R.plurals.expiring_banner, expiring, expiring));
        }
    }
}
