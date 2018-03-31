package com.dchen9010.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(context);
    }

    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LinearLayout songItem = (LinearLayout) songInf.inflate(R.layout.song, parent, false);

        TextView songTitle = (TextView) songItem.findViewById(R.id.song_title);
        TextView songArtist = (TextView) songItem.findViewById(R.id.song_artist);

        Song song = songs.get(pos);
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());

        songItem.setTag(pos);
        return songItem;
    }
}
