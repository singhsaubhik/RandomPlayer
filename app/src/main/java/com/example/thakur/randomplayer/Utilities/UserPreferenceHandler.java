package com.example.thakur.randomplayer.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by architjn on 11/12/15.
 */
public class UserPreferenceHandler {

    private static final String PREF_NAME = "com.thakur";
    private static final String REPEAT_ALL = "repeat_all";
    private static final String REPEAT_ONE = "repeat_one";
    private static final String SHUFFLE = "shuffle";
    private static final String isFirstRun = "isfirstrun";
    private static final String playingAlbum = "Playingalbum";
    private static final String playingSongList = "Playingsonglist";
    private static final String playingSearchList = "Playingsearchlist";
    private static final String lastplayed = "lastplayedsong";
    private static final String lastplayedDur = "lastplayedsongDuration";

    private final boolean gapless = false;

    private static final String default_playlist_created = "defaultplaylistcreated";

    private final SharedPreferences shp;

    public UserPreferenceHandler(Context context) {
        shp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setRepeatAllEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ALL, enable).apply();
    }

    public void setRepeatOneEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ONE, enable).apply();
    }

    public boolean isRepeatEnabled() {
        if (isRepeatAllEnabled() || isRepeatOneEnabled())
            return true;
        return false;
    }

    public void setRepeatEnable() {
        if (isRepeatAllEnabled()) {
            setRepeatAllEnable(false);
            setRepeatOneEnable(true);
        } else if (isRepeatOneEnabled()) {
            setRepeatOneEnable(false);
        } else {
            setRepeatAllEnable(true);
        }
    }

    public boolean isRepeatAllEnabled() {
        return shp.getBoolean(REPEAT_ALL, false);
    }

    public boolean isRepeatOneEnabled() {
        return shp.getBoolean(REPEAT_ONE, false);
    }

    public void setShuffle() {
        if (isShuffleEnabled())
            setShuffleEnabled(false);
        else
            setShuffleEnabled(true);
    }

    public boolean isShuffleEnabled() {
        return shp.getBoolean(SHUFFLE, false);
    }

    public void setShuffleEnabled(boolean value) {
        shp.edit().putBoolean(SHUFFLE, value).apply();
    }

    public void setIsFirstRun(boolean value){
        shp.edit().putBoolean(isFirstRun,value).apply();
    }
    public boolean getIsFirstRun(){
       return shp.getBoolean(isFirstRun,true);
    }


    //Playing what portion
    public void setPlayingAlbum(boolean val){
        shp.edit().putBoolean(playingAlbum,val).apply();
    }
    public void setPlayingSongList(boolean val){
        shp.edit().putBoolean(playingSongList,val).apply();
    }
    public void setPlayingSearchList(boolean val){
        shp.edit().putBoolean(playingSearchList,val).apply();
    }
    public boolean getPlayingSonglist(){
      return shp.getBoolean(playingSongList,false);
    }
    public boolean getPlayingAlbum(){
        return shp.getBoolean(playingAlbum,false);
    }
    public boolean getPlayingSaerchList(){
        return shp.getBoolean(playingSearchList,false);
    }

    //Last played song
    public void setLastPlayed(long songId){
        shp.edit().putLong(lastplayed,songId).apply();
    }
    public void setLastplayedDur(long duration){shp.edit().putLong(lastplayedDur,duration).apply();}

    public long getLastPlayed(){
        return shp.getLong(lastplayed,0);
    }
    public long getLastPlayedDur(){return shp.getLong(lastplayedDur,0);}

    //DefaultPlaylistCreated
    public void setDefaultPlaylistCreated(boolean val){
        shp.edit().putBoolean(default_playlist_created,val).apply();
    }

    public boolean getDefaultPlaylistCreated(){
        return shp.getBoolean(default_playlist_created,false);
    }

    public boolean gaplessPlayback() {
        return gapless;
    }
}
