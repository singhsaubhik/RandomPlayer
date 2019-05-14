package com.example.thakur.randomplayer.Loaders;

import android.content.Context;
import android.support.annotation.Nullable;

import com.example.thakur.randomplayer.items.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thakur on 19-02-2018
 */

public class QueueLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private Context context;
    public QueueLoader(Context context) {
        super(context);
        this.context = context;
    }


    @Nullable
    @Override
    public List<Song> loadInBackground() {
        ArrayList<Song> list = new ArrayList<>();


        return null;
    }
}
