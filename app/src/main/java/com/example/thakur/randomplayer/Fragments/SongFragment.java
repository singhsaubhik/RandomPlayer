package com.example.thakur.randomplayer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.Config;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Adapters.SongListAdapter;
import com.example.thakur.randomplayer.Utilities.Helper;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;


import java.util.ArrayList;

/**
 * Created by Thakur on 01-10-2017
 */

public class SongFragment extends android.support.v4.app.Fragment {

    private ArrayList<Song> songList = new ArrayList<>();
    private SongListAdapter songListAdapter;
    private FastScrollRecyclerView mRecycler;
    private Context context;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_song_list, container, false);
        context = view.getContext();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        songList = SongLoader.getSongList(context);
        Log.e("form song fragment",""+songList.get(1).getPath());
        songListAdapter = new SongListAdapter(context, songList);
        mRecycler = view.findViewById(R.id.song_recyclerview);


        mRecycler.setThumbColor(getResources().getColor(R.color.colorAccent));
        mRecycler.setPopupBgColor(getResources().getColor(R.color.colorAccent));



        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecycler.setAdapter(songListAdapter);
        mRecycler.setHasFixedSize(true);
        //mRecycler.addItemDecoration(new decorator(3));



        super.onViewCreated(view, savedInstanceState);
    }






}
