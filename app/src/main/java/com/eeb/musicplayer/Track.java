package com.eeb.musicplayer;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


class Track extends File{

    private String title;
    private final int length;
    private final int index;

    public Track(String pathname, int index) {
        super(pathname);
        int pos = this.getName().lastIndexOf(".");
        this.title = pos>0?this.getName().substring(0, pos):this.getName();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this.getPath());
        this.length = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();

        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return String with 'hh:mm:ss' or 'mm:ss' if it doesn't have hours
     */
    public String getTime(){
        String timeWithoutHours = String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(length),
                TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length)));
        String time = String.format(Locale.US,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(length),
                TimeUnit.MILLISECONDS.toMinutes(length) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(length)),
                TimeUnit.MILLISECONDS.toSeconds(length) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length)));
        return TimeUnit.MILLISECONDS.toHours(length)==0?timeWithoutHours:time;
    }
}
