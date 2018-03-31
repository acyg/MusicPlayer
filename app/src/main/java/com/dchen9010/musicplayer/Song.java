package com.dchen9010.musicplayer;

import android.net.Uri;

public class Song {
    private long id;
    private String title;
    private String artist;
    private Uri contentUri;

    public Song(long songID, String songTitle, String songArtist, Uri uri) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        contentUri = uri;
    }

    public long getId() {
        return id;
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
}
