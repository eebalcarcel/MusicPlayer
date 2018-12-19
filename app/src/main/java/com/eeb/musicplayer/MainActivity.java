package com.eeb.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.nisrulz.sensey.Sensey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button nextButton, previousButton;
    private ToggleButton mediaButton;
    private TextView mediaStatus, currentSong;
    private Toolbar topBar, bottomBar;
    private RecyclerView rViewSongs;
    private SearchView searchBar;
    private SongAdapter songAdapter;
    private int currentSongIndex;
    private MediaController mc;
    private ConstraintLayout search_layout, content;
    private Window window;
    private ArrayList<Song> songs;
    int first = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        window = getWindow();

        //Makes the status bar transparent
        window.setStatusBarColor(0x00000000);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        //Populates songs with the Songs' JSON
        Intent intent = getIntent();
        String message = intent.getStringExtra("SplashActivity");
        songs = (new Gson()).fromJson(message, new TypeToken<List<Song>>(){}.getType());

        mediaButton = findViewById(R.id.mediaButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        mediaStatus = findViewById(R.id.mediaStatus);
        currentSong = findViewById(R.id.currentSong);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        rViewSongs = findViewById(R.id.rViewSongs);
        search_layout = findViewById(R.id.search);
        content = findViewById(R.id.content);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setIconifiedByDefault(false);

        rViewSongs.setHasFixedSize(true);
        rViewSongs.setLayoutManager(new LinearLayoutManager(this));

        //Adds status bar height to the topBar height
        topBar.post(new Runnable() {
            @Override
            public void run() {
                topBar.setLayoutParams(new Constraints.LayoutParams(topBar.getWidth(), topBar.getHeight() + getStatusBarHeight()));
            }
        });


        /*
          Handles touches gestures
          Shows searchBar when swipe down happens
          Hides searchBar when swipe up happens
         */
        topBar.setOnTouchListener(new OnSwipeListener(this) {
            @Override
            public void onSwipeDown() {
                openSearchBar();
            }

            @Override
            public void onSwipeUp() {
                closeSearchBar();
            }

        });

        mediaButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mc.isPlaying()) {
                    mc.start();
                } else {
                    mc.pause();
                }
            }
        });
        if (!songs.isEmpty()) {
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song s1, Song s2) {
                    return s1.getTitle().compareToIgnoreCase(s2.getTitle());
                }
            });

            mc = new MediaController(songs);
            mc.prepareTrack(0);
            songAdapter = new SongAdapter(this, songs);
            rViewSongs.setAdapter(songAdapter);

            //TODO: Remove line
            currentSong.setText(songs.get(0).getTitle());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }




    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private void closeSearchBar() {
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

        rViewSongs.setLayoutParams(new ConstraintLayout.LayoutParams(rViewSongs.getWidth(), rViewSongs.getHeight() + searchBar.getHeight()));
        rViewSongs.animate()
                .setDuration(100)
                .translationY(0);
    }

    private void openSearchBar() {
        searchBar.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        search_layout.animate()
                .setDuration(100)
                .translationY(searchBar.getHeight())
                .alpha(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.requestFocusFromTouch();
                    }
                });

        rViewSongs.setLayoutParams(new ConstraintLayout.LayoutParams(rViewSongs.getWidth(), rViewSongs.getHeight() - searchBar.getHeight()));
        rViewSongs.animate()
                .setDuration(100)
                .translationY(searchBar.getHeight());
    }
}
