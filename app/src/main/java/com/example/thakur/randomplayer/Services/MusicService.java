package com.example.thakur.randomplayer.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.thakur.randomplayer.Fragments.PlayerFragment;
import com.example.thakur.randomplayer.Handler.NotificationHandler;
import com.example.thakur.randomplayer.Handler.PlayerHandler;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener , ShakeDetector.Listener {

    private final String TAG = "com.example.thakur.randomplayer.TAG";

    public static final String PLAY_ALL_SONGS = "com.example.thakur.randomplayer.Services.playAllSongs";
    public static final String GET_SONG = "com.example.thakur.randomplayer.Services.getsongs";
    public static final String RECIEVE_SONG = "com.example.thakur.randomplayer.Services.recievesongs";
    public static final String PLAY_SINGLE = "com.example.thakur.randomplayer.Services.playsingle";
    public static final String PLAY_PAUSE = "com.example.thakur.randomplayer.Services.playpause";
    public static final String SEEK_SONG = "com.example.thakur.randomplayer.Services.seek";
    public static final String PLAY_NEXT = "com.example.thakur.randomplayer.Services.playnext";
    public static final String PLAY_PREV = "com.example.thakur.randomplayer.Services.playprev";
    public static final String PLAY_SONG_BY_ID = "com.example.thakur.randomplayer.Services.playsongbyid";
    public static final String ACTION_NOTI_CLICK = "com.example.thakur.randomplayer.Services.ACTION_NOTI_CLICK";
    public static final String ACTION_NOTI_REMOVE = "com.example.thakur.randomplayer.Services.ACTION_NOTI_REMOVE";
    public static final String ACTION_NEXT_SONG = "com.example.thakur.randomplayer.Services.ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "com.example.thakur.randomplayer.Services.ACTION_PREV_SONG";
    public static final String ACTION_PAUSE_SONG = "com.example.thakur.randomplayer.Services.ACTION_PAUSE_SONG";
    public static final String PLAYING_STATUS_CHANGED = "com.example.thakur.randomplayer.Services.PLAYING_STATUS_CHANGED";
    public static final String DISCRETE_VIEW_CLICK = "com.example.thakur.randomplayer.Services.DISCRETE_VIEW_CLICK";


    public static int time = 0;

    public static boolean isPlaying=true;
    public static boolean isServiceRunning;
    public static boolean musicPausedBecauseOfCall;
    private AudioManager audioManager;

    //Shake Detector
    private static SensorManager sensorManager;
    private static ShakeDetector shakeDetector;

    //SharedPreference



    private int songPos = 0;
    ArrayList<Song> songList = new ArrayList<>();
    ArrayList<Song> albumSongList = new ArrayList<>();
    PlayerHandler mPlayer;
    private long album_id;
    private long song_id;
    private int searchPos;

    private String folderSongTitle;

    IBinder mBinder = new LocalService();
    UserPreferenceHandler pref;
    NotificationHandler notificationHandler;

    PhoneStateListener phoneStateListener;


    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {



            switch (intent.getAction())
            {
                case PLAY_SINGLE:
                    //Toast.makeText(MusicService.this, "Play Clickn"+songPos, Toast.LENGTH_SHORT).show();
                    mPlayer.playSong(songPos);
                    isPlaying = true;
                    break;

                case ACTION_NOTI_CLICK:
                    final Intent i = new Intent();
                    if (MainActivity.running) {

                    } else {
                        i.setClass(context, MainActivity.class);
                        i.putExtra("openPanel", true);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                    break;

                case ACTION_NOTI_REMOVE:
                    notificationHandler.setNotificationActive(false);
                    break;

                case ACTION_PAUSE_SONG:
                    handleNotificationPlayPause();

                    break;

                case ACTION_NEXT_SONG:
                    handleNotificationNext();
                    break;

                case ACTION_PREV_SONG:
                    handleNotificationPrev();
                    break;


                case PLAYING_STATUS_CHANGED:
                    updateNotificationPlayer();
                    break;

                case DISCRETE_VIEW_CLICK:
                    try{
                        songPos = intent.getIntExtra("discreteView.pos",0);
                        mPlayer.playSong(songPos);
                        sendBroadcast(new Intent(PlayerFragment.trackChange));
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),""+e.getMessage(),Toast.LENGTH_LONG);
                    }


            }

        }
    };

    BroadcastReceiver playReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PLAY_SINGLE:
                    songPos = intent.getIntExtra("player.position", 0);
                    mPlayer.playSong(songPos);
                    //sendBroadcast(new Intent(PlayerFragment.trackChange));
                    break;

                case PLAY_SONG_BY_ID:
                    playSongById(intent.getLongExtra("songId",0));
                    break;

            }
        }
    };

    @Override
    public void onCreate() {
        //audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        pref = new UserPreferenceHandler(getApplicationContext());

        songPos = (int) pref.getLastPlayed();
        //mPlayer.getmPlayer().seekTo((int) pref.getLastPlayedDur());

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    //Log.v(Constants.TAG,"Ringing");
                    if(isReallyPlaying()) {
                        mPlayer.pause();
                        musicPausedBecauseOfCall = true;
                    }
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    //Log.v(Constants.TAG,"Idle");
                    if(musicPausedBecauseOfCall){
                        mPlayer.start();
                        musicPausedBecauseOfCall=false;
                    }
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                   // Log.v(Constants.TAG,"Dialling");
                    if(isReallyPlaying()) {
                        mPlayer.pause();
                        musicPausedBecauseOfCall=true;
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }


        //Shake detector listener
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        setShakeListener(true);


        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        //Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        isServiceRunning = true;

        //songPos = intent.getIntExtra("onStartCommand.position",0);

        //HandlerThread thread = new HandlerThread(TAG,HandlerThread.NORM_PRIORITY);
        //thread.start();
        //Log.i("TAG",""+thread.getThreadId());
        //thread.run();
        notificationHandler = new NotificationHandler(getApplicationContext(),MusicService.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY_ALL_SONGS);
        intentFilter.addAction(PLAY_SONG_BY_ID);
        intentFilter.addAction(ACTION_NOTI_CLICK);
        intentFilter.addAction(ACTION_NOTI_REMOVE);
        intentFilter.addAction(ACTION_NEXT_SONG);
        intentFilter.addAction(ACTION_PAUSE_SONG);
        intentFilter.addAction(ACTION_PREV_SONG);
        intentFilter.addAction(PLAYING_STATUS_CHANGED);
        intentFilter.addAction(DISCRETE_VIEW_CLICK);

        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction(PLAY_SINGLE);
        playFilter.addAction(PLAY_SONG_BY_ID);
        registerReceiver(playReceiver,playFilter);

        songList = ListSongs.getSongList(getApplicationContext());
        mPlayer = new PlayerHandler(getApplicationContext());
        mPlayer.init(songList);
        mPlayer.getmPlayer().setOnCompletionListener(this);

        registerReceiver(mReciever,intentFilter);
        //mPlayer.playSong(songPos);
        //stopSelf(startId);

        return START_STICKY;
    }



    public MusicService() {
    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        handleNextSong();
        Intent i = new Intent();
        i.setAction(PlayerFragment.trackChange);
        sendBroadcast(i);
        updateNotificationPlayer();

    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public long getTotalDuration()
    {
        return mPlayer.getmPlayer().getDuration();
    }


    public void seeTo(int pos){
        mPlayer.getmPlayer().seekTo(pos);
    }

    @Override
    public void onDestroy() {

        isServiceRunning = false;
        unregisterReceiver(mReciever);
        unregisterReceiver(playReceiver);
        setShakeListener(false);

        pref.setLastPlayed(songPos);
        pref.setLastplayedDur(mPlayer.getmPlayer().getCurrentPosition());

        super.onDestroy();
    }

    public String getTitle(){
        return songList.get(songPos).getName();
    }

    public int getCurrentPosition(){
        return mPlayer.getmPlayer().getCurrentPosition();
    }

    public int getSongPos(){
        return songPos;
    }

    @Override
    public void hearShake() {
        handleNotificationNext();

    }

    // @Override
   /* public void onAudioFocusChange(int focusChange) {
        try{
            mPlayer.getmPlayer().pause();
            updateNotificationPlayer();
        }catch (Exception e){
            Toast.makeText(this, "Message "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }*/


    public class LocalService extends Binder{
        public MusicService getMusicService(){
            return MusicService.this;
        }
    }

    public void handleNextSong() {
        if (songPos == songList.size()-1) {
            if(pref.isRepeatAllEnabled()){
                songPos = 0;
                mPlayer.playSong(songPos);
            }
            else{
                Toast.makeText(this, "End of list", Toast.LENGTH_LONG).show();


            }
        } else {
            if (pref.isRepeatOneEnabled()) {
                mPlayer.playSong(songPos);
            } else if (pref.isShuffleEnabled()) {
                int n;
                Random r = new Random();
                n = r.nextInt(songList.size()-2);
                songPos = n;
                mPlayer.playSong(songPos);
            } else {
                songPos++;
                mPlayer.playSong(songPos);
            }
        }
        updateNotificationPlayer();


    }

    public void handlePrevious() {
        if (songPos == 0) {
            songPos = songList.size()-1;
            mPlayer.playSong(songPos);
        } else {
            if (mPlayer.getmPlayer().getCurrentPosition() > 4000) {
                mPlayer.playSong(songPos);
            } else {
                if (pref.isRepeatOneEnabled()) {
                    mPlayer.playSong(songPos);
                } else {
                    songPos--;
                    mPlayer.playSong(songPos);
                }
            }
        }
        updateNotificationPlayer();

    }

    public void playPause(){
        if(mPlayer.getmPlayer().isPlaying()){
            mPlayer.pause();
            isPlaying = false;
            updateNotificationPlayer();
        }
        else {
            mPlayer.start();
            isPlaying = true;
            updateNotificationPlayer();


        }
    }

    public void handleSeek(float seekPosition){
        try{
            mPlayer.getmPlayer().seekTo((int)seekPosition);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop(){
        mPlayer.getmPlayer().stop();
    }

    private void playSongById(long songId){
        Song s = ListSongs.getSong(getApplicationContext(),songId);

    }


    public void setSongList(ArrayList<Song> list){
        //songList = new ArrayList<>();
        this.songList.clear();
        this.songList.addAll(list);
        mPlayer.clearList();
        mPlayer.setPlayerList(list);
    }



    public void setSongPos(int pos){
        this.songPos = pos;
    }


    public void playAlbum(){
        try{
            //mPlayer.init(songList);
            setSongList(ListSongs.getAlbumSongList(getApplicationContext(),album_id));
            mPlayer.playSong(songPos);
            pref.setPlayingAlbum(true);
            pref.setPlayingSearchList(false);
            pref.setPlayingSongList(false);
            isPlaying = true;
            updateNotificationPlayer();

        }catch (Exception e){
            Toast.makeText(this, "Can't play due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void playAll(){
        try{
            //mPlayer.init(songList);
            //mPlayer.clearList();
            //mPlayer.setPlayerList(songList);
            mPlayer.playSong(songPos);
            pref.setPlayingSongList(true);
            pref.setPlayingAlbum(false);
            pref.setPlayingSearchList(false);
            isPlaying = true;
            updateNotificationPlayer();


        }catch (Exception e){
            Toast.makeText(this, "Can't play due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            updateNotificationPlayer();
        }
    }



    public ArrayList<Song> getSongList(){
        return songList;
    }


    public void updateNotificationPlayer() {
        if (!mPlayer.getmPlayer().isPlaying()) {
            stopForeground(false);
            notificationHandler.setNotificationPlayer(true);
        }
        else{
            notificationHandler.setNotificationPlayer(false);
        }

        notificationHandler.changeNotificationDetails(songList.get(songPos).getName(),songList.get(songPos).getArtist(),
                songList.get(songPos).getAlbumId(),
                mPlayer.getmPlayer().isPlaying());
    }

    private void handleNotificationPlayPause(){
        Intent playPause = new Intent();
        playPause.setAction(PlayerFragment.PLAY_PAUSE);
        if(mPlayer.getmPlayer().isPlaying()){

            mPlayer.pause();
            isPlaying = false;




        }
        else{
            mPlayer.start();
            isPlaying = true;


        }
        sendBroadcast(playPause);
        updateNotificationPlayer();

    }

    private void handleNotificationNext(){
        if (songPos == songList.size()-1) {
            if(pref.isRepeatAllEnabled()){
                songPos = 0;
                mPlayer.playSong(songPos);
            }
            else{
                Toast.makeText(this, "End of list", Toast.LENGTH_LONG).show();


            }
        } else {
            if (pref.isRepeatOneEnabled()) {
                mPlayer.playSong(songPos);
            } else if (pref.isShuffleEnabled()) {
                int n;
                Random r = new Random();
                n = r.nextInt(songList.size()-2);
                songPos = n;
                mPlayer.playSong(songPos);
            } else {
                songPos++;
                mPlayer.playSong(songPos);
            }
        }
        sendBroadcast(new Intent(PlayerFragment.trackChange));
        updateNotificationPlayer();


    }

    private void handleNotificationPrev(){
        if (songPos == 0) {
            songPos = songList.size()-1;
            mPlayer.playSong(songPos);
        } else {
            if (mPlayer.getmPlayer().getCurrentPosition() > 4000) {
                mPlayer.playSong(songPos);
            } else {
                if (pref.isRepeatOneEnabled()) {
                    mPlayer.playSong(songPos);
                } else {
                    songPos--;
                    mPlayer.playSong(songPos);
                }
            }
        }
        updateNotificationPlayer();
        sendBroadcast(new Intent(PlayerFragment.trackChange));

    }


    

    public void setAblbumId(long id){
        album_id = id;
    }
    public void setSongId(long id){
        song_id = id;
    }

    public boolean isReallyPlaying(){
        return mPlayer.getmPlayer().isPlaying();
    }

    public String getSongTitle(){
        return songList.get(songPos).getName();
    }

    public String getArtistName(){
        return songList.get(songPos).getArtist();
    }


    public void setShakeListener(boolean status){
        if(status) {
            shakeDetector.start(sensorManager);
        }
        else {
            try {
                shakeDetector.stop();
            }catch (Exception ignored){

            }
        }

    }

    public void playSongById(){
        try{
            mPlayer.playSongByID(song_id);
            updateNotificationPlayer();

        }catch (Exception e){

        }
    }


    public void setFolderSongTitle(String str){
        folderSongTitle = str;
    }

    public void playFolder(){
        long id = ListSongs.getIdFromTitle(getApplicationContext(),folderSongTitle);
        song_id = id;

        playSongById();

        for(int i=0;i<songList.size();i++){
            if(id==songList.get(i).getSongId()){
                songPos = i;
                break;
            }
        }
        //playAll();
        //playSongById();
        //updateNotificationPlayer();
    }




}
