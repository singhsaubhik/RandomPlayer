package com.example.thakur.randomplayer.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;

/**
 * Created by Thakur on 26-12-2017.
 */

public class SongDatabase extends SQLiteOpenHelper {
    public static final String DatabseName = "songdatabse.db";
    public static final String TableName = "recentlyPlayed";

    //KEYS
    private final String TITLE = "title";
    private final String ID = "id";

    public SongDatabase(Context context) {
        super(context, DatabseName, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TableName+" (ID INTEGER PRIMARY KEY,TITLE TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TableName);
    }

    public boolean insertSong(String id,String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TITLE,title);
        v.put(ID,id);

        long i = db.insert(TableName,null,v);
        if(i==-1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getSongs(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cr = db.rawQuery("SELECT * FROM "+TableName,null);
        return cr;

    }


}
