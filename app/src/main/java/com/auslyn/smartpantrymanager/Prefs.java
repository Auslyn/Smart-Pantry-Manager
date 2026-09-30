package com.auslyn.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around SharedPreferences for the settings screen. */
public class Prefs {
    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_ALERTS = "expiry_alerts";

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isDarkMode(Context c) { return prefs(c).getBoolean(KEY_DARK, false); }
    public static void setDarkMode(Context c, boolean on) { prefs(c).edit().putBoolean(KEY_DARK, on).apply(); }

    public static boolean expiryAlertsOn(Context c) { return prefs(c).getBoolean(KEY_ALERTS, true); }
    public static void setExpiryAlerts(Context c, boolean on) { prefs(c).edit().putBoolean(KEY_ALERTS, on).apply(); }
}
