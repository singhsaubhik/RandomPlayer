package com.example.thakur.randomplayer.Fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.thakur.randomplayer.Adapters.AlbumListAdapter;
import com.example.thakur.randomplayer.Loaders.AlbumLoader;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.items.Album;



import java.util.ArrayList;

/**
 * Created by Thakur on 01-10-2017
 */

public class AlbumListFragment extends android.support.v4.app.Fragment{

    Context context;
    ArrayList<Album> albumList = new ArrayList<>();
    AlbumListAdapter albumListAdapter;
    RecyclerView mRecycler;
    //FastScroller fastScroller;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums,container,false);
        context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumList = AlbumLoader.getAlbumList(getActivity());
        albumListAdapter = new AlbumListAdapter(context,albumList);
        mRecycler = view.findViewById(R.id.albumsListContainer);
        //fastScroller = view.findViewById(R.id.album_scroller);

        GridLayoutManager manager = new GridLayoutManager(context,2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.scrollToPosition(0);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(albumListAdapter);
        mRecycler.setHasFixedSize(true);
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        //mRecycler.addItemDecoration(new ItemDecorationAlbumColumns(10,10));
        mRecycler.addItemDecoration(new SpacesItemDecoration(5));
        //fastScroller.setRecyclerView(mRecycler);
        //fastScroller.setBubbleColor(getResources().getColor(R.color.accent));


    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {


            outRect.left = space;
            outRect.top = space;
            outRect.right = space;
            outRect.bottom = space;

        }
    }



}
