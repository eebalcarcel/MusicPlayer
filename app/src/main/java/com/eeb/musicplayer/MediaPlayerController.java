package com.eeb.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
class MediaPlayerController extends MediaPlayer {

    private static final int UPDATE_ELAPSED_TIME = 100;
    private Runnable updateTrackElapsedTime;
    private ArrayList<Track> tracks;
    private Track currentTrack;
    private STATE state;

    public enum STATE {
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
    }

    void next() {
        //Checks if the currentTrack isn't the last one, if so plays the first track
        if (!Objects.equals(currentTrack.getPath(), tracks.get(tracks.size() - 1).getPath())) {
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
        this.setTrack(track == null ? currentTrack : track);
        this.prepareAsync();
    }

    void setTrack(Track track) {
        try {
            reset();
            setDataSource(track.getPath());
            setState(STATE.INITIALIZED);
            currentTrack = track;
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    /**
     * @return The {@link Runnable} that sets the elapsed time to the track every {@value UPDATE_ELAPSED_TIME}ms
     */
    public Runnable getElapsedTimeToTrackRunnable() {
        Handler updateTrackElapsedTimeHandler = new Handler();
        if (currentTrack.getElapsedTime() > 0) {
            this.seekTo(currentTrack.getElapsedTime());
        }
        return updateTrackElapsedTime = () -> {
            currentTrack.setElapsedTime(getCurrentPosition());
            updateTrackElapsedTimeHandler.postDelayed(updateTrackElapsedTime, UPDATE_ELAPSED_TIME);
        };
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        setState(STATE.PREPARED);
        super.prepare();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        setState(STATE.PREPARED);
        super.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        setState(STATE.PLAYING);
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        setState(STATE.STOPPED);
        super.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        setState(STATE.PAUSED);
        super.pause();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        getCurrentTrack().setElapsedTime(msec);
        super.seekTo(msec);
    }
}
