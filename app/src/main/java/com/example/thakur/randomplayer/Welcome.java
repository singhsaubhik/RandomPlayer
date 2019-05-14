package com.example.thakur.randomplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.karumi.dexter.PermissionToken;

public class Welcome extends AppCompatActivity {

    final private int MY_PERMISSIONS_REQUEST = 0;
    private static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.RECORD_AUDIO};
    private boolean mBound=false;

    private ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            MusicService.LocalService playerBinder = (MusicService.LocalService) service;
            MusicService playerService = playerBinder.getMusicService();
            MyApp.setMyService(playerService);
            mBound=true;
            //Log.v(Welcome.class.getSimpleName(),"LAUNCH MAIN ACTIVITY");
            //startActivity(new Intent(Welcome.this, MainActivity.class));

            ///finish();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound=false;
        }
    };

    ImageView welcome_next;
    private final int REQUEST_CODE = 1;
    UserPreferenceHandler pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(ui);
        if (Build.VERSION.SDK_INT >= 21)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pref = new UserPreferenceHandler(Welcome.this);
        setContentView(R.layout.activity_welcome2);


        if(Build.VERSION.SDK_INT >=23) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (pref.getIsFirstRun() && !hasPermissions(Welcome.this, PERMISSIONS)) {
                        try {
                            permissionDetailsDialog();
                            pref.setIsFirstRun(false);
                        } catch (Exception e) {
                            RequestPermission();
                            pref.setIsFirstRun(false);
                        }
                    } else {
                        connectToService();
                        startActivity(new Intent(Welcome.this, MainActivity.class));
                        finish();

                    }
                }
            }, 200);

        }else{
            connectToService();
            startActivity(new Intent(Welcome.this, MainActivity.class));
            finish();
        }



    }



    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle("Permission required")
                .setMessage("To run all feature in this app we need some permissions please allow them.\nBelieve us there is no harm!!")
                .setIcon(R.mipmap.ic_launcher)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void permissionDetailsDialog(){
        new MaterialDialog.Builder(this)
                .title(R.string.permission_details_title)
                .content(R.string.permission_details_content)
                .positiveText("Ask")
                .negativeText(getString(R.string.cancel))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RequestPermission();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .show();
    }

    private void RequestPermission(){
        // Here, thisActivity is the current activity

        ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                MY_PERMISSIONS_REQUEST);

    }

    private void bindService(){
        //initialize music library instance
        // MusicLibrary.getInstance();

        startService(new Intent(this,MusicService.class));
        try {
            Intent playerServiceIntent = new Intent(this, MusicService.class);
            bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(mBound) {
                unbindService(playerServiceConnection);
                mBound=false;
            }

        }catch (Exception ignored){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {

                if(grantResults.length==0){
                    return;
                }
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    //&& grantResults[2] == PackageManager.PERMISSION_GRANTED
                        ) {
                    connectToService();
                    startActivity(new Intent(Welcome.this, MainActivity.class));
                    finish();

                } else {

                    if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        //READ PHONE STATE DENIED
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(this, getString(R.string.phone_stat_perm_required), Toast.LENGTH_LONG).show();
                        finish();
                    }else if(grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(this, getString(R.string.storage_perm_required), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
            break;

        }
    }


    private void connectToService() {
        if (!MusicService.isServiceRunning) {
            Intent serviceIntent = new Intent(Welcome.this, MusicService.class);
            bindService(serviceIntent, playerServiceConnection, BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }
}
