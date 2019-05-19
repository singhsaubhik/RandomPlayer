package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 12-11-2017.
 */

public class DiscreteViewAdapter extends RecyclerView.Adapter<DiscreteViewAdapter.DiscreteViewHolder> {

    private Context context;
    private ArrayList<Song> list = new ArrayList<>();

    public DiscreteViewAdapter(ArrayList<Song> list) {
        this.list = list;
    }

    @Override
    public DiscreteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discreteview_layout, parent, false);
        context = view.getContext();
        return new DiscreteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiscreteViewHolder holder, int position) {
        String path = RandomUtils.getAlbumArt(context, list.get(position).getAlbumId());
        if (RandomUtils.isPathValid(path)) {
            Picasso.with(context).load(new File(path))
                    .into(holder.imageView);
        } else {
            Picasso.with(context).load(R.drawable.discrete_default70dp)
                    .into(holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return list.size() > 0 ? list.size() : 0;
    }

    public class DiscreteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;

        public DiscreteViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.discrete_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            Intent i = new Intent();
            i.setAction(MusicService.DISCRETE_VIEW_CLICK);
            i.putExtra("discreteView.pos", getAdapterPosition());
            context.sendBroadcast(i);
            context.sendBroadcast(new Intent(MusicService.PLAYING_STATUS_CHANGED));

        }
    }
}
