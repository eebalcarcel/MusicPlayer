package com.eeb.musicplayer;

import android.os.Environment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
class FileManager {

    final static String FOLDER = "Music";
    static File directory;
    static String path;

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
    static ArrayList<Track> getTracksFromFiles() {
        ArrayList<Track> tracks = null;
        File[] musicFiles = getMusicFiles();
        if (musicFiles != null) {
            List<File> files = Arrays.asList(musicFiles);
            tracks = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                String filePath = files.get(i).getPath();
                try {
                    String fileName = URLEncoder.encode(filePath, "UTF-8");
                    if (URLConnection.guessContentTypeFromName(fileName).startsWith("audio")) {
                        tracks.add(new Track(i, filePath));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return tracks;
    }


}
