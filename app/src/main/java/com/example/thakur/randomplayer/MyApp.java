package com.example.thakur.randomplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.thakur.randomplayer.Services.MusicService;

/**
 * Created by Thakur on 19-10-2017.
 */

public class MyApp extends Application {

    private static MusicService myService;
    private static MyApp instance;
    private static SharedPreferences pref;

    @Override
    public void onCreate() {
        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();

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
