package com.example.thakur.randomplayer.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur;
import com.example.thakur.randomplayer.Adapters.DiscreteViewAdapter;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Song;


import com.squareup.picasso.Picasso;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.Orientation;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;


import java.io.File;
import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;
import me.tankery.lib.circularseekbar.CircularSeekBar;

/**
 * Created by Thakur on 05-10-2017.
 */

public class PlayerFragment extends Fragment implements View.OnClickListener , CircularSeekBar.OnCircularSeekBarChangeListener {

    MusicService mService;
    TextView tv,songTitle,artist_name,totalTime;
    ArrayList<Song> songList;
    Context context;
    ViewGroup root;
    BlurView bottomBlur;
    ImageView image,imageView,next,prev;
    ImageView repeat,shuffle;
    CircularSeekBar seekBar;

    //discrete recyclerview
    MultiSnapRecyclerView discrete;

    private final String REC =  "com.example.thakur.randomplayer.Services.playAllSongs";
    public static final String trackChange = "com.example.thakur.randomplayer.Services.trackchangelistener";

    public static final String PLAY_PAUSE = "com.example.thakur.randomplayer.Services.playpause";
    public static final String NEXT_TRACK = "com.example.thakur.randomplayer.Services.nexttrack";
    public static final String PREV_TRACK = "com.example.thakur.randomplayer.Services.prevtrack";

    int time=0;
    Handler mHandler;
    private String totalDurationString= " ";

    int currentDur = 0;
    int totalDur = 0;
    Intent intent;
    private int tick = 0;
    FloatingActionButton playpause;
    private int sec=0;

    int height,width;
    int statusBarColor;
    UserPreferenceHandler pref;

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){

                case MusicService.RECIEVE_SONG:
                    //updatePlayer(intent);
                    break;

                case trackChange:
                    if(mService.getSongPos() ==songList.size()-1){
                        stopRepeating();
                    }
                    else {
                        //initUI(mService.getSongPos());
                        updateUI(mService.getSongPos());
                    }
                    break;

                case PLAY_PAUSE:
                    //notificationPlayPause();
                    if(!MusicService.isPlaying)
                    {
                        playpause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                        stopRepeating();
                    }
                    else{
                        playpause.setImageResource(R.drawable.ic_pause_white_36dp);
                        updateUI(mService.getSongPos());
                    }
                    break;

                case NEXT_TRACK:
                    notificationNext();
                    break;

                case PREV_TRACK:
                    notificationPrevious();
                    break;

                case MusicService.PLAYING_STATUS_CHANGED:
                    updateUI(mService.getSongPos());
                    //stopRepeating();
                    break;

            }
        }
    };






    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));

        }
        View view = inflater.inflate(R.layout.player_fragment,container,false);


        context = view.getContext();




        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(view.getContext(), "View created and sngPos="+songPos, Toast.LENGTH_SHORT).show();
        mService = MyApp.getMyService();


        IntentFilter filter = new IntentFilter();
        filter.addAction(trackChange);
        filter.addAction(PLAY_PAUSE);
        filter.addAction(NEXT_TRACK);
        filter.addAction(PREV_TRACK);
        filter.addAction(MusicService.PLAYING_STATUS_CHANGED);
        context.registerReceiver(mReciever,filter);
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



        root = view.findViewById(R.id.root);
        bottomBlur = view.findViewById(R.id.blurView);
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
        LinearLayoutManager manager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        discrete.setLayoutManager(manager);
       // InfiniteScrollAdapter infiniteAdapter = InfiniteScrollAdapter.wrap(adapter);
        discrete.setAdapter(adapter);











        updateUI(mService.getSongPos());


    }



    Runnable runnable = new Runnable() {

        @Override
        public void run() {


            {
                if(mService.isReallyPlaying()) {



                    seekBar.setProgress(seekBar.getProgress() + 1000);
                    tv.setText(songList.get(mService.getSongPos()).getFormatedTime((long)seekBar.getProgress()));
                    //sec++;
                    mHandler.postDelayed(this,1000);
                    //seekBar.postDelayed(this,1000);
                }
            }
        }
    };


    private void updateUI(int pos)
    {

        totalTime.setText(""+songList.get(pos).getDuration());
        songTitle.setText(""+songList.get(pos).getName());
        artist_name.setText(""+songList.get(pos).getArtist());
        final float radius = 10f;


        //set background, if your root layout doesn't have one
        final Drawable windowBackground = getActivity().getWindow().getDecorView().getBackground();

        final BlurView.ControllerSettings topViewSettings = bottomBlur.setupWith(root)
                .windowBackground(windowBackground)
                .blurAlgorithm(new SupportRenderScriptBlur(getActivity()))
                .blurRadius(radius);

        String path = ListSongs.getAlbumArt(context, songList.get(pos).getAlbumId());

        if(Utils.isPathValid(path)) {

            Glide.with(this).load(new File(path))
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(image);
           /*Picasso.with(context)
                    .load(new File(path))
                    .into(image);*/


            Glide.with(this).load(new File(path))
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888).centerCrop())
                    .into(imageView);
        }

         else {


            //Toast.makeText(context, "Null album art", Toast.LENGTH_SHORT).show();


            Glide.with(this).load(R.drawable.defualt_art2)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(image);
           /*Picasso.with(context)
                    .load(new File(path))
                    .into(image);*/


            Glide.with(this).load(R.drawable.defualt_art2)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(imageView);
            //imageView.setBackgroundColor(getResources().getColor(R.color.color400));



        }

        try{
            discrete.scrollToPosition(mService.getSongPos());
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        seekBar.setMax(songList.get(pos).getDurationLong());
        seekBar.setProgress(mService.getCurrentPosition());

        //tv.setText("" + songList.get(pos).getDuration());

        if(mService.isReallyPlaying()){
            playpause.setImageResource(R.drawable.ic_pause_white_36dp);
            stopRepeating();
            updateSeekbar();
        }else{
            playpause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            stopRepeating();
        }

        if(pref.isShuffleEnabled()){
            shuffle.setImageResource(R.drawable.ic_shuffle_selected_36dp);
        }
        else{
            shuffle.setImageResource(R.drawable.ic_shuffle_white_36dp);
        }

        if(pref.isRepeatOneEnabled()){
            repeat.setImageResource(R.drawable.ic_repeat_one_selecred_36dp);
        }
        else if(pref.isRepeatAllEnabled()){
            repeat.setImageResource(R.drawable.ic_repeat_all_36dp);
        }
        else{
            repeat.setImageResource(R.drawable.ic_repeat_black_36dp);
        }


        //updatePlayer(intent);




        //mHandler.removeCallbacksAndMessages(null);
        //stopRepeating();
        //updateSeekbar();



        //tv.setText(getDuration(PlayerActivity.songPos));
        //Toast.makeText(context, " "+getDuration(songPos), Toast.LENGTH_SHORT).show();
    }


    public void updateSeekbar(){

        //seekBar.postDelayed(runnable, 1000);

        mHandler.postDelayed(runnable,1000);

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
                    mService.playPause();
                    updateUI(mService.getSongPos());
                    Log.v("TAG","Playing");

                } else {
                    mService.playPause();
                    updateUI(mService.getSongPos());
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
        if(mService.isReallyPlaying()){
            updateUI(mService.getSongPos());
        }
        else {
            //updateUIPosition(mService.getSongPos());
            updateUI(mService.getSongPos());
        }
        super.onResume();




    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updateUI(mService.getSongPos());
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



    private void stopRepeating(){
        mHandler.removeCallbacks(runnable);
    }



    private void handleRepeat(){
        if(!pref.isRepeatEnabled())
        {
            pref.setRepeatAllEnable(true);
            repeat.setImageResource(R.drawable.ic_repeat_all_36dp);
            pref.setShuffleEnabled(false);
            //updateUI(mService.getSongPos());
        }
        else if(pref.isRepeatAllEnabled()){
            pref.setRepeatOneEnable(true);
            pref.setRepeatAllEnable(false);
            repeat.setImageResource(R.drawable.ic_repeat_one_selecred_36dp);
            pref.setShuffleEnabled(false);
            //updateUI(mService.getSongPos());
        }
        else{
            pref.setRepeatAllEnable(false);
            pref.setRepeatOneEnable(false);
            repeat.setImageResource(R.drawable.ic_repeat_black_36dp);
        }
    }

    private void handleShuffle(){
        if(!pref.isShuffleEnabled()){
            pref.setShuffleEnabled(true);
            shuffle.setImageResource(R.drawable.ic_shuffle_selected_36dp);
            pref.setRepeatAllEnable(false);
            pref.setRepeatOneEnable(false);
            //updateUI(mService.getSongPos());
        }
        else{
            pref.setShuffleEnabled(false);
            shuffle.setImageResource(R.drawable.ic_shuffle_white_36dp);
            //updateUI(mService.getSongPos());
        }
    }
    public void handleNext() {
        if(mService.getSongPos() == songList.size()-1){
            if(pref.isRepeatAllEnabled()) {
                mService.handleNextSong();
                updateUI(mService.getSongPos());
            }

        }
        else {


            mService.handleNextSong();
            updateUI(mService.getSongPos());
        }

    }

    private void handlePrev()
    {
        mService.handlePrevious();
        //initUI(mService.getSongPos());
        updateUI(mService.getSongPos());
    }






    private void notificationNext(){
        if(mService.getSongPos() == songList.size()-1){
            if(pref.isRepeatAllEnabled()) {
                //mService.handleNextSong();
                //initUI(mService.getSongPos());
                updateUI(mService.getSongPos());
            }

        }
        else {


            //mService.handleNextSong();
            //initUI(mService.getSongPos());
            updateUI(mService.getSongPos());
        }
    }

    private void notificationPrevious(){
        //initUI(mService.getSongPos());
        updateUI(mService.getSongPos());
    }

    public void notificationPlayPause(){
        if (MusicService.isPlaying) {
            //mService.playPause();
            playpause.setImageResource(R.drawable.ic_play_arrow_white_36dp);
            stopRepeating();

        } else {
            //mService.playPause();
            playpause.setImageResource(R.drawable.ic_pause_white_36dp);
            updateSeekbar();
        }
    }




}
