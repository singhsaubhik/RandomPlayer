package com.example.thakur.randomplayer;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.example.thakur.randomplayer.Adapters.PlayListListAdapter;
import com.example.thakur.randomplayer.DatabaseHelper.PlaylistDatabase;
import com.example.thakur.randomplayer.Loaders.LastAddedLoader;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Playlist;
import com.example.thakur.randomplayer.items.Song;
import com.github.clans.fab.FloatingActionButton;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayListList extends AppCompatActivity {

    MultiSnapRecyclerView recyclerView;
    ArrayList<Playlist> list = new ArrayList<>();
    PlaylistDatabase db;
    UserPreferenceHandler pref;

    FloatingActionButton fab_addplaylist;
    PlayListListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_list);
        pref = new UserPreferenceHandler(this);
        fab_addplaylist = findViewById(R.id.add_playlist_fab);

        createDefaultPlaylist();

        db = new PlaylistDatabase(this);


        //LastAddedLoader.makeLastAddedCursor(getApplicationContext());
        recyclerView = findViewById(R.id.play_list_list_recycler);
        try{
            list = db.getAllplaylist();
            list.get(0).setCover_albumId(LastAddedLoader.getLastAddedSongs(this).get(0).getAlbumId());
        }catch(Exception e){
            Log.v(getLocalClassName(),e.getMessage());
        }


//        Toast.makeText(this, ""+list.get(0).getPlaylistName(), Toast.LENGTH_SHORT).show();
        adapter = new PlayListListAdapter(list);


        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        fab_addplaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new MaterialDialog.Builder(PlayListList.this)
                        .title("Add Playlist")
                        .content("Adding new playlist")
                        .iconRes(R.mipmap.ic_launcher)
                        .theme(Theme.DARK)
                        .input("Enter name", null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                new PlaylistDatabase(PlayListList.this).addPlaylist(input.toString());
                                adapter.refresh(new PlaylistDatabase(PlayListList.this).getAllplaylist());

                            }
                        }).show();
            }
        });


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
