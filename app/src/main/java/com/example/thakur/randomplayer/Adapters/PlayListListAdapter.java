package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.thakur.randomplayer.DatabaseHelper.PlaylistDatabase;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.PlayList;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.Constants;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Playlist;
import com.example.thakur.randomplayer.items.Song;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 15-11-2017
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
    public void onBindViewHolder(final PlayListListViewHolder holder, int position) {

        holder.title.setText(list.get(position).getPlaylistName());

        final String path = ListSongs.getAlbumArt(context,list.get(position).getCover_albumId());
        if(Utils.isPathValid(path)) {
            Glide.with(context).asBitmap().load(new File(path)).apply(new RequestOptions().fitCenter())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.imageView.setImageBitmap(resource);
                            Palette.Builder b = Palette.from(resource);
                            b.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int[] i = Utils.getAvailableColor(context,palette);
                                    holder.layout.setBackgroundColor(i[0]);
                                    holder.title.setTextColor(i[1]);
                                }
                            });
                        }
                    });
        }else {
            Glide.with(context).asBitmap().load(R.drawable.defualt_art2).apply(new RequestOptions().fitCenter())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                            holder.imageView.setImageBitmap(resource);
                            Palette.Builder b = Palette.from(resource);
                            b.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    int[] i = Utils.getAvailableColor(context,palette);
                                    holder.layout.setBackgroundColor(i[0]);
                                    holder.title.setTextColor(i[1]);
                                }
                            });
                        }
                    });
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class PlayListListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView,menu;
        TextView title;
        LinearLayout layout;
        public PlayListListViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.discrete_big_image);
            title = itemView.findViewById(R.id.playlist_list_name);
            menu = itemView.findViewById(R.id.playlist_list_overflow);
            layout = itemView.findViewById(R.id.playlist_list_linear_layout);

            itemView.setOnClickListener(this);
            menu.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v==itemView){
                switch (getAdapterPosition()){
                    case 0:
                        Intent in = new Intent(context,PlayList.class);
                        //in.putExtra("playlistposition",getAdapterPosition());
                        in.setAction("RecentAdded");
                        context.startActivity(in);
                        break;

                    case 1:
                        Intent in2 = new Intent(context,PlayList.class);
                        in2.setAction("RecentPlayed");
                        context.startActivity(in2);
                        break;

                    case 2:
                        Intent in3 = new Intent(context,PlayList.class);
                        in3.setAction("MostPlayed");
                        context.startActivity(in3);
                        break;

                    default:
                        Intent i = new Intent(context,PlayList.class);
                        i.putExtra(Constants.ARTIST_ID,list.get(getAdapterPosition()).getPlaylistId());
                        i.setAction("UserPlaylist");
                        context.startActivity(i);

                        break;


                }

            }
            else if(v==menu){
                PopupMenu pm = new PopupMenu(context,v);
                pm.inflate(R.menu.playlist_menu);
                pm.show();

                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()){

                            case R.id.playlist_menu_list_playall:
                                break;

                            case R.id.playlist_menu_list_shuffle:
                                break;

                            case R.id.playlist_menu_list_delete:

                                new MaterialDialog.Builder(context)

                                        .title("Delete Playlist : "+list.get(getAdapterPosition()).getPlaylistName())

                                        .content("Are you sure want to delete ?")

                                        .positiveText(R.string.delete_playlist1)

                                        .negativeText(R.string.cancel)

                                        .positiveColorRes(R.color.material_red_400)

                                        .negativeColorRes(R.color.material_red_400)

                                        .titleGravity(GravityEnum.CENTER)

                                        .titleColorRes(R.color.material_red_400)

                                        .contentColorRes(android.R.color.white)

                                        .backgroundColorRes(R.color.material_blue_grey_800)

                                        .dividerColorRes(R.color.accent)

                                        .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)

                                        .positiveColor(Color.WHITE)

                                        .negativeColorAttr(android.R.attr.textColorSecondaryInverse)

                                        .theme(Theme.DARK)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                //new PlaylistDatabase(context).deletePlaylist(list.get(getAdapterPosition()).getPlaylistId());
                                                //refresh(new PlaylistDatabase(context).getAllplaylist());
                                            }
                                        })

                                        .show();

                                break;

                            case R.id.playlist_menu_list_rename:

                                new MaterialDialog.Builder(context)
                                        .title("Rename Playlist")
                                        .content("Enter the new playlist name")
                                        .iconRes(R.mipmap.ic_launcher)
                                        .input("Enter name", null, false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                //new PlaylistDatabase(context).renamePlaylist(list.get(getAdapterPosition()).getPlaylistId(),input.toString());
                                                ///refresh(new PlaylistDatabase(context).getAllplaylist());
                                            }
                                        }).positiveText("Change")
                                        .negativeText("Cancel")
                                        .show();

                                break;


                        }
                        return true;
                    }
                });
            }
        }
    }

    public void refresh(ArrayList<Playlist> list){
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }



}
