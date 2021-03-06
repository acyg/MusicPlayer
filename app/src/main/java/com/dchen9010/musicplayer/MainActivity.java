package com.dchen9010.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaController.MediaPlayerControl {
    private ArrayList<SongMeta> songList;
    private RecyclerView songView;
    private MusicService musicSvc;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private final static int MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 0;

    private ServiceConnection musicConnection = new ServiceConnection() {

        /*
        Serveice Connection for connecting to music player service.
        It sets the playlist for the service.
        sets the controller widget for the activity.
        sets or appends to onPreparedListener for mediaPlayer in the service.
        */

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicSvc = ((MusicService.MusicBinder) service).getService();
            musicSvc.setList(songList);
            setController();

            musicSvc.getPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try{
                        musicSvc.onPrepared(mp);
                    } catch(Exception e) {
                        Log.i("MusicConnection", "Service does not implement onPreparedListener");
                    }

                    controller.show();
                    selectSongView(musicSvc.getPlaying());
                }
            });

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        songView = (RecyclerView) findViewById(R.id.song_list);
        songList = new ArrayList<SongMeta>();

        /*
        Request to permission to load from external sd card.
        Add songs from external sd card if already granted.
        Prepare code in onRequestPermissionResult.
        */
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else addToSongList(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        addToSongList(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);

        Collections.sort(songList, new Comparator<SongMeta>() {
            public int compare(SongMeta a, SongMeta b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdpt = new SongAdapter(this, songList);

        songView.setLayoutManager(new LinearLayoutManager(this));
        songView.setAdapter(songAdpt);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Bind to/Create music player service.

        if(playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(musicConnection);
        musicSvc = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //External sd card access granted, add songs from sd card to songList.

            addToSongList(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

            Collections.sort(songList, new Comparator<SongMeta>() {
                public int compare(SongMeta a, SongMeta b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });

            SongAdapter songApt = new SongAdapter(this, songList);
            songView.setAdapter(songApt);
        }
    }

    public void selectSongView(int pos) {
        SongAdapter adapter = (SongAdapter) songView.getAdapter();
        try{
            songView.getLayoutManager().findViewByPosition(adapter.getSelected()).setBackgroundColor(0);
        } catch(Exception e) {
            Log.d("Select Song View", "Previous view not exits or out of focus.");
        }
        adapter.setSelected(pos);
        try{
            songView.getLayoutManager().findViewByPosition(pos).setBackgroundColor(Color.DKGRAY);
        } catch(Exception e) {
            Log.d("Select Song View", "Selected view not exits or out of focus.");
        }

    }

    public void selectSong(View view) {
        int selectIndex = (int) view.getTag();
        musicSvc.setSong(selectIndex);
        musicSvc.playSong();
    }

    public void addToSongList(Uri targetUri) {
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(targetUri, null, null, null, null);

        if(musicCursor != null && musicCursor.moveToFirst()) {

            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            do {
                long Id = musicCursor.getLong(idColumn);
                int Duration = musicCursor.getInt(durationColumn);
                String Title = musicCursor.getString(titleColumn);
                String Artist = musicCursor.getString(artistColumn);
                String Album = musicCursor.getString(albumColumn);
                Uri contentUri = ContentUris.withAppendedId(targetUri, Id);
                songList.add(new SongMeta(
                        Id,
                        Duration,
                        Title,
                        Artist,
                        Album,
                        contentUri));
            } while(musicCursor.moveToNext());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_end:
                unbindService(musicConnection);
                //stopService(playIntent);
                musicSvc = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                break;
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicSvc.playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicSvc.playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView((View) findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    public void playPrev() {

        musicSvc.playPrev();
    }

    public void playNext() {

        musicSvc.playNext();
    }

    @Override
    public void start() {
        musicSvc.start();
    }

    @Override
    public void pause() {
        musicSvc.pause();
    }

    @Override
    public int getDuration() {
        //Log.d("dur", Integer.toString(songDur));
        return musicSvc.getDuration();//songDur;
    }

    @Override
    public int getCurrentPosition() {
        //Log.d("pos", Integer.toString(player.getCurrentPosition()));
        return musicSvc.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        musicSvc.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return musicSvc.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
