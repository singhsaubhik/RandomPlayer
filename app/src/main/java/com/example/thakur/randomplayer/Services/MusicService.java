package com.example.thakur.randomplayer.Services;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
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
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, ShakeDetector.Listener {

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


    public static int time = 0;

    public static boolean isPlaying = true;
    public static boolean isServiceRunning;
    public static boolean musicPausedBecauseOfCall;
    private static boolean mPausedByTransientLossOfFocus = false;
    private AudioManager audioManager;

    private boolean notHandledMetaChangedForCurrentTrack;

    //Shake Detector
    private static SensorManager sensorManager;
    private static ShakeDetector shakeDetector;


    private PowerManager.WakeLock wakeLock;
    public RemoteControlClient mRemoteControlClient;
    private ComponentName mMediaButtonReceiverComponent;
    private AudioManager mAudioManager;


    private boolean becomingNoisyReceiverRegistered;


    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String command = intent.getStringExtra(CMDNAME);


            handleCommandIntent(intent);

        }
    };


    private IntentFilter becomingNoisyReceiverIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                //pause();
                mPlayer.pause();
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
    MediaSessionCompat mSession;


    BroadcastReceiver mReciever = new BroadcastReceiver() {
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

    BroadcastReceiver headPhones = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isReallyPlaying()) {
                mPlayer.pause();
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
                    playSongById(intent.getLongExtra("songId", 0));
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




        pref = new UserPreferenceHandler(getApplicationContext());
        phoneStateListener();
        //Shake detector listener
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        //initilizeShakeDetector();
        //registerReceiver(headPhones, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(becomingNoisyReceiver, becomingNoisyReceiverIntentFilter);


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


        // Initialize the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);

        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);

        setUpRemoteControlClient();


        songList = SongLoader.getSongList(getApplicationContext());
        mPlayer = new PlayerHandler(getApplicationContext());
        mPlayer.init(songList);
        mPlayer.getmPlayer().setOnCompletionListener(this);

        registerReceiver(mReciever, intentFilter);
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
        //pref.setLastPlayed(songPos);
        //ref.setLastplayedDur(mPlayer.getmPlayer().getCurrentPosition());
        super.onDestroy();
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
            updateNotificationPlayer();
        } else {
            mPlayer.start();
            isPlaying = true;
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


    public void setFolderSongTitle(String str) {
        folderSongTitle = str;
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

    private void initilizeShakeDetector() {
        if (pref.getHearShake()) {
            setShakeListener(true);
        } else {
            setShakeListener(false);
        }
    }


    private void setUpMediaSession() {
        mSession = new MediaSessionCompat(this, "Random");
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPause() {
                mPlayer.pause();
                mPausedByTransientLossOfFocus = false;
            }

            @Override
            public void onPlay() {
                if (mPlayer != null) {
                    mPlayer.start();
                }
            }

            @Override
            public void onSeekTo(long pos) {
                //seek(pos);
            }

            @Override
            public void onSkipToNext() {
                handleNotificationNext();
            }

            @Override
            public void onSkipToPrevious() {
                handleNotificationPrev();
            }

            @Override
            public void onStop() {
                mPlayer.pause();
                mPausedByTransientLossOfFocus = false;
                handleSeek(0);
                //releaseServiceUiAndStop();
            }
        });
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }


    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUpRemoteControlClient() {
        //Legacy for ICS
        if (mRemoteControlClient == null) {
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);

            // create and register the remote control client
            mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
            mAudioManager.registerRemoteControlClient(mRemoteControlClient);
        }

        mRemoteControlClient.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);
    }


    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICECMD.equals(action) ? intent.getStringExtra(CMDNAME) : null;

        //if (D) Log.d(TAG, "handleCommandIntent: action = " + action + ", command = " + command);

        /*if (NotificationHelper.checkIntent(intent)) {
            goToPosition(mPlayPos + NotificationHelper.getPosition(intent));
            return;
        }*/

        if (CMDNEXT.equals(command) || NEXT_ACTION.equals(action)) {
            handleNotificationNext();
        } else if (CMDPREVIOUS.equals(command) || PREVIOUS_ACTION.equals(action)) {
            //prev(PREVIOUS_FORCE_ACTION.equals(action));
            handleNotificationPrev();
        } else if (CMDTOGGLEPAUSE.equals(command) || TOGGLEPAUSE_ACTION.equals(action)) {
            if (isReallyPlaying()) {
                mPlayer.pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                mPlayer.start();
            }
        } else if (CMDPAUSE.equals(command) || PAUSE_ACTION.equals(action)) {
            mPlayer.pause();
            mPausedByTransientLossOfFocus = false;
        } else if (CMDPLAY.equals(command)) {
            mPlayer.start();
        }
    }


}