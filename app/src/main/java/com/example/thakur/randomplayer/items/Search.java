package com.example.thakur.randomplayer.items;

import java.util.ArrayList;

/**
 * Created by architjn on 19/12/15.
 */
public class Search {
    private final ArrayList<Song> songs;
    private final ArrayList<Album> albums;
    private final ArrayList<Artist> artists;

    public Search(ArrayList<Song> songs, ArrayList<Album> albums, ArrayList<Artist> artists) {
        this.songs = songs;
        this.albums = albums;
        this.artists = artists;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }
}
