package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Playlist;

import java.util.ArrayList;

/**
 * Created by Thakur on 26-12-2017.
 */

public class PlaylistList_simple extends RecyclerView.Adapter<PlaylistList_simple.PlaylistList_simple_ViewHolder> {

    private ArrayList<Playlist> list = new ArrayList<>();
    private Context context;

    public PlaylistList_simple(Context context, ArrayList<Playlist> list){
        this.list = list;
        this.context = context;
    }

    @Override
    public PlaylistList_simple_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_list_item,parent,false);
        return new PlaylistList_simple_ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaylistList_simple_ViewHolder holder, int position) {
        holder.title.setText(list.get(position).getPlaylistName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PlaylistList_simple_ViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        public PlaylistList_simple_ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playList_list_title);
        }
    }
}
