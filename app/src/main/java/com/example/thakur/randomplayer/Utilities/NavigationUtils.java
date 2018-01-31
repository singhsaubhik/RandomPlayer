package com.example.thakur.randomplayer.Utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v4.util.Pair;

import com.example.thakur.randomplayer.MainActivity;
import com.example.thakur.randomplayer.PlayList;

import java.util.ArrayList;

/**
 * Created by Thakur on 30-01-2018
 */

public class NavigationUtils {
    @TargetApi(21)
    public static void navigateToPlaylistDetail(Activity context, String action, long firstAlbumID, String playlistName, int foregroundcolor, long playlistID) {
        final Intent intent = new Intent(context, PlayList.class);
        if (!PreferencesUtility.getInstance(context).getSystemAnimations()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        intent.setAction(action);
        intent.putExtra(Constants.PLAYLIST_ID, playlistID);

        intent.putExtra(Constants.ALBUM_ID, firstAlbumID);
        intent.putExtra(Constants.PLAYLIST_NAME, playlistName);



            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST);

    }
}
