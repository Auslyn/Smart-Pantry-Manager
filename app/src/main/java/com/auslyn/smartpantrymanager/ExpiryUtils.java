package com.auslyn.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpiryUtils {

    public static final int SOON_DAYS = 3;
    private static final String FORMAT = "yyyy-MM-dd";

    public static String format(Calendar cal) {
        return new SimpleDateFormat(FORMAT, Locale.US).format(cal.getTime());
    }

    /** Days from today until the date (negative = already expired), or null if no valid date. */
    public static Integer daysUntil(String date) {
        if (date == null || date.trim().isEmpty()) return null;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(FORMAT, Locale.US);
            fmt.setLenient(false);
            Date expiry = fmt.parse(date.trim());

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diff = expiry.getTime() - today.getTimeInMillis();
            return (int) TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isValid(String date) {
        return date == null || date.trim().isEmpty() || daysUntil(date) != null;
    }
}
