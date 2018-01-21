package com.example.thakur.randomplayer.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.thakur.randomplayer.items.Playlist;

import java.util.ArrayList;

/**
 * Created by Thakur on 26-12-2017.
 */

public class PlaylistDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "playlist.db";
    private static final String TABLE_NAME = "playlisttable";
    private static final String ID = "playlistId";
    private static final String NAME = "playlistName";
    private static final String COVER_ALBUM_ID = "coveralbumid";
    private static final int VERSION = 7;


    public PlaylistDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (playlistId INTEGER PRIMARY KEY AUTOINCREMENT, playlistName TEXT,coveralbumid long)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public boolean addPlaylist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        String playlist_name = name.replace(" ","_");


            ContentValues v = new ContentValues();
            v.put(NAME, playlist_name);
            //v.putNull(COVER_ALBUM_ID);


            long i = db.insert(TABLE_NAME, null, v);
            //db.close();
            return i != -1;

    }

    public Cursor makeCursorToGetAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = new String[]{ID, NAME, COVER_ALBUM_ID};
        //Cursor cr = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);

        return db.query(TABLE_NAME, projection, null, null, null, null, null);
    }

    public ArrayList<Playlist> getAllplaylist() {
        ArrayList<Playlist> list = new ArrayList<>();
        String str;
        int i;
        long j;
        Cursor cr = makeCursorToGetAll();
        if (cr != null && cr.getCount() > 0) {
            cr.moveToFirst();
            while (cr.moveToNext()) {
                i = cr.getInt(0);
                str = cr.getString(1);
                j = cr.getInt(2);

                str = str.replace("_"," ");

                list.add(new Playlist(i, str, j));
            }
        }
        return list;
    }

    public void renamePlaylist(int id, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE playlisttable SET playlistName" + "='" + newName + "' WHERE " + ID + "='" + id + "'", null);
        db.close();
    }

    public void deletePlaylist(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "playlistId = ?", new String[]{String.valueOf(id)});
        db.close();
    }


}
