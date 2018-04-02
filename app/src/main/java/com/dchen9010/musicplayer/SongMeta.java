package com.dchen9010.musicplayer;

import android.net.Uri;

public class SongMeta {
    private long id;
    private int duration;
    private String title;
    private String artist;
    private String album;
    private Uri contentUri;

    public SongMeta(long songID, int songDur, String songTitle, String songArtist, String songAlbum, Uri uri) {
        id = songID;
        duration = songDur;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        contentUri = uri;
    }

    public long getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public String getAlbum() {
        return album;
    }
}

