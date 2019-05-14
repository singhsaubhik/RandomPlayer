package com.example.thakur.randomplayer.Services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.thakur.randomplayer.Fragments.PlayerFragment;
import com.example.thakur.randomplayer.Handler.MediaButtonIntentReceiver;
import com.example.thakur.randomplayer.Handler.NotificationHandler;
import com.example.thakur.randomplayer.Handler.PlayerHandler;
import com.example.thakur.randomplayer.Loaders.AlbumLoader;
import com.example.thakur.randomplayer.Loaders.MusicPlaybackQueueStore;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.PlayerActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Constants;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, ShakeDetector.Listener {


    //testing


    public static final String TAG = MusicService.class.getSimpleName();

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
    public static final String SERVICECMD = MusicService.class.getSimpleName() + ".musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDNEXT = "next";
    public static final String NEXT_ACTION = MusicService.class.getSimpleName() + ".next";
    public static final String PREVIOUS_ACTION = MusicService.class.getSimpleName() + ".previous";
    public static final String PAUSE_ACTION = MusicService.class.getSimpleName() + ".pause";
    public static final String TOGGLEPAUSE_ACTION = MusicService.class.getSimpleName() + ".togglepause";

    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String FROM_MEDIA_BUTTON = "frommediabutton";


    ///Ab Intentts
    private PendingIntent pendingIntent;
    private PendingIntent pSwipeToDismiss;
    private PendingIntent ppreviousIntent, pplayIntent, pnextIntent, pdismissIntent, pfavintent;
    private NotificationManager mNotificationManager;

    private long lastTimePlayPauseClicked;
    private PlaybackStateCompat.Builder stateBuilder;

    public static boolean isPlaying = true;
    public static boolean isServiceRunning;
    public static boolean musicPausedBecauseOfCall;
    private static boolean mPausedByTransientLossOfFocus = false;


    //Shake Detector
    private static SensorManager sensorManager;
    private static ShakeDetector shakeDetector;


    private PowerManager.WakeLock wakeLock;
    public RemoteControlClient mRemoteControlClient;
    private ComponentName mMediaButtonReceiverComponent;
    private AudioManager mAudioManager;


    private ContentObserver mediaStoreObserver;


    private int songPos = 0;
    private ArrayList<Song> songList = new ArrayList<>();
    private PlayerHandler mPlayer;
    private long album_id;
    private long song_id;
    private int searchPos;

    private String folderSongTitle;

    IBinder mBinder = new LocalService();
    UserPreferenceHandler pref;
    NotificationHandler notificationHandler;

    PhoneStateListener phoneStateListener;
    MediaSessionCompat mMediaSession;

    //BroadCastRecievers
    BroadcastReceiver mReciever, headPhones, playReceiver;


    @Override
    public void onCreate() {
        //audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        initializeReciever();
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        final HandlerThread thread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();


        pref = new UserPreferenceHandler(getApplicationContext());
        phoneStateListener();
        //Shake detector listener
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        InitializeIntents();
        InitializeMediaSession();

        //initilizeShakeDetector();


        try {
            songList = SongLoader.getSongList(this);
        } catch (Exception e) {
            songList = SongLoader.getSongList(MusicService.this);
        }

        initLastPlayedSong();

        mPlayer = new PlayerHandler(getApplicationContext());
        mPlayer.init(songList);
        mPlayer.getmPlayer().setOnCompletionListener(this);


        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        isServiceRunning = true;

        //songPos = intent.getIntExtra("onStartCommand.position",0);

        //HandlerThread thread = new HandlerThread(TAG,HandlerThread.NORM_PRIORITY);
        //thread.start();
        //Log.i("TAG",""+thread.getThreadId());
        //thread.run();
        notificationHandler = new NotificationHandler(getApplicationContext(), MusicService.this);
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
        registerReceiver(playReceiver, playFilter);


        registerReceiver(mReciever, intentFilter);

        // Initialize the intent filter and each action


        //setUpRemoteControlClient();
        Toast.makeText(this, "" + songPos, Toast.LENGTH_SHORT).show();


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
        return mBinder;
    }

    public long getTotalDuration() {
        return mPlayer.getmPlayer().getDuration();
    }


    public void seeTo(int pos) {
        mPlayer.getmPlayer().seekTo(pos);
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;

        unregisterReceiver(mReciever);
        unregisterReceiver(playReceiver);
        setShakeListener(false);
        wakeLock.release();
        mPlayer.getmPlayer().stop();
        mPlayer.getmPlayer().reset();
        mPlayer.getmPlayer().release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSessionState();
            mMediaSession.setActive(false);
            mMediaSession.release();
        }


        //pref.setLastPlayed(songPos);
        //ref.setLastplayedDur(mPlayer.getmPlayer().getCurrentPosition());
        PreferencesUtility.getInstance(this).setLastPlayedSongId(songList.get(songPos).getSongId());
        MusicPlaybackQueueStore.getInstance(this).saveQueues(songList, songList);
        super.onDestroy();
    }

    private void initLastPlayedSong() {
        long id = PreferencesUtility.getInstance(this).getLastPlayedSongId();
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getSongId() == id) {
                songPos = i;
                break;
            }
        }

    }

    public String getTitle() {
        return songList.get(songPos).getName();
    }

    public int getCurrentPosition() {
        return mPlayer.getmPlayer().getCurrentPosition();
    }

    public int getSongPos() {
        return songPos;
    }

    @Override
    public void hearShake() {
        handleNotificationNext();

    }

    private void initializeReciever() {
        mReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
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
                        try {
                            songPos = intent.getIntExtra("discreteView.pos", 0);
                            mPlayer.playSong(songPos);
                            sendBroadcast(new Intent(PlayerFragment.trackChange));
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }


                }

            }
        };
//headphones listeners
        headPhones = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isReallyPlaying()) {
                    mPlayer.pause();
                }
            }
        };


        playReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case PLAY_SINGLE:
                        songPos = intent.getIntExtra("player.position", 0);
                        mPlayer.playSong(songPos);
                        //sendBroadcast(new Intent(PlayerFragment.trackChange));
                        break;

                    case PLAY_SONG_BY_ID:
                        playSongById(intent.getLongExtra("songId", 0));
                        break;

                }
            }
        };
    }


    public class LocalService extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    public void handleNextSong() {
        if (songPos == songList.size() - 1) {
            if (pref.isRepeatAllEnabled()) {
                songPos = 0;
                mPlayer.playSong(songPos);
            } else {
                Toast.makeText(this, "End of list", Toast.LENGTH_LONG).show();


            }
        } else {
            if (pref.isRepeatOneEnabled()) {
                mPlayer.playSong(songPos);
            } else if (pref.isShuffleEnabled()) {
                int n;
                Random r = new Random();
                n = r.nextInt(songList.size() - 2);
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
            songPos = songList.size() - 1;
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

    public void playPause() {
        if (mPlayer.getmPlayer().isPlaying()) {
            mPlayer.pause();
            isPlaying = false;
            try {
                setSessionState();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateNotificationPlayer();
        } else {
            mPlayer.start();
            isPlaying = true;
            try {
                setSessionState();
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateNotificationPlayer();


        }
    }

    public void handleSeek(float seekPosition) {
        try {
            mPlayer.getmPlayer().seekTo((int) seekPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mPlayer.getmPlayer().stop();
    }

    public void playSongById(long songId) {
        Song s = SongLoader.getSong(getApplicationContext(), (int) songId);
        ArrayList<Song> list = SongLoader.getSongList(MusicService.this);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSongId() == s.getSongId()) {
                songPos = i;
                break;
            }
        }
        setSongList(list);
        playAll();


    }


    public void setSongList(ArrayList<Song> list) {
        //songList = new ArrayList<>();
        this.songList.clear();
        this.songList.addAll(list);
        mPlayer.clearList();
        mPlayer.setPlayerList(list);
    }

    public void setSong(Song song) {
        //songList = new ArrayList<>();
        this.songList.clear();
        this.songList.add(song);
        mPlayer.clearList();
        mPlayer.setPlayerList(songList);
    }


    public void setSongPos(int pos) {
        this.songPos = pos;
    }


    public void playAlbum() {
        try {
            //mPlayer.init(songList);
            setSongList(AlbumLoader.getAlbumSongList(getApplicationContext(), album_id));
            mPlayer.playSong(songPos);
            isPlaying = true;
            updateNotificationPlayer();

        } catch (Exception e) {
            Toast.makeText(this, "Can't play due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void playAll() {
        try {
            //mPlayer.init(songList);
            //mPlayer.clearList();
            //mPlayer.setPlayerList(songList);
            mPlayer.playSong(songPos);
            isPlaying = true;
            updateNotificationPlayer();
            //PostNotification();


        } catch (Exception e) {
            Toast.makeText(this, "Can't play due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            //updateNotificationPlayer();
        }
    }


    public ArrayList<Song> getSongList() {
        return songList;
    }


    public void updateNotificationPlayer() {
        if (!mPlayer.getmPlayer().isPlaying()) {
            stopForeground(false);
            notificationHandler.setNotificationPlayer(true);
        } else {
            notificationHandler.setNotificationPlayer(false);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationHandler.changeNotificationDetails(songList.get(songPos), mPlayer.getmPlayer().isPlaying());
            }
        }, 300);

    }

    private void handleNotificationPlayPause() {
        Intent playPause = new Intent();
        playPause.setAction(PlayerFragment.PLAY_PAUSE);
        if (mPlayer.getmPlayer().isPlaying()) {

            mPlayer.pause();
            isPlaying = false;


        } else {
            mPlayer.start();
            isPlaying = true;


        }
        sendBroadcast(playPause);
        updateNotificationPlayer();

    }

    private void handleNotificationNext() {
        if (songPos == songList.size() - 1) {
            if (pref.isRepeatAllEnabled()) {
                songPos = 0;
                mPlayer.playSong(songPos);
            } else {
                Toast.makeText(this, "End of list", Toast.LENGTH_LONG).show();


            }
        } else {
            if (pref.isRepeatOneEnabled()) {
                mPlayer.playSong(songPos);
            } else if (pref.isShuffleEnabled()) {
                int n;
                Random r = new Random();
                n = r.nextInt(songList.size() - 2);
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

    private void handleNotificationPrev() {
        if (songPos == 0) {
            songPos = songList.size() - 1;
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


    public void setAblbumId(long id) {
        album_id = id;
    }

    public void setSongId(long id) {
        song_id = id;
    }

    public boolean isReallyPlaying() {
        return mPlayer.getmPlayer().isPlaying();
    }


    public void setShakeListener(boolean status) {
        if (status) {
            shakeDetector.start(sensorManager);
        } else {
            try {
                shakeDetector.stop();
            } catch (Exception ignored) {

            }
        }

    }

    public void playSongById() {
        try {
            mPlayer.playSongByID(song_id);
            updateNotificationPlayer();

        } catch (Exception e) {


        }
    }

    public void playFolder() {
        long id = RandomUtils.getIdFromTitle(getApplicationContext(), folderSongTitle);
        song_id = id;

        playSongById();

        for (int i = 0; i < songList.size(); i++) {
            if (id == songList.get(i).getSongId()) {
                songPos = i;
                break;
            }
        }
        //playAll();
        //playSongById();
        //updateNotificationPlayer();
    }

    public Song getCurrentSong() {
        return songList.get(songPos);
    }

    private void phoneStateListener() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    //Log.v(Constants.TAG,"Ringing");
                    if (isReallyPlaying()) {
                        mPlayer.pause();
                        musicPausedBecauseOfCall = true;

                    }
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    //Log.v(Constants.TAG,"Idle");
                    if (musicPausedBecauseOfCall) {
                        mPlayer.start();
                        musicPausedBecauseOfCall = false;

                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    // Log.v(Constants.TAG,"Dialling");
                    if (isReallyPlaying()) {
                        mPlayer.pause();
                        musicPausedBecauseOfCall = true;

                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void InitializeMediaSession() {
        mMediaSession = new MediaSessionCompat(getApplicationContext(), getPackageName() + "." + TAG);
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            public void onPause() {
                Log.d(TAG, "onPause called (media button pressed)");

                onPlayPauseButtonClicked();

                super.onPause();
            }

            public void onSkipToPrevious() {
                Log.d(TAG, "onskiptoPrevious called (media button pressed)");

                handlePrevious();

                super.onSkipToPrevious();
            }

            public void onSkipToNext() {
                Log.d(TAG, "onskiptonext called (media button pressed)");

                handleNextSong();

                super.onSkipToNext();
            }

            public void onPlay() {
                Log.d(TAG, "onPlay called (media button pressed)");

                onPlayPauseButtonClicked();
                super.onPlay();
            }

            public void onStop() {

                stop();

                Log.d(TAG, "onStop called (media button pressed)");
                super.onStop();
            }

            private void onPlayPauseButtonClicked() {

                //if pressed multiple times in 500 ms, skip to next song
                long currentTime = System.currentTimeMillis();
                Log.d(TAG, "onPlay: " + " current " + currentTime);
                if (currentTime - lastTimePlayPauseClicked < 500) {
                    Log.d(TAG, "onPlay: nextTrack on multiple play pause click");
                    if(currentTime - lastTimePlayPauseClicked<500){
                        handlePrevious();
                    }else {
                        handleNextSong();
                    }
                    return;
                }

                lastTimePlayPauseClicked = System.currentTimeMillis();
                playPause();
            }
        });
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        PlaybackStateCompat state = stateBuilder
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1)
                .build();

        mMediaSession.setPlaybackState(state);
        mMediaSession.setActive(true);
    }


    public void setMediaSessionMetadata(final boolean enable) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if (getCurrentSong() == null) return;
                MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, getCurrentSong().getName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, getCurrentSong().getArtist());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, getCurrentSong().getAlbumName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI
                        , RandomUtils.getAlbumArtUri(getCurrentSong().getAlbumId()).toString());
                metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, getCurrentSong().getDurationLong());
                if (MyApp.getPref().getBoolean(getString(R.string.pref_lock_screen_album_Art), true)) {
                    if (enable) {
                        Bitmap b = null;
                        try {
                            b = RandomUtils.decodeUri(getApplication()
                                    , RandomUtils.getAlbumArtUri(getCurrentSong().getAlbumId()), 200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, b);
                    } else {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, null);
                    }
                }
                mMediaSession.setMetadata(metadataBuilder.build());
            }
        });
    }


    public void setSessionState() {
        //set state play
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (mPlayer.getmPlayer().isPlaying()) {
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1);
            } else if (!mPlayer.getmPlayer().isPlaying()) {
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1);
            } else {
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1);
            }
            mMediaSession.setPlaybackState(stateBuilder.build());
        }
    }


    public void PostNotification() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                if (getCurrentSong() == null) {
                    return;
                }

                Bitmap b = null;
                try {
                    b = RandomUtils.decodeUri(getApplication()
                            , RandomUtils.getAlbumArtUri(getCurrentSong().getAlbumId()), 200);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (b != null) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    int maxWidth = 300;
                    int maxHeight = 300;
                    if (width > height) {
                        // landscape
                        float ratio = (float) width / maxWidth;
                        width = maxWidth;
                        height = (int) (height / ratio);
                    } else if (height > width) {
                        // portrait
                        float ratio = (float) height / maxHeight;
                        height = maxHeight;
                        width = (int) (width / ratio);
                    } else {
                        // square
                        height = maxHeight;
                        width = maxWidth;
                    }
                    b = Bitmap.createScaledBitmap(b, width, height, false);
                }


                String trackInfo = getCurrentSong().getArtist() + " - " + getCurrentSong().getName();

                final Notification notification;

                android.support.v4.media.app.NotificationCompat.MediaStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2);

                if (mMediaSession != null) {
                    mediaStyle.setMediaSession(mMediaSession.getSessionToken());
                }


                //For lollipop above devices, use media style notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MusicService.this, getString(R.string.notification_channel))
                        .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                        .setContentTitle(trackInfo)
                        .setContentText(getCurrentSong().getArtist())
                        //.setColor(ColorHelper.getColor(R.color.notification_color))
                        //
                        .setContentIntent(pendingIntent)

                        .setAutoCancel(false);

                //posting notification fails for huawei devices in case of mediastyle notification
                boolean isHuawei = (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1
                        || android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
                        && Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei");
                if (!isHuawei) {
                    builder.setStyle(mediaStyle);
                }

                if (b != null) {
                    builder.setLargeIcon(b);
                } else {
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
                }

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, "Prev", ppreviousIntent));

                if (mPlayer.getmPlayer().isPlaying()) {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_white_24dp, "Pause", pplayIntent));
                } else {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, "Play", pplayIntent));
                }

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, "Next", pnextIntent));


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    builder.setPriority(Notification.PRIORITY_MAX);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(getString(R.string.notification_channel));
                }

                notification = builder.build();

                if (isReallyPlaying()) {
                    //builder.setOngoing(true);
                    startForeground(99, notification);
                } else {
                    stopForeground(false);
                    mNotificationManager.notify(99, notification);
                }

            }
        });
    }


    private void InitializeIntents() {
        //Notification intents
        Intent notificationIntent;
        notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(ACTION_PREV_SONG);
        ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(ACTION_PAUSE_SONG);
        pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(ACTION_NEXT_SONG);
        pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);


    }


    class MediaStoreObserver extends ContentObserver implements Runnable{

        private Handler mHandler;
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this,500);
        }

        @Override
        public void run() {
            Log.d("Service ","refreshing");
            refresh();
        }
    }

    public void refresh(){

    }
}


