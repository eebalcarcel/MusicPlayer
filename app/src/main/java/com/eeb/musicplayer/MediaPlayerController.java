package com.eeb.musicplayer;

import android.media.MediaPlayer;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
class MediaPlayerController extends MediaPlayer {

    private final ArrayList<Track> tracks;
    private Track currentTrack;
    private STATE state;

    public enum STATE{
        IDLE,
        INITIALIZED,
        PREPARED,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public MediaPlayerController(ArrayList<Track> tracks) {
        super();
        this.tracks = tracks;
        setState(STATE.IDLE);
        setOnPreparedListener(MediaPlayer::start);
    }

    void next() {
        //Checks if the currentTrack isn't the last one, if so plays the first track
        if (currentTrack != tracks.get(tracks.size() - 1)) {
            currentTrack = tracks.get(tracks.indexOf(currentTrack) + 1);
        } else {
            currentTrack = tracks.get(0);
        }
        prepareTrack(null);
    }

    void previous() {
        //Checks if the index of the currentTrack isn't the first one, if so plays the last track
        if (tracks.indexOf(currentTrack) >= 1) {
            currentTrack = tracks.get(tracks.indexOf(currentTrack) - 1);
        } else {
            currentTrack = tracks.get(tracks.size() - 1);
        }
        prepareTrack(null);
    }

    void prepareTrack(@Nullable Track track) {
        try {
            this.setTrack(track==null?currentTrack:track);
            this.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setTrack(Track track) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        reset();
        setDataSource(track.getPath());
        setState(STATE.INITIALIZED);
        currentTrack = track;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }


    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
        setState(STATE.PREPARED);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
        setState(STATE.PREPARED);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        setState(STATE.PLAYING);
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        setState(STATE.STOPPED);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        setState(STATE.PAUSED);
    }



}
