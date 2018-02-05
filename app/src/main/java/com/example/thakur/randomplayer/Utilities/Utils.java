package com.example.thakur.randomplayer.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;


import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Thakur on 27-10-2017
 */

public class Utils {
    private static Context context;
    public Utils(Context context){
        this.context = context;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public Bitmap getBitmapOfVector(@DrawableRes int id, int height, int width) {
        Bitmap bm;
        Drawable vectorDrawable = null;
        if (Build.VERSION.SDK_INT >= 21) {
            vectorDrawable = context.getDrawable(id);
        }

        if (vectorDrawable != null)
            vectorDrawable.setBounds(0, 0, width, height);
        bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        if (vectorDrawable != null)
            vectorDrawable.draw(canvas);

        return bm;


    }

    public int getWindowWidth() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public int getWindowHeight() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static boolean isMarshMallowPlus(){
        if(Build.VERSION.SDK_INT>=21){
            return true;
        }
        else{
            return false;
        }
    }

    public static boolean isPathValid(String path){
        return path != "" && path != null && isFileExist(path);
    }

    private static boolean isFileExist(String path){
        File file = new File(path);
        return file.exists();
    }

    public static void shareSongFile(Context context, ArrayList<Song> items, int position) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("file:///" + items.get(position).getPath()));
        context.startActivity(Intent.createChooser(share,
                context.getString(R.string.share_song_file)));
    }

    public static void setAsRingtone(Context context,String filepath) {
        File ringtoneFile = new File(filepath);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
        content.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName());
        content.put(MediaStore.MediaColumns.SIZE, 215454);
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(MediaStore.Audio.Media.DURATION, 230);
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        content.put(MediaStore.Audio.Media.IS_ALARM, true);
        content.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(
                ringtoneFile.getAbsolutePath());


//        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"",
//                null);
        Uri newUri = context.getContentResolver().insert(uri, content);
        RingtoneManager.setActualDefaultRingtoneUri(
                context, RingtoneManager.TYPE_RINGTONE,
                newUri);

        Toast.makeText(context, "Ringtone changed", Toast.LENGTH_SHORT).show();
    }

    public static void showSongDetailDialog(Context context,ArrayList<Song> items, int position) {
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(items.get(position).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaFormat mf = mex.getTrackFormat(0);

        int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
        int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        String mime = mf.getString(MediaFormat.KEY_MIME);
        File songFile = new File(items.get(position).getPath());
        float file_size = (songFile.length() / 1024);
        StringBuilder content = new StringBuilder();
        content.append("Song Name: ");
        content.append(items.get(position).getName());
        content.append("\n\n");
        content.append("Album Name: ");
        content.append(items.get(position).getAlbumName());
        content.append("\n\n");
        content.append("Artist Name: ");
        content.append(items.get(position).getArtist());
        content.append("\n\n");
        content.append("File path: ");
        content.append(items.get(position).getPath());
        content.append("\n\n");
        content.append("File name: ");
        content.append(songFile.getName());
        content.append("\n\n");
        content.append("File Name");
        content.append(mime);
        content.append("\n\n");
        content.append("File Size");
        content.append(String.valueOf(String.format("%.2f", file_size / 1024)));
        content.append(" MB");
        content.append("\n\n");
        content.append("Bitrate: ");
        content.append(String.valueOf(bitRate / 1000));
        content.append(" kb/s");
        content.append("\n\n");
        content.append("Sampling Rate: ");
        content.append(sampleRate);
        content.append(" Hz");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Song info");
        builder.setMessage(content);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            c.moveToFirst();
            while (!c.isAfterLast()) {
                // Remove from current playlist
                final long id = c.getLong(0);
                //MusicPlayer.removeTrack(id);
                // Remove the track from the play count
                //SongPlayCount.getInstance(context).removeItem(id);
                // Remove any items in the recents database
                //RecentStore.getInstance(context).removeItem(id);
                c.moveToNext();
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        //final String message = makeLabel(context, R.plurals.NNNtracksdeleted, list.length);

        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        //MusicPlayer.refresh();
    }


    public static int[] getAvailableColor(Context context,Palette palette) {
        int[] temp = new int[3];
        try {
            //int[] temp = new int[3];
            if (palette.getVibrantSwatch() != null) {
                temp[0] = palette.getVibrantSwatch().getRgb();
                temp[1] = palette.getVibrantSwatch().getBodyTextColor();
                temp[2] = palette.getVibrantSwatch().getTitleTextColor();


            } else if (palette.getDarkVibrantSwatch() != null) {
                temp[0] = palette.getDarkVibrantSwatch().getRgb();
                temp[1] = palette.getDarkVibrantSwatch().getBodyTextColor();
                temp[2] = palette.getDarkVibrantSwatch().getTitleTextColor();

            } else if (palette.getDarkMutedSwatch() != null) {
                temp[0] = palette.getDarkMutedSwatch().getRgb();
                temp[1] = palette.getDarkMutedSwatch().getBodyTextColor();
                temp[2] = palette.getDarkMutedSwatch().getTitleTextColor();
            } else {
                temp[0] = ContextCompat.getColor(context, R.color.colorPrimary);
                temp[1] = ContextCompat.getColor(context, android.R.color.white);
                temp[2] = 0xffe5e5e5;
            }
        }catch (Exception e){
            temp[0] = ContextCompat.getColor(context, R.color.colorPrimary);
            temp[1] = ContextCompat.getColor(context, android.R.color.white);
            temp[2] = 0xffe5e5e5;
        }
        return temp;
    }


}
