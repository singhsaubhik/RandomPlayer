package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.Loaders.AlbumLoader;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Album;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by thakur on 11/11/17.
 */
public class ArtistSubListAdapter extends RecyclerView.Adapter<ArtistSubListAdapter.SimpleItemViewHolder> {

    private ArrayList<Album> items;
    private Context context;

    public ArtistSubListAdapter(Context context, long artistId) {
        this.context = context;
        this.items = AlbumLoader.getAlbumListOfArtist(context, artistId);
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.artist_sub_list_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        holder.name.setText(items.get(position).getAlbumTitle());
        setOnClickListeners(holder, position);
        setAlbumArt(position, holder);
    }

    private void setAlbumArt(final int position, final SimpleItemViewHolder holder) {
        String path = RandomUtils.getAlbumArt(context,
                items.get(position).getAlbumId());

        int size = (RandomUtils.getWindowWidth(context) - (2 * RandomUtils.dpToPx(context , 1))) / 2;
        if (path != null)
            Picasso.with(context).load(new File(path)).resize(size,
                    size).centerCrop().into(holder.img);
        else {
            holder.img.setImageBitmap(RandomUtils.getBitmapOfVector(context , R.drawable.default_art, size, size));
            holder.nameHolder.setBackgroundColor(ContextCompat.getColor(context, R.color.accent));
        }
    }

    private void setOnClickListeners(SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(context, AlbumContentList.class);
                        i.putExtra("albumId", items.get(position).getAlbumId());
                        i.putExtra("albumName", items.get(position).getAlbumTitle());
                        context.startActivity(i);
                        ((MainActivity)context).overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

                    }
                }).start();
            }
        });
    }

    public void updateList(ArrayList<Album> albums) {
        this.items = albums;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public View mainView, nameHolder;
        public ImageView img;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            nameHolder = itemView.findViewById(R.id.artist_sub_name_holder);
            img = (ImageView) itemView.findViewById(R.id.artist_sub_img);
            name = (TextView) itemView.findViewById(R.id.artist_sub_name);
        }
    }

}
