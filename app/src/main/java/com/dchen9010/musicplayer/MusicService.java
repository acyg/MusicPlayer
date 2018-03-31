package com.dchen9010.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songNum;
    private final IBinder musicBinder = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        songNum = 0;
        player = new MediaPlayer();

        initMediaPlayer();
    }

    public void initMediaPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void playSong() {
        player.reset();
        Song song = songs.get(songNum);

        try{
            player.setDataSource(getApplicationContext(), song.getContentUri());
        } catch(Exception e) {
            Log.e("MusicService", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer player) {

    }

    @Override
    public boolean onError(MediaPlayer player, int a, int b) {

        return false;
    }

    public void setSong(int songIndex) {
        songNum = songIndex;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

}
