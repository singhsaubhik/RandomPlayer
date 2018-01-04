package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.ItemClickListener;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Album;
import com.example.thakur.randomplayer.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 01-10-2017.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Album> albumList = new ArrayList<>();
    private ArrayList<Album> filteredDataItems = new ArrayList<>();
    private ArrayList<Album> reference = new ArrayList<>();
    private Context context;

    public AlbumListAdapter(Context context, ArrayList<Album> arrayList)
    {
        this.context = context;
        albumList = arrayList;
        filteredDataItems.addAll(albumList);
    }



    @NonNull
    @Override
    public String getSectionName(int position) {
        return getSong(position).substring(0,1);
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        ItemClickListener clickListener;
        TextView albumListName,artist;
        ImageView albumArt;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.album_list_img);
            albumListName = itemView.findViewById(R.id.album_list_name);
            artist = itemView.findViewById(R.id.album_list_artist);

            itemView.setOnClickListener(this);

        }





        @Override
        public void onClick(View view) {
            setOnCLick(view,getAdapterPosition());
            //Toast.makeText(context, "Hi "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_grid_item,parent,false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        reference = filteredDataItems;
        holder.albumListName.setText(reference.get(position).getAlbumTitle());
        holder.artist.setText(reference.get(position).getAlbumArtist());
        //setImage(holder,position);
        int size = dpToPx(300);

        String path = reference.get(position).getAlbumArtPath();


        setArtistImg(holder,position,size);


    }

    @Override
    public int getItemCount() {
        return filteredDataItems.size();
    }

    public void setImage(AlbumViewHolder holder, int po)
    {






    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setOnCLick(View view , int position)
    {
        Log.v("AlbumListAdapter","You clicked");
        Intent in = new Intent(context, AlbumContentList.class);
        in.putExtra("albumId",filteredDataItems.get(position).getAlbumId());
        context.startActivity(in);
        ((MainActivity)context).overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

    }





    public void filter(String searchQuery){
        if(!searchQuery.equals("")){
            filteredDataItems.clear();
            for(Album d:albumList){
                if(d.getAlbumTitle().toLowerCase().contains(searchQuery)){
                    filteredDataItems.add(d);
                }
            }
        }else {
            filteredDataItems.clear();
            filteredDataItems.addAll(albumList);
        }
        notifyDataSetChanged();
    }

    private String getSong(int pos){
        return filteredDataItems.get(pos).getAlbumTitle();
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    private void setArtistImg(final AlbumViewHolder holder, final int position, final int size) {
        String path = filteredDataItems.get(position).getAlbumArtPath();
        if (isPathValid(path))
            Picasso.with(context).load(new File(path))
                    .centerCrop().resize(size, size).into(holder.albumArt);
        else
            holder.albumArt.setImageBitmap(new Utils(context)
                    .getBitmapOfVector(R.drawable.default_art, size, size));
        //handleRevealAnimation(holder, position);
        //handleColorAnimation(holder, path, size, position);
    }

}


