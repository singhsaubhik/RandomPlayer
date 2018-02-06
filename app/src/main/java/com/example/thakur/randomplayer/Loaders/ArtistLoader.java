/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.example.thakur.randomplayer.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;


import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.items.Artist;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {

    public static Artist getArtist(Cursor cursor) {
        Artist artist = new Artist();
        if (cursor != null) {
            if (cursor.moveToFirst())
                artist = new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3));
        }
        if (cursor != null)
            cursor.close();
        return artist;
    }

    public static List<Artist> getArtistsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                arrayList.add(new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3)));
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }

    public static List<Artist> getAllArtists(Context context) {
        return getArtistsForCursor(makeArtistCursor(context, null, null));
    }

    public static Artist getArtist(Context context, long id) {
        return getArtist(makeArtistCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static List<Artist> getArtists(Context context, String paramString, int limit) {
        List<Artist> result = getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", new String[]{paramString + "%"}));
        if (result.size() < limit) {
            result.addAll(getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", new String[]{"%_" + paramString + "%"})));
        }
        return result.size() < limit ? result : result.subList(0, limit);
    }


    public static Cursor makeArtistCursor(Context context, String selection, String[] paramArrayOfString) {
        final String artistSortOrder = PreferencesUtility.getInstance(context).getArtistSortOrder();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{"_id", "artist", "number_of_albums", "number_of_tracks"}, selection, paramArrayOfString, artistSortOrder);
        return cursor;
    }





    public static ArrayList<Song> getSongsListOfArtist(Context context, String artistName) {
        ArrayList<Song> songList = new ArrayList<>();
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.ARTIST + "='" + artistName.replace("'", "''") + "'";
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
        musicCursor.close();
        return songList;
    }






    public static long getArtistIdFromName(Context context, String name) {
        System.gc();
        String where = MediaStore.Audio.Artists.ARTIST + "='" + name + "'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);
            return musicCursor.getLong(idColumn);
        }
        return 0;
    }





    public static ArrayList<Artist> getArtistList(Context context) {
        ArrayList<Artist> albumList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

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
                albumList.add(new Artist(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getInt(numOfAlbumsColumn),
                        musicCursor.getInt(numOfTracksColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return albumList;
    }
}
