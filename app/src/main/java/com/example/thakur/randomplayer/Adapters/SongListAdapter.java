package com.example.thakur.randomplayer.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.example.thakur.randomplayer.AlbumContentList;
import com.example.thakur.randomplayer.Dialogs.AddPlaylistDialog;
import com.example.thakur.randomplayer.ItemClickListener;
import com.example.thakur.randomplayer.Loaders.SongLoader;
import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.PlayerActivity;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.Utilities.RandomUtils;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;
import com.example.thakur.randomplayer.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by Thakur on 22-09-2017
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder> implements FastScrollRecyclerView.SectionedAdapter,PopupMenu.OnMenuItemClickListener{



    private AppCompatActivity context;
    PlayerActivity mplayerActivity = new PlayerActivity();

    private ArrayList<Song> songList = new ArrayList<>();
    private ArrayList<Song> reference = new ArrayList<>();
    private ArrayList<Song> filteredDataItems = new ArrayList<>();


    UserPreferenceHandler pref;

    public SongListAdapter(Context context ,ArrayList<Song> arrayList)
    {
        this.context= (AppCompatActivity) context;
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
                playerActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                try {
                    MyApp.getMyService().setSongList(filteredDataItems);
                    MyApp.getMyService().setSongPos(getAdapterPosition());
                    //
                    MyApp.getMyService().playAll();
                    //MusicPlayerRemote.openQueue(filteredDataItems,getAdapterPosition(),true);
                    //Toast.makeText(context, "Played by ! songlist", Toast.LENGTH_SHORT).show();
                    //MyApp.getMyService().saveQueue(filteredDataItems,getAdapterPosition(),true);


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
                                new MaterialDialog.Builder(context).iconRes(R.mipmap.ic_launcher)
                                        .theme(Theme.DARK)
                                        .title("Delete song")
                                        .content("Do you really want to delete : "+filteredDataItems.get(getAdapterPosition()).getName())
                                        .positiveText("Yes")
                                        .negativeText("No")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                long[] ids = {filteredDataItems.get(getAdapterPosition()).getSongId()};
                                                RandomUtils.deleteTracks(context,ids);
                                                refresh(SongLoader.getSongList(context));
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                            }
                                        }).show();

                                break;

                            case R.id.bittu_menu_setAsRintone:
                                RandomUtils.setAsRingtone(context,songList.get(getAdapterPosition()).getPath());
                                break;

                            case R.id.bittu_menu_shareTrack:
                                RandomUtils.shareSongFile(context,filteredDataItems,getAdapterPosition());
                                break;

                            case R.id.bittu_menu_showAlbum:
                                context.startActivity(new Intent(context,AlbumContentList.class).putExtra("albumId",filteredDataItems.get(getAdapterPosition()).getAlbumId()));
                                break;

                            case R.id.bittu_menu_showInfo:
                                RandomUtils.showSongDetailDialog(context,filteredDataItems,getAdapterPosition());
                                break;

                            case R.id.bittu_menu_addtoplaylist:
                                AddPlaylistDialog.newInstance(filteredDataItems.get(getAdapterPosition())).show(context.getSupportFragmentManager(), "ADD_PLAYLIST");
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

        String path = RandomUtils.getAlbumArt(context,
                reference.get(position).getAlbumId());

        int size = dpToPx(70);

        if(RandomUtils.isPathValid(path)) {
            Glide.with(context).load(new File(path))
                    .apply(new RequestOptions().override(size).format(DecodeFormat.PREFER_ARGB_8888))
                    .into(holder.img);
        }else{
            Glide.with(context).load(R.drawable.dispacito_64)
                    .apply(new RequestOptions().override(size))
                    .into(holder.img);
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




    public void refresh(ArrayList<Song> list){
        filteredDataItems.clear();
        filteredDataItems.addAll(list);
        notifyDataSetChanged();
    }




}
