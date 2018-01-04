package com.example.thakur.randomplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.thakur.randomplayer.Adapters.AlbumContentAdapter;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.items.Album;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class AlbumContentList extends AppCompatActivity {

    ListView lv;
    ArrayList<Song> album = new ArrayList<>();
    ArrayList<String> arr = new ArrayList<>();
    long albumId;
    RecyclerView recyclerView;
    AlbumContentAdapter adapter;
    ImageView album_art;
    LinearLayout layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.rv_album_activity);
        album_art = (ImageView) findViewById(R.id.activity_album_art);
        layout = (LinearLayout) findViewById(R.id.album_song_name_holder);



        albumId = getIntent().getLongExtra("albumId",0);
        album = ListSongs.getAlbumSongList(getApplicationContext(),albumId);
        adapter = new AlbumContentAdapter(getApplicationContext(),album);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setTitle(ListSongs.getAlbumFromId(getApplicationContext(),albumId).getAlbumTitle());
        String path = ListSongs.getAlbumArt(getApplicationContext(),albumId);

        try{
            Picasso.with(getApplicationContext())
                    .load(new File(path))
                    .fit()
                    .into(album_art);
        }catch (Exception e){

        }



    }
}
