package com.dchen9010.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
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
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener{
    private MediaPlayer player;
    private ArrayList<SongMeta> songs;
    private int songNum;
    private final IBinder musicBinder = new MusicBinder();
    private static final int NOTIFY_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        songNum = 0;
        player = new MediaPlayer();

        initMediaPlayer();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void initMediaPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
    }

    public void setList(ArrayList<SongMeta> songs) {
        this.songs = songs;
    }

    public void playSong() {
        SongMeta song = songs.get(songNum);
        player.reset();

        try{
            player.setDataSource(getApplicationContext(), song.getContentUri());
        } catch(Exception e) {
            Log.e("MusicService", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public int getPlaying() {
        return songNum;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer player, int a, int b) {
        player.reset();
        return false;
    }

    public void setSong(int songIndex) {
        songNum = songIndex;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent notiIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        String songTitle = songs.get(songNum).getTitle();
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification noti = builder.build();

        startForeground(NOTIFY_ID, noti);
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

    public void playPrev() {

        if(songNum == 0) songNum = songs.size() - 1;
        else songNum--;
        playSong();
    }

    public void playNext() {

        if(songNum < songs.size() - 1) songNum++;
        else songNum = 0;
        playSong();
    }

    public void start() {
        player.start();
    }

    public void pause() {
        player.pause();
    }

    public int getDuration() {
        //Log.d("dur", Integer.toString(songDur));
        return songs.get(songNum).getDuration();
    }

    public int getCurrentPosition() {
        //Log.d("pos", Integer.toString(player.getCurrentPosition()));
        return player.getCurrentPosition();
    }

    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
