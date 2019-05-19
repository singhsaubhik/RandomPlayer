package com.example.thakur.randomplayer;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thakur.randomplayer.Adapters.RecentlyAdded;
import com.example.thakur.randomplayer.Adapters.RecentlyAddedDiscrete;
import com.example.thakur.randomplayer.Loaders.LastAddedLoader;
import com.example.thakur.randomplayer.Loaders.PlaylistSongLoader;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.Loaders.TopTracksLoader;

import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Constants;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayList extends AppCompatActivity {

    String action;
    long playlistID;
    String path = null;
    HashMap<String, Runnable> playlistsMap = new HashMap<>();
    Runnable playlistLastAdded = new Runnable() {
        public void run() {

            new loadLastAdded().execute("");
        }
    };
    Runnable playlistRecents = new Runnable() {
        @Override
        public void run() {
            new loadRecentlyPlayed().execute("");

        }
    };
    Runnable playlistToptracks = new Runnable() {
        @Override
        public void run() {
            new loadTopTracks().execute("");
        }
    };
    Runnable playlistUsercreated = new Runnable() {
        @Override
        public void run() {
            new loadUserCreatedPlaylist().execute("");

        }
    };

    private RecyclerView recyclerView;
    private RecentlyAdded recentlyAdded;

    private RecyclerView discrete_recycler;
    private RecentlyAddedDiscrete discrete_adapter;
    private ArrayList<Song> list = new ArrayList<>();
    private ImageView header_image;
    private int startedBy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 19)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_secondary_library);

        Toolbar toolbar = findViewById(R.id.play_queue_title);
        setSupportActionBar(toolbar);

        action = getIntent().getAction();

        try {
            playlistsMap.put("RecentPlayed", playlistRecents);
            playlistsMap.put("RecentAdded", playlistLastAdded);
            playlistsMap.put("MostPlayed", playlistToptracks);
            playlistsMap.put("UserPlaylist", playlistUsercreated);
        } catch (Exception e) {
            e.printStackTrace();

        }


        header_image = findViewById(R.id.main_backdrop);
        recyclerView = findViewById(R.id.secondaryLibraryList);
        discrete_recycler = findViewById(R.id.discrete_recycler_recently_add);
        //LastAddedLoader.makeLastAddedCursor(getApplicationContext());

        //setList();


        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);


        LinearLayoutManager manager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        discrete_recycler.setLayoutManager(manager1);


        //setHeaderImage();

        try {
            setUpSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setHeaderImage() {

        if (RandomUtils.isPathValid(path)) {
            Picasso.with(getApplicationContext())
                    .load(new File(path))
                    .placeholder(R.drawable.default_art)
                    .into(header_image);
        } else {
            Picasso.with(getApplicationContext())
                    .load(R.drawable.defualt_art2)
                    .placeholder(R.drawable.default_art)
                    .into(header_image);
        }
    }


    private class loadRecentlyPlayed extends AsyncTask<String, Void, String> {

        List<Song> recentsongs;
        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(PlayList.this, TopTracksLoader.QueryType.RecentSongs);
            recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());

            if(recentsongs.size()<=0){
                return "Executed";
            }

            recentlyAdded = new RecentlyAdded((ArrayList<Song>) recentsongs);
            discrete_adapter = new RecentlyAddedDiscrete((ArrayList<Song>) recentsongs);
            path = RandomUtils.getAlbumArt(PlayList.this, recentsongs.get(0).getAlbumId());
            //mAdapter = new SongsListAdapter(mContext, recentsongs, true, animate);
            //mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (recentsongs.size() > 0) {
                setRecyclerViewAapter(false);
                setHeaderImage();
            } else {
                setRecyclerViewAapter(true);
            }

        }

        @Override
        protected void onPreExecute() {
            //showDialog();
        }
    }


    private class loadLastAdded extends AsyncTask<String, Void, String> {

        List<Song> lastadded;
        @Override
        protected String doInBackground(String... params) {
            lastadded = LastAddedLoader.getLastAddedSongs(PlayList.this);

            if(lastadded.size()<=0){
                return "Executed";
            }
            recentlyAdded = new RecentlyAdded((ArrayList<Song>) lastadded);
            discrete_adapter = new RecentlyAddedDiscrete((ArrayList<Song>) lastadded);
            path = RandomUtils.getAlbumArt(PlayList.this, lastadded.get(0).getAlbumId());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (lastadded.size() > 0) {
                setRecyclerViewAapter(false);
                setHeaderImage();
            } else {
                setRecyclerViewAapter(true);
            }
        }

        @Override
        protected void onPreExecute() {
            //showDialog();
        }
    }

    private class loadTopTracks extends AsyncTask<String, Void, String> {
        List<Song> toptracks;

        @Override
        protected String doInBackground(String... params) {
            TopTracksLoader loader = new TopTracksLoader(PlayList.this, TopTracksLoader.QueryType.TopTracks);
            toptracks = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());

            if(toptracks.size()>0) {
                recentlyAdded = new RecentlyAdded((ArrayList<Song>) toptracks);
            }else{
                return "Executed";
            }
            discrete_adapter = new RecentlyAddedDiscrete((ArrayList<Song>) toptracks);
            path = RandomUtils.getAlbumArt(PlayList.this, toptracks.get(0).getAlbumId());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (toptracks.size() > 0) {
                setRecyclerViewAapter(false);
                setHeaderImage();
            } else {
                setRecyclerViewAapter(true);
            }

        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class loadUserCreatedPlaylist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            playlistID = getIntent().getLongExtra(Constants.PLAYLIST_ID, 0);
            List<Song> playlistsongs = PlaylistSongLoader.getSongsInPlaylist(PlayList.this, playlistID);
            recentlyAdded = new RecentlyAdded((ArrayList<Song>) playlistsongs);
            discrete_adapter = new RecentlyAddedDiscrete((ArrayList<Song>) playlistsongs);
            try {
                path = RandomUtils.getAlbumArt(PlayList.this, playlistsongs.get(0).getAlbumId());
            } catch (Exception e) {
                path = "";
                //Toast.makeText(PlayList.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter(false);
            setHeaderImage();
        }

        @Override
        protected void onPreExecute() {
        }
    }


    private void setRecyclerViewAapter(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setAdapter(recentlyAdded);
            discrete_recycler.setAdapter(discrete_adapter);
        }
    }

    private void setUpSongs() {
        Runnable navigation = playlistsMap.get(action);
        if (navigation != null) {
            navigation.run();
        }
    }


}
