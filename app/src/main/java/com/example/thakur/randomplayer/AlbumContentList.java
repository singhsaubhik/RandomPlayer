package com.example.thakur.randomplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.example.thakur.randomplayer.Adapters.AlbumContentAdapter;
import com.example.thakur.randomplayer.Loaders.AlbumLoader;
import com.example.thakur.randomplayer.Utilities.Helper;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;


import java.io.File;
import java.util.ArrayList;

public class AlbumContentList extends AppCompatActivity{


    ArrayList<Song> album = new ArrayList<>();
    long albumId;
    RecyclerView recyclerView;
    AlbumContentAdapter adapter;
    ImageView album_art;
    LinearLayout layout;

    int primariColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.rv_album_activity);
        album_art = (ImageView) findViewById(R.id.activity_album_art);
        ///layout = (LinearLayout) findViewById(R.id.album_song_name_holder);



        albumId = getIntent().getLongExtra("albumId",0);
        album = AlbumLoader.getAlbumSongList(getApplicationContext(),albumId);
        adapter = new AlbumContentAdapter(getApplicationContext(),album);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        primariColor = Config.primaryColor(this, Helper.getATEKey(this));

        setupToolbar();

        String path = RandomUtils.getAlbumArt(getApplicationContext(),albumId);

        try{
            Glide.with(this)
                    .load(new File(path))
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(album_art);
        }catch (Exception e){
            Glide.with(this)
                    .load(R.drawable.defualt_art2)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(album_art);
        }



    }

    private void setupToolbar(){
        try{

            getSupportActionBar().setTitle(AlbumLoader.getAlbumFromId(getApplicationContext(),albumId).getAlbumTitle());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
