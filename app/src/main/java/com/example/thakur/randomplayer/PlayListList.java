package com.example.thakur.randomplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import com.example.thakur.randomplayer.Adapters.PlayListListAdapter;
import com.example.thakur.randomplayer.DatabaseHelper.PlaylistDatabase;
import com.example.thakur.randomplayer.Loaders.LastAddedLoader;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Playlist;
import com.example.thakur.randomplayer.items.Song;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayListList extends AppCompatActivity {

    MultiSnapRecyclerView recyclerView;
    ArrayList<Playlist> list = new ArrayList<>();
    PlaylistDatabase db;
    UserPreferenceHandler pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_list);
        pref = new UserPreferenceHandler(this);
        createDefaultPlaylist();

        db = new PlaylistDatabase(this);


        //LastAddedLoader.makeLastAddedCursor(getApplicationContext());
        recyclerView = findViewById(R.id.play_list_list_recycler);
        list = db.getAllplaylist();

//        Toast.makeText(this, ""+list.get(0).getPlaylistName(), Toast.LENGTH_SHORT).show();


        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new PlayListListAdapter(list));
        recyclerView.setHasFixedSize(true);


    }

    private void createDefaultPlaylist() {
        if(pref.getDefaultPlaylistCreated()) {

            final PlaylistDatabase db1 = new PlaylistDatabase(this);

            db1.addPlaylist("Recently Added");
            db1.addPlaylist("Recently Played");
            db1.addPlaylist("Most Played");


            pref.setDefaultPlaylistCreated(true);
        }

    }
}
