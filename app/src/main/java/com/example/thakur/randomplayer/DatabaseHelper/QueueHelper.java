package com.example.thakur.randomplayer.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;

/**
 * Created by Thakur on 19-02-2018
 */

public class QueueHelper extends SQLiteOpenHelper {

    public static final String QUEUE_DATABASE_NAME = "queue_database_name.db";
    private static int VERSION = 1;

    public static final String QUEUE_TABLE_NAME = "queue_table_name";
    public static final String SONG_ID = "song_id";

    public QueueHelper(Context context) {
        super(context, QUEUE_DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+QUEUE_TABLE_NAME+" (song_id INTEGER)" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+QUEUE_TABLE_NAME);
        this.onCreate(db);
    }

    public void addSongIds(ArrayList<Song> list){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            for (int i = 0; i < list.size();i++){
                values.put(SONG_ID,list.get(i).getSongId());
                db.insert(QUEUE_TABLE_NAME,null,values);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Cursor makeCursor(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(QUEUE_TABLE_NAME,new String[]{SONG_ID},null,null,null,null,null);
    }


    public ArrayList<Integer> getQueue(){
        ArrayList<Integer> list = new ArrayList<>();
        Cursor cr = makeCursor();
        if(cr!=null && cr.moveToFirst()){
            while(cr.moveToNext()){
                list.add(cr.getInt(0));
            }
        }
        return list;
    }
}
