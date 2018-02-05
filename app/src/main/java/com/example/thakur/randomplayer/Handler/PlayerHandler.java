package com.example.thakur.randomplayer.Handler;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.Provider.RecentStore;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by Thakur on 04-09-2017
 */

public class PlayerHandler implements AudioManager.OnAudioFocusChangeListener
{
    private Context context;
    private int currSong=0;
    public MediaPlayer mPlayer= new MediaPlayer();
    ArrayList<Song> songList = new ArrayList<>();
    MusicService mService;
    private boolean isBind = false;
    AudioManager audioManager ;

    //service
    MusicService service;



    public void init(ArrayList<Song> songs)
    {
        //mPlayer = new MediaPlayer();
        mPlayer.reset();
        songList = songs;

    }


    public PlayerHandler(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        service = MyApp.getMyService();
    }

    public void setCurrSong(int currSong) {
        this.currSong = currSong;
    }

    public void playSong(int position){
        //play

        if(mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mPlayer.reset();
        //player= MediaPlayer.create(this,R.raw.a);
        //player.reset();

        //get song

        //get title
        Song song = new Song();
        song = songList.get(position);
        String songTitle=song.getName();
        //get id
        long currSong = song.getSongId();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        //set the data source
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try{
            mPlayer.setDataSource(context.getApplicationContext(),trackUri);
            mPlayer.prepare();
            audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }





        //player.prepareAsync();


        RecentStore.getInstance(context).addSongId(songList.get(position).getSongId());
        mPlayer.start();
        //new SongDatabase(context).insertSong(String.valueOf(songList.get(position).getSongId()),songList.get(position).getName());


    }

    public void playSongByID(long songId){

        if(mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mPlayer.reset();
        //player= MediaPlayer.create(this,R.raw.a);
        //player.reset();

        //get song

        //get title

        //get id
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songId);
        //set the data source
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try{
            mPlayer.setDataSource(context.getApplicationContext(),trackUri);
            mPlayer.prepare();
            audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }





        //player.prepareAsync();


        mPlayer.start();


    }

    public void start()
    {
        try{
            audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            mPlayer.start();
            //service.updateNotificationPlayer();
            context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
            MyApp.getMyService().setShakeListener(true);
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pause(){
        try{
            mPlayer.pause();
            //service.updateNotificationPlayer();
            context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
            MyApp.getMyService().setShakeListener(false);
        }catch (Exception e){

        }
    }

    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public int getDurationn()
    {
        return mPlayer.getDuration();
    }

    public int getCurrPos()
    {
        return mPlayer.getCurrentPosition();
    }

    public void seek(int progress)
    {
        mPlayer.seekTo(progress);
    }




    public void seekSong(int value){
        mService.seeTo(value);
    }

    public void setPlayerList(ArrayList<Song> s){
        songList.addAll(s);
    }

    public void clearList(){
        try{
            songList.clear();

        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case -1:
                if(mPlayer.isPlaying()){
                    mPlayer.pause();
                    context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
                }
                break;

        }
    }
}
