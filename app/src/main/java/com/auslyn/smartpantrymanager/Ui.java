package com.auslyn.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Shared screen helpers: system-bar insets and bottom navigation. */
public class Ui {

    /**
     * Pushes the header below the status bar and the bottom view above the
     * navigation bar, so the app looks right when drawn edge to edge.
     */
    public static void applyInsets(View root, View header, View bottom) {
        final int headerTop = header.getPaddingTop();
        final int bottomPad = bottom.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            header.setPadding(header.getPaddingLeft(), headerTop + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            bottom.setPadding(bottom.getPaddingLeft(), bottom.getPaddingTop(),
                    bottom.getPaddingRight(), bottomPad + bars.bottom);
            return insets;
        });
    }

    /** Wires the bottom navigation bar; each tab opens its own Activity. */
    public static void setupBottomNav(Activity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = PantryActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else if (id == R.id.nav_settings) {
                target = SettingsActivity.class;
            } else {
                target = MainActivity.class;
            }

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            if (!(activity instanceof MainActivity)) activity.finish();
            return true;
        });
    }
}
