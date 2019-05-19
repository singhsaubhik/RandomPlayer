package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;

/**
 * Created by Thakur on 26-10-2017.
 */

public class AlbumContentAdapter extends RecyclerView.Adapter<AlbumContentAdapter.MyAlbumViewHolder> {
    private Context context;
    private ArrayList<Song> albumList = new ArrayList<>();

    public AlbumContentAdapter(Context context, ArrayList<Song> a) {
        this.context = context;
        this.albumList = a;
    }


    @Override
    public MyAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_content_list, parent, false);
        return new MyAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAlbumViewHolder holder, int position) {
        holder.tv.setText(albumList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return (albumList.size() > 0 ? albumList.size() : -1);
    }

    public class MyAlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv;

        public MyAlbumViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.album_song_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            handleOnClick(v, getAdapterPosition());
        }

    }

    private void handleOnClick(View v, int pos) {
        UserPreferenceHandler pref = new UserPreferenceHandler(context);
        MyApp.getMyService().setAblbumId(albumList.get(pos).getAlbumId());
        MyApp.getMyService().setSongPos(pos);
        MyApp.getMyService().playAlbum();
    }
}
