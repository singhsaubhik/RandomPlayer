package com.example.thakur.randomplayer.Adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.ItemClickListener;
import com.example.thakur.randomplayer.Loaders.ListSongs;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.PlayerActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.Utilities.Utils;
import com.example.thakur.randomplayer.items.Song;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 22-09-2017.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder> implements FastScrollRecyclerView.SectionedAdapter,PopupMenu.OnMenuItemClickListener{



    private Context context;
    PlayerActivity mplayerActivity = new PlayerActivity();

    ArrayList<Song> songList = new ArrayList<>();
    ArrayList<Song> reference = new ArrayList<>();
    ArrayList<Song> filteredDataItems = new ArrayList<>();


    UserPreferenceHandler pref;

    public SongListAdapter(Context context ,ArrayList<Song> arrayList)
    {
        this.context=context;
        this.songList = arrayList;

        filteredDataItems.addAll(songList);

        pref = new UserPreferenceHandler(context);

    }






    @NonNull
    @Override
    public String getSectionName(int position) {
        return getSong(position).substring(0,1);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener{

        private ItemClickListener itemClickListener;

        public View mainView;
        public TextView title , artist;
        public ImageView img,popUp;
        public MyViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;

            title = (TextView) itemView.findViewById(R.id.song_item_name);
            artist = (TextView) itemView.findViewById(R.id.song_item_artist);
            img = itemView.findViewById(R.id.song_item_img);

            popUp = itemView.findViewById(R.id.song_item_menu);
            itemView.setOnClickListener(this);
            popUp.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View view) {
            //itemClickListener.onClick(view,getAdapterPosition(),false);

            if(view == itemView){
                Intent playerActivity = new Intent(context,PlayerActivity.class);

                try {
                    MyApp.getMyService().setSongList(filteredDataItems);
                    MyApp.getMyService().setSongPos(getAdapterPosition());
                    MyApp.getMyService().playAll();
                    //Toast.makeText(context, "Played by ! songlist", Toast.LENGTH_SHORT).show();


                    //playerActivity.putExtra("pos",position);
                    context.startActivity(playerActivity);
                    ((MainActivity)context).overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

                }catch (Exception e){
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            else if(view==popUp){

                PopupMenu popupMenu = new PopupMenu(context,view);
                popupMenu.inflate(R.menu.bittu_menu);
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.bittu_menu_playOnly:
                                break;

                            case R.id.bittu_menu_deleteTrack:
                                long[] ids = {filteredDataItems.get(getAdapterPosition()).getSongId()};
                                Utils.deleteTracks(context,ids);
                                notifyDataSetChanged();
                                break;

                            case R.id.bittu_menu_setAsRintone:

                                Dexter.withActivity((Activity) context).withPermission(Manifest.permission.WRITE_SETTINGS)
                                        .withListener(new PermissionListener() {
                                            @Override
                                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                                Utils.setAsRingtone(context,filteredDataItems.get(getAdapterPosition()).getPath());

                                            }

                                            @Override
                                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                                Toast.makeText(context, "We need permission", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                                showPermissionRationale(token);
                                            }
                                        }).check();
                                break;

                            case R.id.bittu_menu_shareTrack:
                                Utils.shareSongFile(context,filteredDataItems,getAdapterPosition());
                                break;

                            case R.id.bittu_menu_showAlbum:
                                context.startActivity(new Intent(context,AlbumContentList.class).putExtra("albumId",filteredDataItems.get(getAdapterPosition()).getAlbumId()));
                                break;

                            case R.id.bittu_menu_showInfo:
                                Utils.showSongDetailDialog(context,filteredDataItems,getAdapterPosition());
                                break;
                        }
                        return true;
                    }
                });
            }


        }

        @Override
        public boolean onLongClick(View view) {

            //itemClickListener.onClick(view,getAdapterPosition(),true);

            return true;
        }
    }




    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_list_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        reference = filteredDataItems;
        holder.title.setText(reference.get(position).getName());
        holder.artist.setText(reference.get(position).getArtist());
        //setAlbumArt(position,holder);
        //setOnClicks(holder,position);

        String path = ListSongs.getAlbumArt(context,
                reference.get(position).getAlbumId());

        int size = dpToPx(70);
        if (path != null)
            Picasso.with(context).load(new File(path)).resize(size,
                    size).centerCrop().into(holder.img);
        else {
            Picasso.with(context).load(R.drawable.dispacito_64).resize(size,size).centerCrop().into(holder.img);
        }


    }


    @Override
    public int getItemCount() {
        return filteredDataItems.size();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }



    private String getSong(int pos){
        return filteredDataItems.get(pos).getName();
    }

    public void filter(String searchQuery){
        if(!searchQuery.equals("")){
            filteredDataItems.clear();
            for(Song d:songList){
                if(d.getName().toLowerCase().contains(searchQuery)){
                    filteredDataItems.add(d);
                }
            }
        }else {
            filteredDataItems.clear();
            filteredDataItems.addAll(songList);

        }
        notifyDataSetChanged();
    }



    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(context).setTitle("Permission required")
                .setMessage("To run all feature in this app we need some permissions please allow them.\nBelieve us there is no harm!!")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }


}
