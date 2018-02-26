package com.example.thakur.randomplayer.Fragments.NowPlayingScreen;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.MiniPlayer;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerScreen2 extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String trackChange = "com.example.thakur.randomplayer.Services.trackchangelistener";
    private static final String PLAY_PAUSE = "com.example.thakur.randomplayer.Services.playpause";


    private ImageView album_art, blur_album_art, prev, next;
    private MusicService service;
    private Activity activity;
    private RelativeLayout control_bg;
    private android.support.design.widget.FloatingActionButton playpause;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private ArrayList<Song> songList = new ArrayList<>();
    private Song song = null;

    public PlayerScreen2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_player_screen2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            service = MyApp.getMyService();
        }catch (Exception e){
            e.printStackTrace();
        }
        activity = getActivity();





        IntentFilter filter = new IntentFilter();
        filter.addAction(trackChange);
        filter.addAction(PLAY_PAUSE);
        activity.registerReceiver(mReciver, filter);

        //Images
        album_art = view.findViewById(R.id.album_art_screen2);
        blur_album_art = view.findViewById(R.id.album_art_blurred);
        control_bg = view.findViewById(R.id.control_bg_screen2);

        //Controls
        playpause = view.findViewById(R.id.playpause_screen2);
        next = view.findViewById(R.id.next_screen2);
        prev = view.findViewById(R.id.prev_screen2);
        seekBar = view.findViewById(R.id.seekbar_screen2);
        seekBar.setProgress(0);
        seekBar.setMax(100);


        //Controls Onclicks
        playpause.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);


        updateUI(true);


    }

    private void updateUI(boolean loadImage) {
        if(service!=null){
            song = service.getCurrentSong();
        }
        String path = RandomUtils.getAlbumArt(activity, song.getAlbumId());

        if (RandomUtils.isPathValid(path)) {
            Glide.with(this).asBitmap().load(new File(path))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            album_art.setImageBitmap(resource);

                            blur_album_art.setImageBitmap(resource);

                            Palette.Builder p = Palette.from(resource);
                            p.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    Palette.Swatch s = palette.getDarkVibrantSwatch();
                                    try {
                                        control_bg.setBackgroundColor(s.getRgb());
                                    } catch (Exception e) {
                                        control_bg.setBackgroundColor(activity.getResources().getColor(R.color.primary_dark));
                                    }
                                }
                            });


                        }
                    });


        } else {
            Glide.with(this).load(R.drawable.defualt_art2)
                    .into(album_art);
        }

        if(service.isReallyPlaying()) {
            seekBar.setProgress(service.getCurrentPosition());
        }
        seekBar.setMax((int) song.getDurationLong());
        //seekBar.setProgress(mService.getCurrentPosition());
        updatePlayPause();


    }

    private void updatePlayPause() {
        if(service!=null) {
            if (!service.isReallyPlaying()) {
                playpause.setImageResource(R.drawable.ic_play_arrow_white2_24dp);
                stopRepeating();
            } else {
                playpause.setImageResource(R.drawable.ic_pause_white_24dp);
                updateSeekbar();
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //updateUI(service.g, service.isReallyPlaying());
    }

    BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case trackChange:
                    updateUI(true);
                    break;

                case PLAY_PAUSE:
                    updatePlayPause();
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (view == playpause) {

            service.playPause();
            updatePlayPause();
            activity.sendBroadcast(new Intent().setAction(MiniPlayer.miniplyerStatusChange));
        } else if (view == next) {
            service.handleNextSong();
            updateUI(true);
            activity.sendBroadcast(new Intent().setAction(MiniPlayer.miniPlayerTrackChange));
        } else if (view == prev) {
            service.handlePrevious();
            updateUI(true);
            activity.sendBroadcast(new Intent().setAction(MiniPlayer.miniPlayerTrackChange));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (service != null) {
            service.handleSeek(seekBar.getProgress());
            //seekBar.setProgress(seekBar.getProgress());
        }
    }


    public void updateSeekbar() {
        //seekBar.postDelayed(runnable, 1000);
        stopRepeating();
        mHandler.postDelayed(runnable, 1000);

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            {
                if (service.isReallyPlaying()) {
                    seekBar.setProgress(seekBar.getProgress() + 1000);
                    //tv.setText(songList.get(mService.getSongPos()).getFormatedTime((long)seekBar.getProgress()));
                    //sec++;
                    mHandler.postDelayed(this, 1000);
                    //seekBar.postDelayed(this,1000);
                }
            }
        }
    };

    private void stopRepeating() {
        mHandler.removeCallbacks(runnable);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.unregisterReceiver(mReciver);
    }
}
