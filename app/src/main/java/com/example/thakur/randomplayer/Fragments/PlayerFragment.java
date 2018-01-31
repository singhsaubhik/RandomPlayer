package com.example.thakur.randomplayer.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.thakur.randomplayer.Adapters.DiscreteViewAdapter;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.ImageUtils;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Song;

import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;



import java.io.File;
import java.util.ArrayList;


import me.tankery.lib.circularseekbar.CircularSeekBar;

/**
 * Created by Thakur on 05-10-2017
 */

public class PlayerFragment extends Fragment implements View.OnClickListener, CircularSeekBar.OnCircularSeekBarChangeListener {

    private MusicService mService;
    private TextView tv, songTitle, artist_name, totalTime;
    private ArrayList<Song> songList;
    private Context context;
    private ImageView image, imageView, next, prev;
    private ImageView repeat, shuffle;
    private CircularSeekBar seekBar;



    //discrete recyclerview
    private MultiSnapRecyclerView discrete;

    public static final String trackChange = "com.example.thakur.randomplayer.Services.trackchangelistener";

    public static final String PLAY_PAUSE = "com.example.thakur.randomplayer.Services.playpause";
    public static final String NEXT_TRACK = "com.example.thakur.randomplayer.Services.nexttrack";
    public static final String PREV_TRACK = "com.example.thakur.randomplayer.Services.prevtrack";

    private Handler mHandler;

    private Intent intent;
    private FloatingActionButton playpause;


    int height, width;
    UserPreferenceHandler pref;

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {

                case MusicService.RECIEVE_SONG:
                    //updatePlayer(intent);
                    break;

                case trackChange:
                    if (mService.getSongPos() == songList.size() - 1) {
                        stopRepeating();
                    } else {
                        //initUI(mService.getSongPos());
                        updateUI(mService.getSongPos(),true);
                    }
                    break;

                case PLAY_PAUSE:
                    //notificationPlayPause();
                    if (!MusicService.isPlaying) {
                        playpause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                        stopRepeating();
                    } else {
                        playpause.setImageResource(R.drawable.ic_pause_white_36dp);
                        //updateUI(mService.getSongPos());
                    }
                    updatePlayPause();
                    break;

                case NEXT_TRACK:
                    notificationNext();
                    break;

                case PREV_TRACK:
                    notificationPrevious();
                    break;

                case MusicService.PLAYING_STATUS_CHANGED:
                    updateUI(mService.getSongPos(),false);
                    //stopRepeating();
                    break;

            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));

        }
        View view = inflater.inflate(R.layout.player_fragment, container, false);


        context = view.getContext();


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(view.getContext(), "View created and sngPos="+songPos, Toast.LENGTH_SHORT).show();
        mService = MyApp.getMyService();

        /*android.support.v7.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("");
        }*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(trackChange);
        filter.addAction(PLAY_PAUSE);
        filter.addAction(NEXT_TRACK);
        filter.addAction(PREV_TRACK);
        filter.addAction(MusicService.PLAYING_STATUS_CHANGED);
        context.registerReceiver(mReciever, filter);
        pref = new UserPreferenceHandler(context);
        //view.getContext().sendBroadcast(new Intent(MusicService.GET_SONG));
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(play);


        intent = getActivity().getIntent();

        mHandler = new Handler();

        //time = MusicService.time;


        image = view.findViewById(R.id.album_art);
        imageView = view.findViewById(R.id.album);

        songList = new ArrayList<>();

        songList = MyApp.getMyService().getSongList();

        ///bottomBlur = view.findViewById(R.id.blurView);
        tv = view.findViewById(R.id.cuurentText);
        totalTime = view.findViewById(R.id.totalText);
        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(this);
        songTitle = view.findViewById(R.id.song_title);
        songTitle.setSelected(true);
        playpause = view.findViewById(R.id.playpuse);
        artist_name = view.findViewById(R.id.artist_name_player);

        playpause.setOnClickListener(this);
        next = view.findViewById(R.id.next);
        prev = view.findViewById(R.id.prev);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        repeat = view.findViewById(R.id.repeat);
        shuffle = view.findViewById(R.id.shuffle);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        DiscreteViewAdapter adapter = new DiscreteViewAdapter(songList);

        discrete = view.findViewById(R.id.discrete_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        discrete.setLayoutManager(manager);
        // InfiniteScrollAdapter infiniteAdapter = InfiniteScrollAdapter.wrap(adapter);
        discrete.setAdapter(adapter);


        updateUI(mService.getSongPos(),true);


    }


    Runnable runnable = new Runnable() {

        @Override
        public void run() {


            {
                if (mService.isReallyPlaying()) {


                    seekBar.setProgress(seekBar.getProgress() + 1000);
                    tv.setText(songList.get(mService.getSongPos()).getFormatedTime((long) seekBar.getProgress()));
                    //sec++;
                    mHandler.postDelayed(this, 1000);
                    //seekBar.postDelayed(this,1000);
                }
            }
        }
    };


    private void updateUI(int pos,boolean loadImage) {


        totalTime.setText("" + songList.get(pos).getDuration());
        songTitle.setText("" + songList.get(pos).getName());
        artist_name.setText("" + songList.get(pos).getArtist());

        String path = ListSongs.getAlbumArt(context, songList.get(pos).getAlbumId());

        if (loadImage) {
            if (Utils.isPathValid(path)) {
                Glide.with(this).asBitmap().load(new File(path))
                        .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Palette.Builder p = Palette.from(resource);

                                p.generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {
                                        Palette.Swatch swatch = palette.getMutedSwatch();
                                        try {
                                            seekBar.setCircleProgressColor(swatch.getRgb());
                                            seekBar.setPointerColor(swatch.getRgb());
                                        }catch (Exception e){
                                            seekBar.setCircleProgressColor(getResources().getColor(R.color.primary));
                                        }

                                    }
                                });
                                image.setImageBitmap(resource);
                                doAlbumArtStuff(resource);
                            }
                        });

            } else {
                Glide.with(this).asBitmap().load(R.drawable.defualt_art2)
                        .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Palette.Builder p = Palette.from(resource);

                                p.generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {
                                        Palette.Swatch swatch = palette.getDominantSwatch();
                                        try {
                                            seekBar.setCircleProgressColor(swatch.getRgb());
                                        }catch (Exception e){
                                            seekBar.setCircleProgressColor(getResources().getColor(R.color.primary));
                                        }
                                    }
                                });
                                image.setImageBitmap(resource);
                                doAlbumArtStuff(resource);

                            }
                        });

            }
        }


        try {
            discrete.scrollToPosition(mService.getSongPos());
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seekBar.setMax(songList.get(pos).getDurationLong());
        seekBar.setProgress(mService.getCurrentPosition());

        //tv.setText("" + songList.get(pos).getDuration());

        updatePlayPause();

        if (pref.isShuffleEnabled()) {
            shuffle.setImageResource(R.drawable.ic_shuffle_selected_36dp);
        } else {
            shuffle.setImageResource(R.drawable.ic_shuffle_36dp);
        }

        if (pref.isRepeatOneEnabled()) {
            repeat.setImageResource(R.drawable.ic_repeat_one_24dp);
        } else if (pref.isRepeatAllEnabled()) {
            repeat.setImageResource(R.drawable.ic_repeat_selected_24dp);
        } else {
            repeat.setImageResource(R.drawable.ic_repeat_24dp);
        }

    }

    private void updatePlayPause() {
        if (mService.isReallyPlaying()) {
            playpause.setImageResource(R.drawable.ic_pause_white_24dp);
            stopRepeating();
            updateSeekbar();
        } else {
            playpause.setImageResource(R.drawable.ic_play_arrow_white2_24dp);
            stopRepeating();
        }
    }


    public void updateSeekbar() {

        //seekBar.postDelayed(runnable, 1000);

        mHandler.postDelayed(runnable, 1000);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context.unregisterReceiver(mReciever);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.playpuse:
                if (MusicService.isPlaying) {
                    //changeBecauseOfPlaypuase = true;
                    mService.playPause();
                    //updateUI(mService.getSongPos());
                    updatePlayPause();
                    //Log.v("TAG", "Playing");

                } else {
                    mService.playPause();
                    //updateUI(mService.getSongPos());
                    updatePlayPause();
                }

                //updateSeekbar(songPos);
                break;

            case R.id.next:
                handleNext();
                break;

            case R.id.prev:
                handlePrev();
                break;

            case R.id.repeat:
                handleRepeat();
                break;

            case R.id.shuffle:
                handleShuffle();
                break;

        }


    }


    @Override
    public void onResume() {
        //seekBar.setProgress(seekBar.getProgress());

        //updateUIPosition(mService.getSongPos());
        updateUI(mService.getSongPos(),true);
        super.onResume();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateUI(mService.getSongPos(),true);
    }

    @Override
    public void onPause() {
        stopRepeating();
        super.onPause();

    }


    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {

    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {

        mService.handleSeek(seekBar.getProgress());

    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {

    }


    private void stopRepeating() {
        mHandler.removeCallbacks(runnable);
    }


    private void handleRepeat() {
        if (!pref.isRepeatEnabled()) {
            pref.setRepeatAllEnable(true);
            repeat.setImageResource(R.drawable.ic_repeat_24dp);
            pref.setShuffleEnabled(false);
            //updateUI(mService.getSongPos(),false);
        } else if (pref.isRepeatAllEnabled()) {
            pref.setRepeatOneEnable(true);
            pref.setRepeatAllEnable(false);
            repeat.setImageResource(R.drawable.ic_repeat_selected_24dp);
            pref.setShuffleEnabled(false);
            //updateUI(mService.getSongPos());
        } else if (pref.isRepeatOneEnabled()) {
            pref.setRepeatAllEnable(false);
            pref.setRepeatOneEnable(false);
            repeat.setImageResource(R.drawable.ic_repeat_one_24dp);
        }

        updateUI(mService.getSongPos(),false);
    }

    private void handleShuffle() {
        if (!pref.isShuffleEnabled()) {
            pref.setShuffleEnabled(true);
            shuffle.setImageResource(R.drawable.ic_shuffle_selected_36dp);
            pref.setRepeatAllEnable(false);
            pref.setRepeatOneEnable(false);
            //updateUI(mService.getSongPos());
        } else {
            pref.setShuffleEnabled(false);
            shuffle.setImageResource(R.drawable.ic_shuffle_36dp);
            //updateUI(mService.getSongPos());
        }
        updateUI(mService.getSongPos(),false);
    }

    public void handleNext() {
        if (mService.getSongPos() == songList.size() - 1) {
            if (pref.isRepeatAllEnabled()) {
                mService.handleNextSong();
                updateUI(mService.getSongPos(),true);
            }

        } else {


            mService.handleNextSong();
            updateUI(mService.getSongPos(),true);
        }

    }

    private void handlePrev() {
        mService.handlePrevious();
        //initUI(mService.getSongPos());
        updateUI(mService.getSongPos(),true);
    }


    private void notificationNext() {
        if (mService.getSongPos() == songList.size() - 1) {
            if (pref.isRepeatAllEnabled()) {
                //mService.handleNextSong();
                //initUI(mService.getSongPos());
                updateUI(mService.getSongPos(),true);
            }

        } else {
            //mService.handleNextSong();
            //initUI(mService.getSongPos());
            updateUI(mService.getSongPos(),true);
        }
    }

    private void notificationPrevious() {
        //initUI(mService.getSongPos());
        updateUI(mService.getSongPos(),true);
    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (imageView.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    imageView.getDrawable(),
                                    result
                            });
                    imageView.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    imageView.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

    public void doAlbumArtStuff(Bitmap loadedImage) {
        setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
        blurredAlbumArt.execute(loadedImage);
    }


}
