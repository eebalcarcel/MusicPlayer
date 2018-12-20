package com.eeb.musicplayer;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

class MediaController extends MediaPlayer {

    private final ArrayList<Track> tracks;
    private int currentTrackIndex;

    public MediaController(ArrayList<Track> tracks) {
        super();
        this.tracks = tracks;

    }

    void next() {
        currentTrackIndex++;
        this.reset();
        prepareTrack(currentTrackIndex);
        this.start();
    }

    void previous() {
        currentTrackIndex--;
        this.reset();
        prepareTrack(currentTrackIndex);
        this.start();
    }

    void prepareTrack(int trackIndex) {
        try {
            currentTrackIndex = trackIndex;
            this.setDataSource(tracks.get(trackIndex).getPath());
            this.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        setOnPreparedListener(player -> player.start());
    }
}
