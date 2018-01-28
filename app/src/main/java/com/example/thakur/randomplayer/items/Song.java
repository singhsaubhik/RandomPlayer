package com.example.thakur.randomplayer.items;

/**
 * Created by architjn on 28/11/15.
 */
public class Song {

    public static final Song EMPTY_SONG = new Song(-1,-1,"","","","",-1);

    private long songId, albumId, dateAdded;
    private int artist_id;
    private String name;
    private String artist;
    private String path;
    private String albumName;
    private int track_number;
    private long date_modified;

    public int getTrack_number() {
        return track_number;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public Song(long songId, long albumId, long dateAdded, int artist_id, String name, String artist, String path, String albumName, int track_number, long date_modified, long duration) {

        this.songId = songId;
        this.albumId = albumId;
        this.dateAdded = dateAdded;
        this.artist_id = artist_id;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.albumName = albumName;
        this.track_number = track_number;
        this.date_modified = date_modified;
        this.duration = duration;
    }

    public Song(long songId, long albumId, long dateAdded, int artist_id, String name, String artist, String path, String albumName, int track_number, long duration) {

        this.songId = songId;
        this.albumId = albumId;
        this.dateAdded = dateAdded;
        this.artist_id = artist_id;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.albumName = albumName;
        this.track_number = track_number;
        this.duration = duration;
    }

    // private String durationString;
    private long duration;
    private boolean fav;


    public Song() {
        super();
    }

    public Song(long albumId, int artist_id, String name, String artist, String path, String albumName, long duration) {
        this.albumId = albumId;
        this.artist_id = artist_id;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.albumName = albumName;
        this.duration = duration;
    }



    public Song(long songId, long albumId, long dateAdded, String name, String artist, String albumName, long duration) {
        this.songId = songId;
        this.albumId = albumId;
        this.dateAdded = dateAdded;
        this.name = name;
        this.artist = artist;
        this.albumName = albumName;
        this.duration = duration;
    }

    public Song(long songId, String name, String artist,
                String path, boolean fav, long albumId,
                String albumName, long dateAdded, long duration) {
        this.songId = songId;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.fav = fav;
        this.dateAdded = dateAdded;
        this.albumId = albumId;
        this.albumName = albumName;
        this.duration = duration;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getSongId() {
        return songId;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getPath() {
        return path;
    }

    public boolean isFav() {
        return fav;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public long getDurationLong() {
        return duration;
    }

    public String getDuration() {
        try {
            Long time = duration;
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }

    public String getFormatedTime(long duration) {
        try {
            Long time = duration;
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);

            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }
}
