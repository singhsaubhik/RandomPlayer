package com.example.thakur.randomplayer.DatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

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
}
