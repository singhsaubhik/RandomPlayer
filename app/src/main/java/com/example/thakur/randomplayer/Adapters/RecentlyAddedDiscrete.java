package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 14-11-2017.
 */

public class RecentlyAddedDiscrete extends RecyclerView.Adapter<RecentlyAddedDiscrete.RecentlyAddedDiscreteViewHolder> {

    private Context context;
    private ArrayList<Song> list = new ArrayList<>();

    public RecentlyAddedDiscrete(ArrayList<Song> list) {
        this.list = list;
    }

    @Override
    public RecentlyAddedDiscreteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discreteview_layout_playlist, parent, false);
        context = view.getContext();
        return new RecentlyAddedDiscreteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentlyAddedDiscreteViewHolder holder, int position) {
        if (RandomUtils.isPathValid(RandomUtils.getAlbumArt(context, list.get(position).getAlbumId()))) {
            Glide.with(context).load(new File(RandomUtils.getAlbumArt(context, list.get(position).getAlbumId())))
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(holder.discrete_img);
        } else {
            Glide.with(context).load(R.drawable.discrete_default70dp)
                    .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                    .into(holder.discrete_img);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecentlyAddedDiscreteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView discrete_img;

        public RecentlyAddedDiscreteViewHolder(View itemView) {
            super(itemView);

            discrete_img = itemView.findViewById(R.id.discrete_image_playlist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, AlbumContentList.class);
            i.putExtra("albumId", list.get(getAdapterPosition()).getAlbumId());
            context.startActivity(i);


        }
    }
}
