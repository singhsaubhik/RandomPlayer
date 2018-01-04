package com.example.thakur.randomplayer.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.thakur.randomplayer.Adapters.ArtistsListAdapter;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Artist;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistList extends Fragment {

    private Context context;
    private RecyclerView recyclerView;
    private TextView tv;
    private ArrayList<Artist> list = new ArrayList<>();

    public ArtistList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_list, container, false);
        this.context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.artist_recycler);
        list = ListSongs.getArtistList(context);
        ArtistsListAdapter adapter = new ArtistsListAdapter(context,list,this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));


        super.onViewCreated(view, savedInstanceState);
    }

}
