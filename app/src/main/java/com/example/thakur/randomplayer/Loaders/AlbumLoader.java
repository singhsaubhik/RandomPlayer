package com.example.thakur.randomplayer.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;


import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.items.Album;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;
import java.util.List;

public class AlbumLoader {


    public static Album getAlbum(Cursor cursor) {
        Album album = new Album();
        if (cursor != null) {
            if (cursor.moveToFirst())
                album = new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5),cursor.getString(6));
        }
        if (cursor != null)
            cursor.close();
        return album;
    }


    public static List<Album> getAlbumsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                arrayList.add(new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5)));
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }

    public static List<Album> getAllAlbums(Context context) {
        return getAlbumsForCursor(makeAlbumCursor(context, null, null));
    }

    public static Album getAlbum(Context context, long id) {
        return getAlbum(makeAlbumCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static List<Album> getAlbums(Context context, String paramString, int limit) {
        List<Album> result = getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", new String[]{paramString + "%"}));
        if (result.size() < limit) {
            result.addAll(getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", new String[]{"%_" + paramString + "%"})));
        }
        return result.size() < limit ? result : result.subList(0, limit);
    }


    public static Cursor makeAlbumCursor(Context context, String selection, String[] paramArrayOfString) {
        final String albumSortOrder = MediaStore.Audio.Albums.ALBUM;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{"_id", "album", "artist", "artist_id", "numsongs", "minyear", MediaStore.Audio.Albums.ALBUM_ART}, selection, paramArrayOfString, albumSortOrder);

        return cursor;
    }



    public static ArrayList<Song> getAlbumSongList(Context context, long albumId) {
        System.gc();
        Cursor musicCursor;
        ArrayList<Song> songs = new ArrayList<>();
        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {String.valueOf(albumId)};
        String orderBy = MediaStore.Audio.Media._ID;

        musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, whereVal, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int albumSongDuration = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            do {
                songs.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(albumSongDuration)));
            }
            while (musicCursor.moveToNext());
        }
        return songs;
    }





    public static ArrayList<Album> getAlbumListOfArtist(Context context, long artistId) {
        final ArrayList<Album> albumList = new ArrayList<>();
        System.gc();
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                        null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
            int albumArtColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new Album(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        false, musicCursor.getString(albumArtColumn),
                        musicCursor.getInt(numOfSongsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
        return albumList;
    }







    public static Album getAlbumFromId(Context context, long albumId) {
        System.gc();
        final String where = MediaStore.Audio.Albums._ID + "='" + albumId + "'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumArtColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            return new Album(musicCursor.getLong(idColumn),
                    musicCursor.getString(titleColumn),
                    musicCursor.getString(artistColumn),
                    false, musicCursor.getString(albumArtColumn),
                    musicCursor.getInt(numOfSongsColumn));
        }
        return null;
    }







    public static ArrayList<Album> getAlbumList(Context context) {
        final ArrayList<Album> albumList = new ArrayList<>();

        System.gc();
        final String orderBy = MediaStore.Audio.Albums.ALBUM;
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumArtColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new Album(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        false, musicCursor.getString(albumArtColumn),
                        musicCursor.getInt(numOfSongsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return albumList;
    }
}
