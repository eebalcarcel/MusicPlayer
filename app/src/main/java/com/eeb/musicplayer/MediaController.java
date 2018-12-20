package com.eeb.musicplayer;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class MediaController extends MediaPlayer {

    private ArrayList<Track> tracks;


    public MediaController(ArrayList<Track> tracks) {
        super();
        this.tracks = tracks;
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
            this.setDataSource(tracks.get(trackIndex).getPath());
            this.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
