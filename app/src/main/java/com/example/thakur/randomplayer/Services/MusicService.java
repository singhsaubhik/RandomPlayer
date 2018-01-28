package com.example.thakur.randomplayer.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.thakur.randomplayer.DatabaseHelper.HistoryStore;
import com.example.thakur.randomplayer.Fragments.PlayerFragment;
import com.example.thakur.randomplayer.Handler.NotificationHandler;
import com.example.thakur.randomplayer.Handler.PlayerHandler;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Loaders.MusicPlaybackQueueStore;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener , ShakeDetector.Listener,Playback.PlaybackCallbacks {

    public static final String TAG = MusicService.class.getSimpleName();
    public static final String PHONOGRAPH_PACKAGE_NAME = "com.kabouzeid.gramophone";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String SAVED_POSITION = "POSITION";
    public static final String SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK";
    public static final String SAVED_SHUFFLE_MODE = "SHUFFLE_MODE";
    public static final String SAVED_REPEAT_MODE = "REPEAT_MODE";

    public static final int RELEASE_WAKELOCK = 0;
    public static final int RESTORE_QUEUES = 9;
    public static final int SAVE_QUEUES = 0;

    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;

    private static final int FOCUS_CHANGE = 6;
    public static final int PREPARE_NEXT = 4;
    public static final int PLAY_SONG = 3;
    public static final int SET_POSITION = 5;
    public static final int TRACK_ENDED = 1;
    public static final int TRACK_WENT_TO_NEXT = 2;
    private static final int DUCK = 7;
    private static final int UNDUCK = 8;

    public static final String META_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".metachanged";
    public static final String QUEUE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".queuechanged";
    public static final String PLAY_STATE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".playstatechanged";


    public static final String REPEAT_MODE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".mediastorechanged";



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

    private boolean notHandledMetaChangedForCurrentTrack;

    //Shake Detector
    private static SensorManager sensorManager;
    private static ShakeDetector shakeDetector;


    private ArrayList<Song> playingQueue = new ArrayList<>();
    private ArrayList<Song> originalPlayingQueue = new ArrayList<>();

    private int position = -1;
    private int nextPosition = -1;
    private int shuffleMode;
    private int repeatMode;

    private boolean queuesRestored;
    private boolean pausedByTransientLossOfFocus;

    private PowerManager.WakeLock wakeLock;

    private QueueSaveHandler queueSaveHandler;
    private HandlerThread queueSaveHandlerThread;
    private HandlerThread musicPlayerHandlerThread;
    private PlaybackHandler playerHandler;
    private Playback playback;

    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            playerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };

    private boolean becomingNoisyReceiverRegistered;

    private IntentFilter becomingNoisyReceiverIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
        }
    };

    private ContentObserver mediaStoreObserver;


    private int songPos = 0;
    private ArrayList<Song> songList = new ArrayList<>();
    private ArrayList<Song> albumSongList = new ArrayList<>();
    private PlayerHandler mPlayer;
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
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        queueSaveHandlerThread = new HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND);
        queueSaveHandlerThread.start();
        queueSaveHandler = new QueueSaveHandler(this, queueSaveHandlerThread.getLooper());

        pref = new UserPreferenceHandler(getApplicationContext());



        //songPos = (int) pref.getLastPlayed();
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

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
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

        return START_NOT_STICKY;
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
        wakeLock.release();
        //pref.setLastPlayed(songPos);
        //ref.setLastplayedDur(mPlayer.getmPlayer().getCurrentPosition());
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

    @Override
    public void onTrackWentToNext() {

    }

    @Override
    public void onTrackEnded() {

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationHandler.changeNotificationDetails(songList.get(songPos),mPlayer.getmPlayer().isPlaying());
            }
        },300);

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

    public Song getCurrentSong(){
        return songList.get(songPos);
    }

    private static final class QueueSaveHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;

        public QueueSaveHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MusicService service = mService.get();
            switch (msg.what) {
                case SAVE_QUEUES:
                    service.saveQueuesImpl();
                    break;
            }
        }
    }

    private void saveQueuesImpl() {
        MusicPlaybackQueueStore.getInstance(this).saveQueues(playingQueue, originalPlayingQueue);
    }

    private void savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION, getPosition()).apply();
    }

    private void savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION_IN_TRACK, getSongProgressMillis()).apply();
    }

    public void saveState() {
        saveQueues();
        savePosition();
        savePositionInTrack();
    }

    private void saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES);
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES);
    }

    private void restoreState() {
        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0);
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0);
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED);

        playerHandler.removeMessages(RESTORE_QUEUES);
        playerHandler.sendEmptyMessage(RESTORE_QUEUES);
    }

    private synchronized void restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            ArrayList<Song> restoredQueue = MusicPlaybackQueueStore.getInstance(this).getSavedPlayingQueue();
            ArrayList<Song> restoredOriginalQueue = MusicPlaybackQueueStore.getInstance(this).getSavedOriginalPlayingQueue();
            int restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION, -1);
            int restoredPositionInTrack = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION_IN_TRACK, -1);

            if (restoredQueue.size() > 0 && restoredQueue.size() == restoredOriginalQueue.size() && restoredPosition != -1) {
                this.originalPlayingQueue = restoredOriginalQueue;
                this.playingQueue = restoredQueue;

                position = restoredPosition;
                openCurrent();
                prepareNext();

                if (restoredPositionInTrack > 0) seek(restoredPositionInTrack);

                notHandledMetaChangedForCurrentTrack = true;
                sendChangeInternal(META_CHANGED);
                sendChangeInternal(QUEUE_CHANGED);
            }
        }
        queuesRestored = true;
    }

    private void quit() {
        //pause();
        //playingNotification.stop();

        //closeAudioEffectSession();
        //getAudioManager().abandonAudioFocus(audioFocusListener);
        stopSelf();
    }

    private void releaseResources() {
        playerHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            musicPlayerHandlerThread.quitSafely();
        } else {
            musicPlayerHandlerThread.quit();
        }
        queueSaveHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            queueSaveHandlerThread.quitSafely();
        } else {
            queueSaveHandlerThread.quit();
        }
        playback.release();
        playback = null;
        //mediaSession.release();
    }

    public boolean isPlaying() {
        return playback != null && playback.isPlaying();
    }

    public int getPosition() {
        return position;
    }

    public void playNextSong(boolean force) {
        playSongAt(getNextPosition(force));
    }

    private boolean openTrackAndPrepareNextAt(int position) {
        synchronized (this) {
            this.position = position;
            boolean prepared = openCurrent();
            if (prepared) prepareNextImpl();
            notifyChange(META_CHANGED);
            notHandledMetaChangedForCurrentTrack = false;
            return prepared;
        }
    }

    private void prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT);
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget();
    }

    private boolean prepareNextImpl() {
        synchronized (this) {
            try {
                int nextPosition = getNextPosition(false);
                playback.setNextDataSource(getTrackUri(getSongAt(nextPosition)));
                this.nextPosition = nextPosition;
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void closeAudioEffectSession() {
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        //audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
    }

    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }


    public Song getSongAt(int position) {
        if (position >= 0 && position < getPlayingQueue().size()) {
            return getPlayingQueue().get(position);
        } else {
            return Song.EMPTY_SONG;
        }
    }

    public int getNextPosition(boolean force) {
        int position = getPosition() + 1;
        switch (getRepeatMode()) {
            case REPEAT_MODE_ALL:
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (isLastTrack()) {
                        position = 0;
                    }
                } else {
                    position -= 1;
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (isLastTrack()) {
                    position -= 1;
                }
                break;
        }
        return position;
    }

    private boolean isLastTrack() {
        return getPosition() == getPlayingQueue().size() - 1;
    }

    public ArrayList<Song> getPlayingQueue() {
        return playingQueue;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(final int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_ALL:
            case REPEAT_MODE_THIS:
                this.repeatMode = repeatMode;
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putInt(SAVED_REPEAT_MODE, repeatMode)
                        .apply();
                prepareNext();
                handleAndSendChangeInternal(REPEAT_MODE_CHANGED);
                break;
        }
    }

    public void openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            // it is important to copy the playing queue here first as we might add/remove songs later
            originalPlayingQueue = new ArrayList<>(playingQueue);
            this.playingQueue = new ArrayList<>(originalPlayingQueue);

            int position = startPosition;
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(this.playingQueue, startPosition);
                position = 0;
            }
            if (startPlaying) {
                playSongAt(position);
            } else {
                setPosition(position);
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSong(int position, Song song) {
        playingQueue.add(position, song);
        originalPlayingQueue.add(position, song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        originalPlayingQueue.add(song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(int position, List<Song> songs) {
        playingQueue.addAll(position, songs);
        originalPlayingQueue.addAll(position, songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        originalPlayingQueue.addAll(songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(int position) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.remove(position);
            originalPlayingQueue.remove(position);
        } else {
            originalPlayingQueue.remove(playingQueue.remove(position));
        }

        rePosition(position);

        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(@NonNull Song song) {
        for (int i = 0; i < playingQueue.size(); i++) {
            if (playingQueue.get(i).getSongId() == song.getSongId()) {
                playingQueue.remove(i);
                rePosition(i);
            }
        }
        for (int i = 0; i < originalPlayingQueue.size(); i++) {
            if (originalPlayingQueue.get(i).getSongId() == song.getSongId()) {
                originalPlayingQueue.remove(i);
            }
        }
        notifyChange(QUEUE_CHANGED);
    }

    private void rePosition(int deletedPosition) {
        int currentPosition = getPosition();
        if (deletedPosition < currentPosition) {
            position = currentPosition - 1;
        } else if (deletedPosition == currentPosition) {
            if (playingQueue.size() > deletedPosition) {
                setPosition(position);
            } else {
                setPosition(position - 1);
            }
        }
    }

    public void moveSong(int from, int to) {
        if (from == to) return;
        final int currentPosition = getPosition();
        Song songToMove = playingQueue.remove(from);
        playingQueue.add(to, songToMove);
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            Song tmpSong = originalPlayingQueue.remove(from);
            originalPlayingQueue.add(to, tmpSong);
        }
        if (from > currentPosition && to <= currentPosition) {
            position = currentPosition + 1;
        } else if (from < currentPosition && to >= currentPosition) {
            position = currentPosition - 1;
        } else if (from == currentPosition) {
            position = to;
        }
        notifyChange(QUEUE_CHANGED);
    }

    public void clearQueue() {
        playingQueue.clear();
        originalPlayingQueue.clear();

        setPosition(-1);
        notifyChange(QUEUE_CHANGED);
    }

    public void playSongAt(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG);
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
    }

    public void setPosition(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(SET_POSITION);
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget();
    }

    private void playSongAtImpl(int position) {
        if (openTrackAndPrepareNextAt(position)) {
            play();
        } else {
            Toast.makeText(this, getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        pausedByTransientLossOfFocus = false;
        if (playback.isPlaying()) {
            playback.pause();
            notifyChange(PLAY_STATE_CHANGED);
        }
    }

    public void play() {
        synchronized (this) {
            if (requestFocus()) {
                if (!playback.isPlaying()) {
                    if (!playback.isInitialized()) {
                        playSongAt(getPosition());
                    } else {
                        playback.start();
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(becomingNoisyReceiver, becomingNoisyReceiverIntentFilter);
                            becomingNoisyReceiverRegistered = true;
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED);
                            notHandledMetaChangedForCurrentTrack = false;
                        }
                        notifyChange(PLAY_STATE_CHANGED);

                        // fixes a bug where the volume would stay ducked because the AudioManager.AUDIOFOCUS_GAIN event is not sent
                        //playerHandler.removeMessages(DUCK);
                        //playerHandler.sendEmptyMessage(UNDUCK);
                    }
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void playSongs(ArrayList<Song> songs, int shuffleMode) {
        if (songs != null && !songs.isEmpty()) {
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                int startPosition = 0;
                if (!songs.isEmpty()) {
                    startPosition = new Random().nextInt(songs.size());
                }
                openQueue(songs, startPosition, false);
                setShuffleMode(shuffleMode);
            } else {
                openQueue(songs, 0, false);
            }
            play();
        } else {
            Toast.makeText(getApplicationContext(), R.string.playlist_is_empty, Toast.LENGTH_LONG).show();
        }
    }

    public void playPreviousSong(boolean force) {
        playSongAt(getPreviousPosition(force));
    }

    public void back(boolean force) {
        if (getSongProgressMillis() > 2000) {
            seek(0);
        } else {
            playPreviousSong(force);
        }
    }

    public int getPreviousPosition(boolean force) {
        int newPosition = getPosition() - 1;
        switch (repeatMode) {
            case REPEAT_MODE_ALL:
                if (newPosition < 0) {
                    newPosition = getPlayingQueue().size() - 1;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (newPosition < 0) {
                        newPosition = getPlayingQueue().size() - 1;
                    }
                } else {
                    newPosition = getPosition();
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }
        return newPosition;
    }

    public int getSongProgressMillis() {
        return playback.position();
    }

    public int getSongDurationMillis() {
        return playback.duration();
    }

    public long getQueueDurationMillis(int position) {
        long duration = 0;
        for (int i = position + 1; i < playingQueue.size(); i++)
            duration += playingQueue.get(i).getDurationLong();
        return duration;
    }

    public int seek(int millis) {
        synchronized (this) {
            try {
                int newPosition = playback.seek(millis);
                //throttledSeekHandler.notifySeek();
                return newPosition;
            } catch (Exception e) {
                return -1;
            }
        }
    }

    public void cycleRepeatMode() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
                setRepeatMode(REPEAT_MODE_ALL);
                break;
            case REPEAT_MODE_ALL:
                setRepeatMode(REPEAT_MODE_THIS);
                break;
            default:
                setRepeatMode(REPEAT_MODE_NONE);
                break;
        }
    }

    public void toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE);
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE);
        }
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(final int shuffleMode) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(SAVED_SHUFFLE_MODE, shuffleMode)
                .apply();
        switch (shuffleMode) {
            case SHUFFLE_MODE_SHUFFLE:
                this.shuffleMode = shuffleMode;
                ShuffleHelper.makeShuffleList(this.getPlayingQueue(), getPosition());
                position = 0;
                break;
            case SHUFFLE_MODE_NONE:
                this.shuffleMode = shuffleMode;
                int currentSongId = (int) getCurrentSong().getSongId();
                playingQueue = new ArrayList<>(originalPlayingQueue);
                int newPosition = 0;
                for (Song song : getPlayingQueue()) {
                    if (song.getSongId() == currentSongId) {
                        newPosition = getPlayingQueue().indexOf(song);
                    }
                }
                position = newPosition;
                break;
        }
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
        notifyChange(QUEUE_CHANGED);
    }

    private void notifyChange(@NonNull final String what) {
        handleAndSendChangeInternal(what);
        sendPublicIntent(what);
    }

    private void handleAndSendChangeInternal(@NonNull final String what) {
        handleChangeInternal(what);
        sendChangeInternal(what);
    }

    // to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
    private void sendPublicIntent(@NonNull final String what) {
        final Intent intent = new Intent(what.replace(PHONOGRAPH_PACKAGE_NAME, MUSIC_PACKAGE_NAME));

        final Song song = getCurrentSong();

        intent.putExtra("id", song.getSongId());

        intent.putExtra("artist", song.getArtist());
        intent.putExtra("album", song.getAlbumName());
        intent.putExtra("track", song.getName());

        intent.putExtra("duration", song.getDurationLong());
        intent.putExtra("position", (long) getSongProgressMillis());

        intent.putExtra("playing", isPlaying());

        intent.putExtra("scrobbling_source", PHONOGRAPH_PACKAGE_NAME);

        sendStickyBroadcast(intent);
    }

    private void sendChangeInternal(final String what) {
        sendBroadcast(new Intent(what));
        //appWidgetBig.notifyChange(this, what);
        //appWidgetClassic.notifyChange(this, what);
        //appWidgetSmall.notifyChange(this, what);
        //appWidgetCard.notifyChange(this, what);
    }

    private void handleChangeInternal(@NonNull final String what) {
        switch (what) {
            case PLAY_STATE_CHANGED:
                //updateNotification();
                //updateMediaSessionPlaybackState();
                final boolean isPlaying = isPlaying();
                if (!isPlaying && getSongProgressMillis() > 0) {
                    savePositionInTrack();
                }
                //songPlayCountHelper.notifyPlayStateChanged(isPlaying);
                break;
            case META_CHANGED:
                //updateNotification();
                //updateMediaSessionMetaData();
                savePosition();
                savePositionInTrack();
                final Song currentSong = getCurrentSong();
                HistoryStore.getInstance(this).addSongId(currentSong.getSongId());
                /*if (songPlayCountHelper.shouldBumpPlayCount()) {
                    SongPlayCountStore.getInstance(this).bumpPlayCount(songPlayCountHelper.getSong().id);
                }
                songPlayCountHelper.notifySongChanged(currentSong);*/
                break;
            case QUEUE_CHANGED:
                //updateMediaSessionMetaData(); // because playing queue size might have changed
                saveState();
                if (playingQueue.size() > 0) {
                    prepareNext();
                } else {
                    //playingNotification.stop();
                }
                break;
        }
    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }



    private static final class PlaybackHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;
        private float currentDuckVolume = 1.0f;

        public PlaybackHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull final Message msg) {
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {

                case TRACK_WENT_TO_NEXT:
                    if (service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.pause();
                        service.seek(0);
                    } else {
                        service.position = service.nextPosition;
                        service.prepareNextImpl();
                        service.notifyChange(META_CHANGED);
                    }
                    break;

                case TRACK_ENDED:
                    if (service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.notifyChange(PLAY_STATE_CHANGED);
                        service.seek(0);
                    } else {
                        service.playNextSong(false);
                    }
                    sendEmptyMessage(RELEASE_WAKELOCK);
                    break;

                case RELEASE_WAKELOCK:
                    service.releaseWakeLock();
                    break;

                case PLAY_SONG:
                    service.playSongAtImpl(msg.arg1);
                    break;

                case SET_POSITION:
                    service.openTrackAndPrepareNextAt(msg.arg1);
                    service.notifyChange(PLAY_STATE_CHANGED);
                    break;

                case PREPARE_NEXT:
                    service.prepareNextImpl();
                    break;

                case RESTORE_QUEUES:
                    service.restoreQueuesAndPositionIfNecessary();
                    break;

                case FOCUS_CHANGE:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!service.isPlaying() && service.pausedByTransientLossOfFocus) {
                                service.play();
                                service.pausedByTransientLossOfFocus = false;
                            }
                            removeMessages(DUCK);
                            sendEmptyMessage(UNDUCK);
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Lost focus for an unbounded amount of time: stop playback and release media playback
                            service.pause();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Lost focus for a short time, but we have to stop
                            // playback. We don't release the media playback because playback
                            // is likely to resume
                            boolean wasPlaying = service.isPlaying();
                            service.pause();
                            service.pausedByTransientLossOfFocus = wasPlaying;
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Lost focus for a short time, but it's ok to keep playing
                            // at an attenuated level
                            removeMessages(UNDUCK);
                            sendEmptyMessage(DUCK);
                            break;
                    }
                    break;
            }
        }
    }


    private class MediaStoreObserver extends ContentObserver implements Runnable {
        // milliseconds to delay before calling refresh to aggregate events
        private static final long REFRESH_DELAY = 500;
        private Handler mHandler;

        public MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            // if a change is detected, remove any scheduled callback
            // then post a new one. This is intended to prevent closely
            // spaced events from generating multiple refresh calls
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, REFRESH_DELAY);
        }

        @Override
        public void run() {
            // actually call refresh when the delayed callback fires
            // do not send a sticky broadcast here
            handleAndSendChangeInternal(MEDIA_STORE_CHANGED);
        }
    }

    private boolean openCurrent() {
        synchronized (this) {
            try {
                return playback.setDataSource(getTrackUri(getCurrentSong()));
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static String getTrackUri(@NonNull Song song) {
        return null ; //ListSongs.getSongFileUri(song.getSongId()).toString();
    }

}
