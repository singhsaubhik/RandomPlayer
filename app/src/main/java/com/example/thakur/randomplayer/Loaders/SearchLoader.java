package com.example.thakur.randomplayer.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.thakur.randomplayer.items.Album;
import com.example.thakur.randomplayer.items.Artist;
import com.example.thakur.randomplayer.items.Search;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;

/**
 * Created by Thakur on 05-02-2018.
 */

public class SearchLoader {

    public static Search getSearchResults(Context context, String sQuery) {
        System.gc();
        ArrayList<Song> songList = searchSong(context, sQuery);
        ArrayList<Album> albumList = searchAlbum(context, sQuery);
        ArrayList<Artist> artistList = searchArtist(context, sQuery);
        return new Search(songList, albumList, artistList);
    }

    private static ArrayList<Song> searchSong(Context context, String sQuery) {
        ArrayList<Song> songList = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.TITLE + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
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
            int songDurationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            do {
                songList.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(songDurationColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return songList;
    }

    private static ArrayList<Album> searchAlbum(Context context, String sQuery) {
        ArrayList<Album> albumList = new ArrayList<>();
        final String where = MediaStore.Audio.Albums.ALBUM + " LIKE '%" + sQuery.replace("'", "''") + "%'";
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


    private static ArrayList<Artist> searchArtist(Context context, String sQuery) {
        ArrayList<Artist> artistList = new ArrayList<>();
        final String where = MediaStore.Audio.Artists.ARTIST + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);
            int numOfAlbumsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int numOfTracksColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            //add albums to list
            do {
                artistList.add(new Artist(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getInt(numOfAlbumsColumn),
                        musicCursor.getInt(numOfTracksColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return artistList;
    }
}
