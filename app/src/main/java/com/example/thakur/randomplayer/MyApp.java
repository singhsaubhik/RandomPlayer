package com.example.thakur.randomplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.afollestad.appthemeengine.ATE;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;

/**
 * Created by Thakur on 19-10-2017
 */

public class MyApp extends Application {

    private static MusicService myService;
    private static MyApp instance;
    private static SharedPreferences pref;

    public static boolean isAppVisible = false;

    @Override
    public void onCreate() {
        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();



       /* if (!ATE.config(this, "light_theme").isConfigured()) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppThemeLight)
                    .primaryColorRes(R.color.colorPrimaryLight)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured()) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkTheme)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "light_theme_notoolbar").isConfigured()) {
            ATE.config(this, "light_theme_notoolbar")
                    .activityTheme(R.style.AppThemeLight)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorTransparent)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme_notoolbar").isConfigured()) {
            ATE.config(this, "dark_theme_notoolbar")
                    .activityTheme(R.style.AppThemeDark)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorPrimaryDarkTheme)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(true)
                    .usingMaterialDialogs(true)
                    .commit();
        }*/

    }
    public static SharedPreferences getPref(){
        return pref;
        // or return instance.getApplicationContext();
    }

    public static MyApp getInstance(){
        return instance;
    }

    public static MyApp getContext(){
        return instance;
    }

    public static void setMyService(MusicService mService){
        myService = mService;
    }

    public static MusicService getMyService(){
        return myService;
    }


}
