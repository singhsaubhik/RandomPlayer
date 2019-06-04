package com.example.thakur.randomplayer.Handler;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.Provider.RecentStore;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.Constants;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by Thakur on 04-09-2017
 */

public class PlayerHandler implements AudioManager.OnAudioFocusChangeListener {
    private Context context;
    private int currSong = 0;
    private MediaPlayer mPlayer = new MediaPlayer();
    private ArrayList<Song> songList = new ArrayList<>();
    private MusicService mService;
    private boolean isBind = false;
    private AudioManager audioManager;

    //service
    private MusicService service;

    // Preference Handler
    private UserPreferenceHandler pref;


    public void init(ArrayList<Song> songs) {
        //mPlayer = new MediaPlayer();
        mPlayer.reset();
        songList = songs;

        long pref_song_id = pref.getLastPlayed();

        for (int i = 0; i < songList.size(); i++) {
            if (pref_song_id == songList.get(i).getSongId()) {

                currSong = i;
                break;
            }
        }

        long songId = songList.get(currSong).getSongId();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songId);
        //set the data source
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(context.getApplicationContext(), trackUri);
            mPlayer.prepare();
            //audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
            MusicService.isPlaying = false;
        }


    }


    public PlayerHandler(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        service = MyApp.getMyService();
        pref = new UserPreferenceHandler(context);
    }

    public void setCurrSong(int currSong) {
        this.currSong = currSong;
    }


    public void playSong(int position) {
        //play

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            MusicService.isPlaying = false;
        }
        mPlayer.reset();

        Song song;
        song = songList.get(position);
        String songTitle = song.getName();
        //get id
        long currSong = song.getSongId();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        //set the data source
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(context.getApplicationContext(), trackUri);
            mPlayer.prepare();
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        Intent in = new Intent();
        in.setAction(Constants.HEADER_VIEW_POSITION);
        context.sendBroadcast(in);

        RecentStore.getInstance(context).addSongId(songList.get(position).getSongId());
        mPlayer.start();
        MusicService.isPlaying = true;
    }

    public void playSongByID(long songId) {

        if (mPlayer.isPlaying()) {
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

        try {
            mPlayer.setDataSource(context.getApplicationContext(), trackUri);
            mPlayer.prepare();
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
            MusicService.isPlaying = false;
        }


        //player.prepareAsync();


        mPlayer.start();
        MusicService.isPlaying = true;


    }

    public void start() {
        try {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (MyApp.getMyService() != null) {

            }
            int d = (int)pref.getLastPlayedDur();

            mPlayer.seekTo(d);
            mPlayer.start();
            MusicService.isPlaying = true;
            //service.updateNotificationPlayer();
            context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
            MyApp.getMyService().setShakeListener(new UserPreferenceHandler(context).getHearShake());
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            MusicService.isPlaying = false;
        }
    }

    public void pause() {
        try {
            mPlayer.pause();
            //service.updateNotificationPlayer();
            MusicService.isPlaying = false;
            context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
            MyApp.getMyService().setShakeListener(false);
        } catch (Exception e) {
            MusicService.isPlaying = false;

        }
    }


    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public int getDurationn() {
        return mPlayer.getDuration();
    }

    public int getCurrPos() {
        return mPlayer.getCurrentPosition();
    }

    public void seek(int progress) {
        mPlayer.seekTo(progress);
    }


    public void seekSong(int value) {
        mService.seeTo(value);
    }

    public void setPlayerList(ArrayList<Song> s) {
        songList.addAll(s);
    }

    public void clearList() {
        try {
            songList.clear();

        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case -1:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    MusicService.isPlaying = false;
                    context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
                }
                break;

            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    MusicService.isPlaying = false;
                    context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));
                }
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayer.isPlaying()) {
                    mPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mPlayer.isPlaying()) {
                    mPlayer.setVolume(0.3f, 0.3f);
                }
                break;

        }
    }
}
