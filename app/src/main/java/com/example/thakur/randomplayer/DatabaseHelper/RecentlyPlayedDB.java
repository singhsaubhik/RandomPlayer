package com.example.thakur.randomplayer.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.items.Song;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Thakur on 13-01-2018.
 */

public class RecentlyPlayedDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "recentlyplayed.db";
    private static final String TABLE_NAME = "table_name";
    private static final int DATABASE_VERSION = 1;

    private static final String SONG_ID = "SONGID";
    private static final String MYDATE = "MYDATE";

    public RecentlyPlayedDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+ " (SONGID LONG,MYDATE DATE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addSong(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SONG_ID,id);
        contentValues.put(MYDATE,getDateTime());


        return true;
    }

    private Cursor getCursor(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = new String[]{SONG_ID,MYDATE};
        return db.query(TABLE_NAME,projection,null,null,null,null,"MYDATE DESC");
    }

    private ArrayList<Long> getId(){
        ArrayList<Long> list = new ArrayList<>();
        long l;
        Cursor cr = getCursor();
        if(cr!=null && cr.moveToFirst()){
            cr.moveToFirst();

            while(cr.moveToFirst()){
                l = cr.getLong(0);
                list.add(l);
            }
        }

        return list;
    }

    public ArrayList<Song> getRecentlyPlayed(Context context){
        ArrayList<Song> list = new ArrayList<>();
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<Song> finallist = new ArrayList<>();

        list = ListSongs.getSongList(context);

        for(int i=0;i<ids.size();i++){
            for(int j=0;j<list.size();i++){
                if(ids.get(i)==list.get(j).getAlbumId()){
                    finallist.add(list.get(j));
                }
            }
        }

        return finallist;
    }

    private String getDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        return sdf.format(date);
    }
}
