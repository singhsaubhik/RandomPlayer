package com.example.thakur.randomplayer.Utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * Created by Thakur on 29-01-2018
 */

public class RandomUtils {
    public static void removeFromPlaylist(final Context context, final long id,
                                          final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = ? ", new String[]{
                Long.toString(id)
        });
    }


    public enum PlaylistType {
        LastAdded(-1, R.string.playlist_last_added),
        RecentlyPlayed(-2, R.string.playlist_recently_played),
        TopTracks(-3, R.string.playlist_top_tracks);

        public long mId;
        public int mTitleId;

        PlaylistType(long id, int titleId) {
            mId = id;
            mTitleId = titleId;
        }

        public static PlaylistType getTypeById(long id) {
            for (PlaylistType type : PlaylistType.values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            return null;
        }
    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }

    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

    public static int getActionBarHeight(Context context) {
        int mActionBarHeight;
        TypedValue mTypedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, context.getResources().getDisplayMetrics());

        return mActionBarHeight;
    }

    public static String getAlbumArtForFile(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
    }

    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }


    public static String getAlbumArt(Context context, long albumdId) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumdId)},
                null);
        String imagePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        return imagePath;
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

    public static void showSongDetailDialog(Context context, Song items) {
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(items.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaFormat mf = mex.getTrackFormat(0);

        int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
        int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        String mime = mf.getString(MediaFormat.KEY_MIME);
        File songFile = new File(items.getPath());
        float file_size = (songFile.length() / 1024);
        StringBuilder content = new StringBuilder();
        content.append("Song Name: ");
        content.append(items.getName());
        content.append("\n\n");
        content.append("Album Name: ");
        content.append(items.getAlbumName());
        content.append("\n\n");
        content.append("Artist Name: ");
        content.append(items.getArtist());
        content.append("\n\n");
        content.append("File path: ");
        content.append(items.getPath());
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



    public static void shareSongFile(Context context, ArrayList<Song> items, int position) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("file:///" + items.get(position).getPath()));
        context.startActivity(Intent.createChooser(share,
                context.getString(R.string.share_song_file)));
    }




    public static boolean isPathValid(String path){
        return path != "" && path != null && isFileExist(path);
    }

    private static boolean isFileExist(String path){
        File file = new File(path);
        return file.exists();
    }



    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getWindowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }




    public static Bitmap getBitmapOfVector(@NonNull Context context , @DrawableRes int id, int height, int width) {
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



    public static int dpToPx(@NonNull Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }



    public static void SetRingtone(final Context context, final String filePath , final String songTitle){
        if(!checkSystemWritePermission(context)){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            openAndroidPermissionsMenu(context);
                            //Yes button clicked
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Please provide write setting permission for application to change Ringtone")
                    .setPositiveButton("Sure", dialogClickListener)
                    .setNegativeButton("Not now", dialogClickListener).show();

        }else {

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    File k = new File(filePath);

                    File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
                            .getAbsolutePath()
                            + "/RandomPlayer_Music_tone.mp3");
                    try
                    {
                        newFile.createNewFile();
                        copy(k,newFile);
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    if(!k.canRead()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Unable to set ringtone: " + songTitle, Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, newFile.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, songTitle+" Tone");
                    values.put(MediaStore.MediaColumns.SIZE, k.length());
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    values.put(MediaStore.Audio.Media.DURATION, 230);
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

//Insert it into the database
                    Uri uri1 = MediaStore.Audio.Media.getContentUriForPath(newFile.getAbsolutePath());
                    context.getContentResolver().delete(uri1, MediaStore.MediaColumns.DATA + "=\"" + newFile.getAbsolutePath() + "\"",
                            null);
                    Uri newUri = context.getContentResolver().insert(uri1, values);

                    RingtoneManager.setActualDefaultRingtoneUri(
                            context,
                            RingtoneManager.TYPE_RINGTONE,
                            newUri
                    );

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Ringtone set: " + songTitle, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });



        }
    }




    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
            //Log.d(Constants.TAG, "Can Write Settings: " + retVal);
        }
        return retVal;
    }




    private static void openAndroidPermissionsMenu(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }




    public static boolean isConnectedToInternet(){
        ConnectivityManager
                cm = (ConnectivityManager) MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null
                && activeNetwork.isConnectedOrConnecting()) {
            return true;
        }else {
            //Toast.makeText(MyApp.getContext(), "No internet connection!", Toast.LENGTH_LONG).show();
            return false;
        }
    }



    public static int getIdFromTitle(Context context , String title){
        ContentResolver cr = context.getContentResolver();
        if(title.contains("'")){
            //title = ((char)34+title+(char)34);
            //fuck you bug
            //you bugged my mind
            title = title.replaceAll("'","''");
        }
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection =  MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                +MediaStore.Audio.Media.TITLE  + "= '" +   title  +"'";
        String[] projection = {
                MediaStore.Audio.Media._ID
        };
        Cursor cursor=cr.query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC");

        if(cursor!=null){
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            return  id;
        }
        return 0;
    }




    public static String getTitleFromFilePath(Context context ,String filePath){
        if(filePath.contains("\"")){
            //filePath = RandomUtils.escapeDoubleQuotes(filePath);
        }
        ContentResolver cr = context.getContentResolver();
        Uri videosUri = MediaStore.Audio.Media.getContentUri("external");
        String[] projection = {MediaStore.Audio.Media.TITLE};
        Cursor cursor = cr.query(videosUri, projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[] { filePath }, null);
        if(cursor!=null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            String id = "";
            try {
                id = cursor.getString(0);
            } catch (Exception e) {
                cursor.close();
                return null;
            }
            cursor.close();
            return id;
        }else {
            return null;
        }
    }



    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException, SecurityException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;


        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }




}
