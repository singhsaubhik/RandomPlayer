package com.example.thakur.randomplayer.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thakur.randomplayer.Adapters.PlaylistList_simple;
import com.example.thakur.randomplayer.DatabaseHelper.PlaylistDatabase;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Playlist extends Fragment {

    public Playlist() {
        // Required empty public constructor
    }


    private Context context;
    private ArrayList<com.example.thakur.randomplayer.items.Playlist> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private PlaylistList_simple adapter;
    private FloatingActionButton fab;

    private PlaylistDatabase db;

    private ArrayList<Song> songList = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        // Inflate the layout for this fragment
        context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
       /* list.add("Recently Added");
        list.add("Most Played");
        list.add("Recently played");
        list.add("My Fav");*/
       //songList = ListSongs.getSongList(context);
        fab = view.findViewById(R.id.add_playlist);
        db = new PlaylistDatabase(context);
        list.clear();
        list = db.getAllplaylist();


        recyclerView = view.findViewById(R.id.playlist_list_recycler);

        adapter = new PlaylistList_simple(context,list);
        LinearLayoutManager manager = new LinearLayoutManager(context);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaylist();
            }
        });
    }

    /*private ArrayList<String> test(){
        ArrayList<String> l = new ArrayList<>();
        String str;
        Cursor cr = new SongDatabase(context).getSongs();
        if(cr!=null && cr.getCount()>0){
            cr.moveToFirst();
            while(cr.moveToNext()){
                str = cr.getString(1);
                l.add(str);
            }
        }
        return l;
    }*/

    private void addPlaylist(){
        boolean a = new PlaylistDatabase(context).addPlaylist(1,"Kuchh bhi",90);
        //boolean b = db.addPlaylist(1,"Recently Played",30);
        //boolean c = db.addPlaylist(1,"Most Played",10);

        if(a){
            Toast.makeText(context, "Inserted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
