package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 14-11-2017.
 */

public class RecentlyAdded extends RecyclerView.Adapter<RecentlyAdded.RecentlyAddedViewHolder>{
    private Context context;
    private ArrayList<Song> list = new ArrayList<>();

    public RecentlyAdded(ArrayList<Song> list) {
        this.list = list;
    }

    @Override
    public RecentlyAddedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_list_item,parent,false);
        context = view.getContext();
        return new RecentlyAddedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentlyAddedViewHolder holder, int position) {
        holder.song_name.setText(list.get(position).getName());
        holder.artist_name.setText(list.get(position).getArtist());
        String path = RandomUtils.getAlbumArt(context,list.get(position).getAlbumId());

        if(RandomUtils.isPathValid(path)){
            Picasso.with(context).load(new File(path))
                    .placeholder(R.drawable.default_art)
                    .into(holder.album_art);
        }
        else{
            Picasso.with(context)
                    .load(R.drawable.dispacito_64)
                    .into(holder.album_art);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecentlyAddedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView song_name,artist_name;
        ImageView album_art;

        public RecentlyAddedViewHolder(View itemView) {
            super(itemView);
            song_name = itemView.findViewById(R.id.song_item_name);
            album_art = itemView.findViewById(R.id.song_item_img);
            artist_name = itemView.findViewById(R.id.song_item_artist);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                MyApp.getMyService().setSongList(list);
                MyApp.getMyService().setSongPos(getAdapterPosition());
                MyApp.getMyService().playAll();
                //((PlayList)context).overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

            }catch (Exception e){
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
