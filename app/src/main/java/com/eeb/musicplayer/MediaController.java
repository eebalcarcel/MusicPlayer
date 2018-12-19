package com.eeb.musicplayer;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaController extends MediaPlayer {

    ArrayList<Song> songs;

    public MediaController(ArrayList<Song> songs) {
        super();
        this.songs = songs;
    }

    public void next(){
        //prepareTrack();
    }

    public void previous(){

    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }

    public void prepareTrack(int trackIndex) {
        try {
            this.setDataSource(songs.get(trackIndex).getPath());
            this.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
