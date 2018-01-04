package com.example.thakur.randomplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.example.thakur.randomplayer.Adapters.PlayListListAdapter;
import com.example.thakur.randomplayer.Loaders.LastAddedLoader;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.items.Song;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.util.ArrayList;

public class PlayListList extends AppCompatActivity {

    MultiSnapRecyclerView recyclerView;
    ArrayList<Song> list = new ArrayList<>();
    ArrayList<Song> list2 = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_list);

        //LastAddedLoader.makeLastAddedCursor(getApplicationContext());
        recyclerView = findViewById(R.id.play_list_list_recycler);
        list = (ArrayList<Song>) LastAddedLoader.getLastAddedSongs(getApplicationContext());

        list2.add(list.get(0));
        list2.add(list.get(1));


        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new PlayListListAdapter(list2));
        recyclerView.setHasFixedSize(true);


    }
}
