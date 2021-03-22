package com.roberts.magnificentmessageboard;
/*
 * @author Robert Roberts
 */
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * A collection of shared things
 */
public class Globals {
    private static SharedPreferences preferences = null;
    public static String PREF_TRIPCODE = "com.roberts.magnificentmessageboard.tripcode";

    /**
     * Allowing the whole program to access preferences. What's the worst that can happen?
     * @param pref This app's preferences
     */
    public static void SetPreferences(SharedPreferences pref) {
        // Only set if it hasn't already
        if (preferences == null) {
            preferences = pref;
        }
    }

    /**
     * Get the preferences object
     * @return The {@link SharedPreferences} object, or null if not set yet
     */
    public static SharedPreferences GetPreferences() {
        if (preferences != null) {
            return preferences;
        }
        return null;
    }

    /**
     * Shows a {@link Toast}, even if we are outside of the main thread
     * WHAT COULD GO WRONG
     * @param str String to show
     */
    public static void ShowToast(String str) {
        System.out.println("Toasting: "+str);
        // Get the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        // Run this in the main thread
        handler.post(() -> {
            if (MainActivity.getAppContext() != null) {
                Toast toast = Toast.makeText(MainActivity.getAppContext(), str, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

}
