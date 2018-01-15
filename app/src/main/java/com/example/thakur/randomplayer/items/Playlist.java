package com.example.thakur.randomplayer.items;


public class Playlist {

    private int playlistId;
    private String playlistName;
    private long cover_albumId;

    public Playlist(int playlistId, String playlistName, long cover_albumId) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.cover_albumId = cover_albumId;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public long getCover_albumId() {
        return cover_albumId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setCover_albumId(long cover_albumId) {
        this.cover_albumId = cover_albumId;
    }
}
