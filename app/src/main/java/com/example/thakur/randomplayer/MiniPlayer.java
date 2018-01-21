package com.example.thakur.randomplayer;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MiniPlayer extends Fragment {

    private ArrayList<Song> list = new ArrayList<>();
    private Context context;

    private ImageView image, playpause;
    private TextView title;

    private MusicService mService;
    private boolean isBound = false;

    public MiniPlayer() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        context = view.getContext();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        image = view.findViewById(R.id.second_player_screen_image);
        title = view.findViewById(R.id.second_player_screen_title);
        playpause = view.findViewById(R.id.second_player_screen_playpause);
        connectToService();

        list = ListSongs.getSongList(context);
        mService = MyApp.getMyService();
        setImageAndTitle();
        setPayPuaseDrawable(mService.isReallyPlaying());
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePlayPause();
                setPayPuaseDrawable(mService.isReallyPlaying());
            }
        });


        super.onViewCreated(view, savedInstanceState);
    }

    private void setImageAndTitle() {
        String path = ListSongs.getAlbumArt(context, list.get(0).getAlbumId());
        try {
            Glide.with(this).load(new File(path))
                    .into(image);
            title.setText(list.get(0).getName());
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePlayPause() {
        try {
            Intent i = new Intent();
            i.setAction(MusicService.ACTION_PAUSE_SONG);
            context.sendBroadcast(i);
        } catch (Exception e) {

        }
    }

    private void setPayPuaseDrawable(boolean isPlaying){
        if(isPlaying){
            playpause.setImageResource(R.drawable.ic_pause_white_36dp);
        }else {
            playpause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
        }
    }

    private void connectToService() {

            Intent serviceIntent = new Intent(context, MusicService.class);
            context.bindService(serviceIntent, mConnection1, BIND_AUTO_CREATE);
            context.startService(serviceIntent);

    }

    ServiceConnection mConnection1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.LocalService lc = (MusicService.LocalService) iBinder;
            mService = lc.getMusicService();
            MyApp.setMyService(lc.getMusicService());
            isBound = true;
            //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            isBound = false;
        }
    };

    @Override
    public void onDestroy() {
        context.unbindService(mConnection1);
        isBound = false;
        super.onDestroy();
    }
}
