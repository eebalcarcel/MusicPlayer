package com.eeb.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST = 1;
    private Button nextButton, previousButton;
    private ToggleButton mediaButton;
    private TextView mediaStatus, song;
    private Toolbar topBar, bottomBar;
    private RecyclerView rViewSongs;
    private SearchView searchBar;
    private List<Song> songs;
    private SongAdapter songAdapter;
    private int currentSongIndex;
    private MediaPlayer mp;
    private ConstraintLayout search_layout;
    private Window window;
    private int songTimePostion;
    int first = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        window = getWindow();

        //Makes the status bar transparent
        window.setStatusBarColor(0x00000000);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST);

        songTimePostion = 0;
        mediaButton = findViewById(R.id.mediaButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        mediaStatus = findViewById(R.id.mediaStatus);
        song = findViewById(R.id.song);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        rViewSongs = findViewById(R.id.rViewSongs);
        search_layout = findViewById(R.id.search);
        searchBar = findViewById(R.id.searchBar);


        rViewSongs.setHasFixedSize(true);
        rViewSongs.setLayoutManager(new LinearLayoutManager(this));

        //Adds status bar height to the topBar height
        topBar.post(new Runnable() {
            @Override
            public void run() {
                topBar.setLayoutParams(new Constraints.LayoutParams(topBar.getWidth(), topBar.getHeight() + getStatusBarHeight()));
            }
        });

        //Moves searchBar down so slide down animation works flawlessly the first time
        searchBar.post(new Runnable() {
            @Override
            public void run() {
                search_layout.setTranslationY(searchBar.getHeight());
            }
        });

        //Hides searchBar when it loses focus
        searchBar.setIconifiedByDefault(false);
        searchBar.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    search_layout.animate()
                            .setDuration(250)
                            .translationY(-searchBar.getHeight())
                            .alpha(0f)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    searchBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

        /**
         * Handles touches gestures
         * Shows searchBar when swipe down happens
         * Hides searchBar when swipe up happens
         */
        TouchTypeDetector.TouchTypListener touchTypListener = new TouchTypeDetector.TouchTypListener() {
            @Override
            public void onSwipe(int swipeDirection) {
                switch (swipeDirection) {
                    case TouchTypeDetector.SWIPE_DIR_UP:
                        search_layout.animate()
                                .setDuration(250)
                                .translationY(searchBar.getHeight())
                                .alpha(0f)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        searchBar.setVisibility(View.GONE);
                                    }
                                });
                        break;
                    case TouchTypeDetector.SWIPE_DIR_DOWN:
                        searchBar.setVisibility(View.VISIBLE);
                        search_layout.animate()
                                .setDuration(100)
                                .translationY(topBar.getHeight())
                                .alpha(1f)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        searchBar.requestFocusFromTouch();
                                    }
                                });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                }
            }

            @Override
            public void onTwoFingerSingleTap() {
            }

            @Override
            public void onThreeFingerSingleTap() {
            }

            @Override
            public void onDoubleTap() {
            }

            @Override
            public void onScroll(int scrollDirection) {
            }

            @Override
            public void onSingleTap() {
            }

            @Override
            public void onLongPress() {
            }
        };
        Sensey.getInstance().init(getBaseContext());
        Sensey.getInstance().startTouchTypeDetection(getBaseContext(), touchTypListener);


        mp = new MediaPlayer();
        mediaButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mp.isPlaying()) {
                    try {
                        if (first == 0) {
                            //currentSongIndex = files.get(0;
                            //TODO: Get last song played or song selected
                            mp.setDataSource(songs.get(0).getPath());
                            mp.prepare();
                            first++;
                        }
                        mp.seekTo(songTimePostion);
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    songTimePostion = mp.getCurrentPosition();
                    mp.pause();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }


    /**
     *
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String path = Environment.getExternalStorageDirectory() + "/Music";
                    File directory = new File(path);
                    List<File> files = Arrays.asList(directory.listFiles());
                    if (files != null) {
                        songs = new ArrayList<>();

                        for (File file : files) {
                            songs.add(new Song(file.getPath()));
                        }

                        Collections.sort(songs, new Comparator<Song>() {
                            @Override
                            public int compare(Song s1, Song s2) {
                                return s1.getTitle().compareToIgnoreCase(s2.getTitle());
                            }
                        });

                        //TODO: Remove line
                        song.setText(songs.get(0).getTitle());

                        songAdapter = new SongAdapter(this, songs);
                        rViewSongs.setAdapter(songAdapter);
                    }
                } else {
                    finishAndRemoveTask();
                }
                return;
            }
        }
    }


    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId>0?getResources().getDimensionPixelSize(resourceId):0;
    }
}
