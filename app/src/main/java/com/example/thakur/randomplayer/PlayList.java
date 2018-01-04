package com.example.thakur.randomplayer;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.thakur.randomplayer.Adapters.RecentlyAdded;
import com.example.thakur.randomplayer.Adapters.RecentlyAddedDiscrete;
import com.example.thakur.randomplayer.Loaders.LastAddedLoader;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class PlayList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecentlyAdded recentlyAdded;

    private RecyclerView discrete_recycler;
    private RecentlyAddedDiscrete discrete_adapter;
    private ArrayList<Song> list = new ArrayList<>();
    private ImageView header_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>19)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_secondary_library);

        Toolbar toolbar = findViewById(R.id.play_queue_title);
        setSupportActionBar(toolbar);

        header_image = findViewById(R.id.main_backdrop);
        recyclerView = findViewById(R.id.secondaryLibraryList);
        discrete_recycler = findViewById(R.id.discrete_recycler_recently_add);
        //LastAddedLoader.makeLastAddedCursor(getApplicationContext());

        list = (ArrayList<Song>) LastAddedLoader.getLastAddedSongs(getApplicationContext());

        recentlyAdded = new RecentlyAdded(list);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recentlyAdded);

        discrete_adapter = new RecentlyAddedDiscrete(list);
        LinearLayoutManager manager1 = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        discrete_recycler.setLayoutManager(manager1);
        discrete_recycler.setAdapter(discrete_adapter);


        setHeaderImage();



    }

    private void setHeaderImage(){
        String path = ListSongs.getAlbumArt(getApplicationContext(),list.get(0).getAlbumId());
        if(Utils.isPathValid(path)){
            Picasso.with(getApplicationContext())
                    .load(new File(path))
                    .placeholder(R.drawable.default_art)
                    .into(header_image);
        }
        else{
            Picasso.with(getApplicationContext())
                    .load(R.drawable.defualt_art2)
                    .placeholder(R.drawable.default_art)
                    .into(header_image);
        }
    }
}
