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
    private static final String SONGCOUNT = "playlistSongCount";


    public PlaylistDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" (playlistId INTEGER PRIMARY KEY, playlistName TEXT, playlistSongCount INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

    }

    public boolean addPlaylist(int id,String name, int songCount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();

        v.put(ID,id);
        v.put(NAME,name);
        v.put(SONGCOUNT,songCount);

        long i = db.insert(TABLE_NAME,null,v);
        if(i==-1){
            return false;
        }else{
            return true;
        }
    }

    private Cursor makeCursorToGetAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = new String[]{ID,NAME,SONGCOUNT};
        //Cursor cr = db.query(TABLE_NAME,projection,null,null,null,null,NAME+" DESC");
        Cursor cr = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);

        return cr;
    }

    public ArrayList<Playlist> getAllplaylist(){
        ArrayList<Playlist> list = new ArrayList<>();
        String str;
        int i,j;
        Cursor cr = makeCursorToGetAll();
        if(cr!=null && cr.getCount()>0){
            cr.moveToFirst();
            while(cr.moveToNext()){
                i = cr.getInt(0);
                str = cr.getString(1);
                j= cr.getInt(2);

                list.add(new Playlist(i,str,j));
            }
        }
        return list;
    }
}
