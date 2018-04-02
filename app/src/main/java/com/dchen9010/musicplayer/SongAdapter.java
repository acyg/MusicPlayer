package com.dchen9010.musicplayer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<SongMeta> songs;
    private LayoutInflater songInf;
    private int selectedPos = -1;

    public SongAdapter(Context context, ArrayList<SongMeta> songs) {
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

    public void setSelected(int pos) {
        selectedPos = pos;
    }

    public int getSelected() {
        return selectedPos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LinearLayout songItem = (LinearLayout) songInf.inflate(R.layout.song, parent, false);

        TextView songTitle = (TextView) songItem.findViewById(R.id.song_title);
        TextView songArtist = (TextView) songItem.findViewById(R.id.song_artist);
        TextView songAlbum = (TextView) songItem.findViewById(R.id.song_album);

        SongMeta song = songs.get(pos);
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songAlbum.setText(song.getAlbum());

        songItem.setTag(pos);
        if(selectedPos == pos) songItem.setBackgroundColor(Color.DKGRAY);

        return songItem;
    }
}
