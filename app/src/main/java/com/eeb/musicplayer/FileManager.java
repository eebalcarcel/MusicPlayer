package com.eeb.musicplayer;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
class FileManager {

    final static String FOLDER = "Music";
    final static String PINNED_TRACKS_FILE_NAME = "pinned_tracks";
    final static String PINNED_TRACKS_FILE_EXTENSION = ".json";
    final String pinnedTracksPath;
    final Context context;
    static File directory;
    static String path;


    public FileManager(Context context) {
        this.context = context;
        pinnedTracksPath = context.getCacheDir() + File.separator + PINNED_TRACKS_FILE_NAME + PINNED_TRACKS_FILE_EXTENSION;
    }

    /**
     * @return Array of File from the {@value FileManager#FOLDER} folder
     */
    static File[] getMusicFiles() {
        path = Environment.getExternalStorageDirectory() + File.separator + FOLDER;
        directory = new File(path);
        return directory.listFiles();
    }

    /**
     * @return ArrayList of Track populated with {@link FileManager#getMusicFiles()}
     */
    ArrayList<Track> getTracksFromFiles() {
        ArrayList<Track> tracks = null;
        File[] musicFiles = getMusicFiles();
        if (musicFiles != null) {
            File[] files = musicFiles;
            tracks = new ArrayList<>();
            ArrayList<Track> pinnedTracks = getPinnedTracks();

            for (File file : files) {
                String filePath = file.getPath();
                try {
                    String fileName = URLEncoder.encode(filePath, "UTF-8");
                    if (URLConnection.guessContentTypeFromName(fileName).startsWith("audio")) {
                        Track track = new Track(filePath);
                        if ((pinnedTracks != null && !pinnedTracks.isEmpty()) && pinnedTracks.contains(track)) {
                            track.setPinned(true);
                        }
                        tracks.add(track);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        }
        return tracks;
    }


    void storePinnedTracks(ArrayList<Track> tracks) {
        try (FileWriter fileWriter = new FileWriter(pinnedTracksPath)) {
            ArrayList<Track> pinnedTracks = new ArrayList<>();
            for (Track track : tracks) {
                if (track.isPinned()) {
                    pinnedTracks.add(track);
                }
            }
            fileWriter.write(new Gson().toJson(pinnedTracks));
        } catch (IOException e) {
            Toast.makeText(context, "Error storing pinned tracks to cache", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    ArrayList<Track> getPinnedTracks() {
        ArrayList<Track> pinnedTracks = null;
        if((new File(pinnedTracksPath)).isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(pinnedTracksPath))) {
                pinnedTracks = (new Gson()).fromJson(reader, new TypeToken<List<Track>>() {
                }.getType());

            } catch (IOException e) {
                Toast.makeText(context, "Error getting pinned tracks from cache", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        return pinnedTracks;
    }


}
