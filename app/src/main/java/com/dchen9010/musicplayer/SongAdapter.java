package com.dchen9010.musicplayer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.mViewHolder> {
    private ArrayList<SongMeta> songs;
    private LayoutInflater songInf;
    private int selectedPos = -1;

    public SongAdapter(Context context, ArrayList<SongMeta> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(context);
    }

    public void setSelected(int pos) {
        selectedPos = pos;
    }

    public int getSelected() {
        return selectedPos;
    }

    public static class mViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public mViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    @NonNull
    @Override
    public SongAdapter.mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LinearLayout songItem = (LinearLayout) songInf.inflate(R.layout.song, parent, false);

        return new mViewHolder(songItem);
    }

    @Override
    public void onBindViewHolder(@NonNull mViewHolder holder, int position) {

        View songItem = holder.mView;


        TextView songTitle = (TextView) songItem.findViewById(R.id.song_title);
        TextView songArtist = (TextView) songItem.findViewById(R.id.song_artist);
        TextView songAlbum = (TextView) songItem.findViewById(R.id.song_album);

        SongMeta song = songs.get(position);
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());
        songAlbum.setText(song.getAlbum());

        songItem.setTag(position);
        if(selectedPos == position) songItem.setBackgroundColor(Color.DKGRAY);
        else songItem.setBackgroundColor(0);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
