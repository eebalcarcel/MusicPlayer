package com.eeb.musicplayer;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class Song extends File{

    private String title;
    private int length;

    public Song(String pathname) {
        super(pathname);
        int pos = this.getName().lastIndexOf(".");
        this.title = pos>0?this.getName().substring(0, pos):this.getName();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this.getPath());
        this.length = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    /**
     *
     * @return String with 'hh:mm:ss' or 'mm:ss' if it doesn't have hours
     */
    public String getTime(){
        String timeWithoutHours = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(length),
                TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length)));
        String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(length),
                TimeUnit.MILLISECONDS.toMinutes(length) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(length)),
                TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length)));
        return TimeUnit.MILLISECONDS.toHours(length)==0?timeWithoutHours:time;
    }
}
