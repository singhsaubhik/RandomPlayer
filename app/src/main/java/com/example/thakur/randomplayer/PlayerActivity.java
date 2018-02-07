package com.example.thakur.randomplayer;


import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.appthemeengine.ATEActivity;
import com.example.thakur.randomplayer.BaseActivity.BaseThemedActivity;
import com.example.thakur.randomplayer.Fragments.PlayerFragment;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.Helper;


import java.net.Inet4Address;

public class PlayerActivity extends BaseThemedActivity {


    public boolean isBind = false;
    public static boolean isRunning;
    int screenWidth, screenHeight;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(ui);
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }*/

        setContentView(R.layout.activity_player);


        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.player_fragment_container, new PlayerFragment());
        ft.commit();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isBind = false;

        isRunning = false;
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent mainIntent = new Intent(PlayerActivity.this, MainActivity.class);
            //mainIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainIntent);
        }

        //else {
        super.onBackPressed();
        //}
        //super.onBackPressed();
    }


}

