package com.auslyn.smartpantrymanager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        db = new DatabaseHelper(this);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), nav);
        Ui.setupBottomNav(this, nav, R.id.nav_settings);

        MaterialSwitch switchDark = findViewById(R.id.switchDarkMode);
        MaterialSwitch switchAlerts = findViewById(R.id.switchExpiryAlerts);

        switchDark.setChecked(Prefs.isDarkMode(this));
        switchAlerts.setChecked(Prefs.expiryAlertsOn(this));

        switchDark.setOnCheckedChangeListener((button, checked) -> {
            Prefs.setDarkMode(this, checked);
            AppCompatDelegate.setDefaultNightMode(checked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });
        switchAlerts.setOnCheckedChangeListener((button, checked) ->
                Prefs.setExpiryAlerts(this, checked));

        findViewById(R.id.btnClearPantry).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.clear_title)
                        .setMessage(R.string.clear_message)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            db.clearAllPantry();
                            Toast.makeText(this, R.string.pantry_cleared, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show());

        findViewById(R.id.btnResetSample).setOnClickListener(v -> {
            db.resetPantryToSample();
            Toast.makeText(this, R.string.sample_restored, Toast.LENGTH_SHORT).show();
        });
    }
}
