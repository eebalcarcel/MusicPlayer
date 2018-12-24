package com.eeb.musicplayer;

import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("unused")
class Track extends File{

    private String title;
    private final int duration;
    private final int id;

    public Track(int id, String pathname) {
        super(pathname);

        //Remove the filename extension
        int pos = this.getName().lastIndexOf(".");
        this.title = pos>0?this.getName().substring(0, pos):this.getName();

        //Gets the duration of the track
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this.getPath());
        this.duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();

        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    /**
     *
     * @return String with 'hh:mm:ss' or 'mm:ss' if it doesn't have hours
     */
    public static String getFormattedDuration(int milliseconds){
        String timeWithoutHours = String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        String time = String.format(Locale.US,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        return TimeUnit.MILLISECONDS.toHours(milliseconds)==0?timeWithoutHours:time;
    }
}
