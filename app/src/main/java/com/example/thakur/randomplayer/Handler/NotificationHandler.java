package com.example.thakur.randomplayer.Handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;


import com.example.thakur.randomplayer.PlayerActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NotificationHandler {

    private static int NOTIFICATION_ID = 786524;
    private Context context;
    private MusicService service;
    private boolean notificationActive;

    private Notification notificationCompat;
    private NotificationManager notificationManager;

    public NotificationHandler(Context context, MusicService service) {
        this.context = context;
        this.service = service;
    }

    private Notification.Builder createBuiderNotification(boolean removable) {

        Intent notificationIntent = new Intent(context, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        notificationIntent.setAction(MusicService.ACTION_NOTI_CLICK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(MusicService.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        if (removable)
            return new Notification.Builder(context)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
        else
            return new Notification.Builder(context)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
    }

    public void setNotificationPlayer(boolean removable) {
        //createID();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = createBuiderNotification(removable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("saubhik", "myservice", NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("saubhik");


        }

        notificationCompat = builder.build();
        RemoteViews notiLayoutBig = new RemoteViews(context.getPackageName(),
                R.layout.notification_layout);
        RemoteViews notiCollapsedView = new RemoteViews(context.getPackageName(),
                R.layout.notification_small);
        if (Build.VERSION.SDK_INT >= 16) {
            notificationCompat.bigContentView = notiLayoutBig;
        }





        notificationCompat.contentView = notiCollapsedView;
        notificationCompat.priority = Notification.PRIORITY_MAX;

        if (!removable)
            service.startForeground(NOTIFICATION_ID, notificationCompat);
        try {
            notificationManager.notify(NOTIFICATION_ID, notificationCompat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationActive = true;
    }


    public void changeNotificationDetails(Song song, boolean playing) {
        if (Build.VERSION.SDK_INT >= 16) {
            //createID();
            notificationCompat.bigContentView.setTextViewText(R.id.noti_name, song.getName());
            notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, song.getArtist());
            notificationCompat.contentView.setTextViewText(R.id.noti_name, song.getName());
            notificationCompat.contentView.setTextViewText(R.id.noti_artist, song.getArtist());

            Intent playClick = new Intent();
            playClick.setAction(MusicService.ACTION_PAUSE_SONG);
            PendingIntent playClickIntent = PendingIntent.getBroadcast(context, 21021, playClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
            setPlayPause(playing);


            Intent prevClick = new Intent();
            prevClick.setAction(MusicService.ACTION_PREV_SONG);
            PendingIntent prevClickIntent = PendingIntent.getBroadcast(context, 21121, prevClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);
            setPlayPause(playing);

            Intent nextClick = new Intent();
            nextClick.setAction(MusicService.ACTION_NEXT_SONG);
            PendingIntent nextClickIntent = PendingIntent.getBroadcast(context, 21221, nextClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);
            setPlayPause(playing);

            String path = RandomUtils.getAlbumArt(context, song.getAlbumId());

            try {
                File f = null;
                if (path != null) {
                    f = new File(path);

                    Picasso.with(context).load(new File(path)).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                            notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);


                            //notificationManager.notify(NOTIFICATION_ID, notificationCompat);

                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            setDefaultImageView();
                            //notificationManager.notify(NOTIFICATION_ID,notificationCompat);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });

                }

            } catch (Exception e) {
                setDefaultImageView();
                //notificationManager.notify(NOTIFICATION_ID,notificationCompat);
            }


            notificationManager.notify(NOTIFICATION_ID, notificationCompat);
        }

    }

    private void setDefaultImageView() {

        notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art,
                RandomUtils.getBitmapOfVector(context, R.drawable.default_art,
                        RandomUtils.dpToPx(context, 100), RandomUtils.dpToPx(context, 100)));
        notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art,
                RandomUtils.getBitmapOfVector(context, R.drawable.default_art,
                        RandomUtils.dpToPx(context, 50), RandomUtils.dpToPx(context, 50)));
        //notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public void updateNotificationView() {
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public boolean isNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

    public Notification getNotificationCompat() {
        return notificationCompat;
    }

    public void createID() {
        Date now = new Date();
        NOTIFICATION_ID = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));

    }

    public void setPlayPause(boolean isPlaying) {

        if (isPlaying) {
            //playStateRes = R.drawable.ic_pause_white_36dp;
            notificationCompat.bigContentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause_white_36dp);
            notificationCompat.contentView.setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause_white_36dp);
        } else {
            //playStateRes = R.drawable.ic_play_arrow_white_48dp;
            notificationCompat.bigContentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play_arrow_white_48dp);
            notificationCompat.contentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play_arrow_white_48dp);
        }
    }
}
