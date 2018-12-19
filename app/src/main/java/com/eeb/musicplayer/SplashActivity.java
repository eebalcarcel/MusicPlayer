package com.eeb.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;
    private static final int SPLASH_TIME = 250;
    private String songsJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ActivityCompat.requestPermissions(SplashActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);

        getWindow().setStatusBarColor(0x00000000);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("SplashActivity", songsJson);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);
    }



    //Populates songsJson with the files inside the Music folder
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String path = Environment.getExternalStorageDirectory() + "/Music";
                    File directory = new File(path);
                    List<File> files = Arrays.asList(directory.listFiles());
                    if (files != null) {
                        ArrayList<Song> songs = new ArrayList<>();

                        for (int i = 0; i < files.size(); i++){
                            String file = files.get(i).getPath();
                            try {
                                String fileName = URLEncoder.encode(file, "UTF-8");
                                if (URLConnection.guessContentTypeFromName(fileName).startsWith("audio")) {
                                    songs.add(new Song(file, i));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!songs.isEmpty()) {
                            Collections.sort(songs, new Comparator<Song>() {
                                @Override
                                public int compare(Song s1, Song s2) {
                                    return s1.getTitle().compareToIgnoreCase(s2.getTitle());
                                }
                            });

                            Gson gson = new Gson();
                            songsJson = gson.toJson(songs);
                        }
                    }
                } else {
                    finishAndRemoveTask();
                }
            }
        }
    }


}
