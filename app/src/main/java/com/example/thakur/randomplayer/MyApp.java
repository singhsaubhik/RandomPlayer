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



    @Override
    public void onCreate() {
        instance = this;

        super.onCreate();





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
