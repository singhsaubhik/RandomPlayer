package com.example.thakur.randomplayer.Utilities;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Thakur on 31-01-2018.
 */

public class Helper {

    public static String getATEKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }
}
