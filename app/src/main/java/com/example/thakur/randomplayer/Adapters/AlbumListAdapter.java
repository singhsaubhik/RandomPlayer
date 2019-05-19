package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.ItemClickListener;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Album;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 01-10-2017.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Album> albumList = new ArrayList<>();
    private Context context;

    public AlbumListAdapter(Context context, ArrayList<Album> arrayList) {
        this.context = context;
        albumList = arrayList;

    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        return getSong(position).substring(0, 1);
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemClickListener clickListener;
        TextView albumListName, artist;
        ImageView albumArt;
        LinearLayout layout;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.album_list_img);
            albumListName = itemView.findViewById(R.id.album_list_name);
            artist = itemView.findViewById(R.id.album_list_artist);
            layout = itemView.findViewById(R.id.album_list_bottom);

            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            setOnCLick(view, getAdapterPosition());
            //Toast.makeText(context, "Hi "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_grid_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        holder.albumListName.setText(albumList.get(position).getAlbumTitle());
        holder.artist.setText(albumList.get(position).getAlbumArtist());
        //setImage(holder,position);
        int size = dpToPx(300);

        //
        String path = albumList.get(position).getAlbumArtPath();
        //String path = RandomUtils.getAlbumArt(context,albumList.get(position).getAlbumId());


        if (RandomUtils.isPathValid(path)) {
            Glide.with(context).asBitmap().load(new File(path)).apply(new RequestOptions().override(size))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.albumArt.setImageBitmap(resource);

                            Palette.Builder b = Palette.from(resource);
                            b.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int[] colors = RandomUtils.getAvailableColor(context, palette);
                                    holder.layout.setBackgroundColor(colors[0]);
                                    holder.albumListName.setTextColor(colors[1]);
                                    holder.artist.setTextColor(colors[2]);
                                }
                            });
                        }
                    });
        } else {
            Glide.with(context).asBitmap().load(R.drawable.defualt_art2).apply(new RequestOptions().override(size))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.albumArt.setImageBitmap(resource);

                            Palette.Builder b = Palette.from(resource);
                            b.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int[] colors = RandomUtils.getAvailableColor(context, palette);
                                    holder.layout.setBackgroundColor(colors[0]);
                                    holder.albumListName.setTextColor(colors[1]);
                                    holder.artist.setTextColor(colors[2]);
                                }
                            });
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return albumList.size() > 0 ? albumList.size() : 0;
    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setOnCLick(View view, int position) {
        Log.v("AlbumListAdapter", "You clicked");
        Intent in = new Intent(context, AlbumContentList.class);
        in.putExtra("albumId", albumList.get(position).getAlbumId());
        context.startActivity(in);
        ((MainActivity) context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }


    private String getSong(int pos) {
        return albumList.get(pos).getAlbumTitle();
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }




    /*public void refresh(){
        this.albumList.clear();
        this.albumList.addAll(ListSongs.getAlbumList(context));
    }*/

}


