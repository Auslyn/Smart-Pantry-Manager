package com.auslyn.smartpantrymanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class SmartPantryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Restore the saved theme choice before any screen is drawn.
        AppCompatDelegate.setDefaultNightMode(Prefs.isDarkMode(this)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
