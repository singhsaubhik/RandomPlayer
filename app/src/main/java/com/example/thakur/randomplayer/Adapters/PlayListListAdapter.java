package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.PlayList;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Playlist;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 15-11-2017.
 */

public class PlayListListAdapter extends RecyclerView.Adapter<PlayListListAdapter.PlayListListViewHolder> {
    private ArrayList<Playlist> list = new ArrayList<>();
    private Context context;

    public PlayListListAdapter(ArrayList<Playlist> list) {
        this.list = list;
    }

    @Override
    public PlayListListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discreteview_layout_playlist_list,parent,false);
        context = view.getContext();
        return new PlayListListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlayListListViewHolder holder, int position) {

        holder.title.setText(list.get(position).getPlaylistName());

        String path = ListSongs.getAlbumArt(context,list.get(position).getCover_albumId());

        if(Utils.isPathValid(path)){
            Picasso.with(context)
                    .load(new File(path))
                    .into(holder.imageView);
        }else{
            Picasso.with(context)
                    .load(R.drawable.defualt_art2)
                    .into(holder.imageView);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PlayListListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView title;
        public PlayListListViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.discrete_big_image);
            title = itemView.findViewById(R.id.playlist_list_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (getAdapterPosition()){

                case 0:
                    context.startActivity(new Intent(context, PlayList.class));
                    break;
            }
        }
    }


}
