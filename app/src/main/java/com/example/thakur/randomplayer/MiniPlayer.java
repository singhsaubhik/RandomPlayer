package com.example.thakur.randomplayer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MiniPlayer extends Fragment implements View.OnClickListener {

    private ArrayList<Song> list = new ArrayList<>();
    private Context context;

    private ImageView image, playpause;
    private TextView title;

    private MusicService mService;
    private boolean isBound = false;

    public MiniPlayer() {
        // Required empty public constructor
    }

    public static final String miniPlayerTrackChange = "miniPlayerTrachChange";
    public static final String miniplyerStatusChange = "miniplyerPlaypuaseStatusChange";
    private static final String trackchange = "com.example.thakur.randomplayer.Services.trackchangelistener";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(trackchange);
        filter.addAction(miniPlayerTrackChange);
        filter.addAction(miniplyerStatusChange);

        try {
            getActivity().registerReceiver(mReciever, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        list = SongLoader.getSongList(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        context = view.getContext();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        image = view.findViewById(R.id.second_player_screen_image);
        title = view.findViewById(R.id.second_player_screen_title);
        playpause = view.findViewById(R.id.second_player_screen_playpause);
        playpause.setOnClickListener(this);

        try {
            mService = MyApp.getMyService();
        } catch (Exception e) {

        }

       if(mService!=null) {
           updateUI(mService.getSongPos(),mService.isReallyPlaying());
       }else{
           updateUI(0,false);
       }



    }


    private void handlePlayPause() {
        try {
            Intent i = new Intent();
            i.setAction(MusicService.ACTION_PAUSE_SONG);

            context.sendBroadcast(i);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateUI(MyApp.getMyService().getSongPos(), MyApp.getMyService().isReallyPlaying());
                }
            }, 100);


        } catch (Exception e) {

        }
    }


    private void updateUI(int pos, boolean isPalying) {
        String path = RandomUtils.getAlbumArt(context, list.get(pos).getAlbumId());
        try {
            Glide.with(this).load(new File(path))
                    .into(image);
            title.setText("" + list.get(pos).getName());
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        updatePlayPause(isPalying);


    }

    private void updatePlayPause(boolean isPlaying) {
        if (isPlaying) {
            playpause.setImageResource(R.drawable.ic_pause_white_24dp);

        } else {
            playpause.setImageResource(R.drawable.ic_play_arrow_white2_24dp);

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mReciever);

    }

    @Override
    public void onClick(View view) {
        handlePlayPause();

    }

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case trackchange:
                    updateUI(MyApp.getMyService().getSongPos(), MyApp.getMyService().isReallyPlaying());
                    break;

                case miniPlayerTrackChange:
                    updateUI(MyApp.getMyService().getSongPos(), MyApp.getMyService().isReallyPlaying());
                    break;

                case miniplyerStatusChange:
                    updatePlayPause(MyApp.getMyService().isReallyPlaying());
                    break;


            }
        }
    };
}
