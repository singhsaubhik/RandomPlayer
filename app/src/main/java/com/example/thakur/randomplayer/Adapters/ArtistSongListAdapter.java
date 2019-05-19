package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Services.MusicService;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ArtistSongListAdapter extends RecyclerView.Adapter<ArtistSongListAdapter.SimpleItemViewHolder> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private View header;
    private ArrayList<Song> items;
    private Context context;

    public ArtistSongListAdapter(Context context, View header, ArrayList<Song> items) {
        this.context = context;
        this.header = header;
        this.items = items;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER)
            return new SimpleItemViewHolder(header);
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.songs_list_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        if (isHeader(position))
            return;
        holder.name.setText(items.get(position - 1).getName());
        holder.artistName.setText(items.get(position - 1).getArtist());
        setAlbumArt(position - 1, holder);
        setOnClicks(holder, position - 1);
    }

    private void setAlbumArt(int position, SimpleItemViewHolder holder) {
        String path = RandomUtils.getAlbumArt(context,
                items.get(position).getAlbumId());
        int size = RandomUtils.dpToPx(context , 50);
        if (path != null)
            Picasso.with(context).load(new File(path)).resize(size,
                    size).centerCrop().into(holder.img);
        else {
            holder.img.setImageBitmap(RandomUtils.getBitmapOfVector(context , R.drawable.default_art, size, size));
        }
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    public void run() {

                    }
                }).start();
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(context, view);
                pm.inflate(R.menu.popup_song);
                pm.show();
                final Intent intent = new Intent();
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                intent.setAction(MusicService.PLAY_SINGLE);
                                intent.putExtra("songId", items.get(position).getSongId());
                                context.sendBroadcast(intent);
                                break;
                            case R.id.popup_song_addtoplaylist:

                                break;
                        }
                        return false;
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size() > 0 ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position))
            return ITEM_VIEW_TYPE_HEADER;
        else
            return ITEM_VIEW_TYPE_ITEM;
    }

    private String getString(){
        return null;
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, artistName;
        public View mainView, menu;
        public ImageView img;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = itemView.findViewById(R.id.song_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);
        }
    }

}
