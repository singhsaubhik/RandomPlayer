package com.example.thakur.randomplayer.Fragments.NowPlayingScreen;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Song;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.security.MessageDigest;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerScreen2 extends Fragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    private static final String trackChange = "com.example.thakur.randomplayer.Services.trackchangelistener";

    private Song song;
    private ImageView album_art, blur_album_art, prev, next;
    private MusicService service;
    private Activity activity;
    private RelativeLayout control_bg;
    private FloatingActionButton playpause;
    private SeekBar seekBar;

    public PlayerScreen2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return inflater.inflate(R.layout.fragment_player_screen2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            service = MyApp.getMyService();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        activity = getActivity();

        //Images
        album_art = view.findViewById(R.id.album_art_screen2);
        blur_album_art = view.findViewById(R.id.album_art_blurred);
        control_bg = view.findViewById(R.id.control_bg_screen2);

        //Controls
        playpause = view.findViewById(R.id.playpause_screen2);
        next = view.findViewById(R.id.next_screen2);
        prev = view.findViewById(R.id.prev_screen2);
        seekBar = view.findViewById(R.id.seekbar_screen2);


        //Controls Onclicks
        playpause.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(trackChange);
        activity.registerReceiver(mReciver, filter);

        updateUI(service.getCurrentSong(), service.isReallyPlaying(),0);

    }

    private void updateUI(Song song, boolean isPlaying,long progress) {
        String path = ListSongs.getAlbumArt(activity, song.getAlbumId());

        if (Utils.isPathValid(path)) {
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
                                    }catch (Exception e){
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
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateUI(service.getCurrentSong(), service.isReallyPlaying(),0);
    }

    BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case trackChange:
                    updateUI(service.getCurrentSong(), service.isReallyPlaying(),0);
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
