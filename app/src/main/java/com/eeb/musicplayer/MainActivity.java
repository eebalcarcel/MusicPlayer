package com.eeb.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.nisrulz.sensey.Sensey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TrackAdapter.RecyclerViewClickListener {
    private final static String MEDIA_STATUS_PLAYING = "Playing";
    private final static String MEDIA_STATUS_PAUSED = "Paused";
    private final static int UPDATE_SEEKBAR_TIME = 1000;
    private Button nextButton, previousButton;
    private ToggleButton mediaButton;
    private TextView mediaStatus, txtVcurrentTrack;
    private Toolbar topBar, bottomBar;
    private RecyclerView rViewTracks;
    private SearchView searchBar;
    private TrackAdapter trackAdapter;
    private MediaController mc;
    private ConstraintLayout search_layout, content;
    private SeekBar trackSeekBar;
    private Window window;
    private ArrayList<Track> tracks;


    public enum MEDIA_ACTION {
        START,
        PAUSE,
        NEXT,
        PREVIOUS
    }

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

        mediaButton = findViewById(R.id.mediaButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        mediaStatus = findViewById(R.id.mediaStatus);
        txtVcurrentTrack = findViewById(R.id.txtVcurrentTrack);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        rViewTracks = findViewById(R.id.rViewTracks);
        search_layout = findViewById(R.id.search);
        content = findViewById(R.id.content);
        trackSeekBar = findViewById(R.id.trackSeekBar);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setIconifiedByDefault(false);

        rViewTracks.setHasFixedSize(true);
        rViewTracks.setLayoutManager(new LinearLayoutManager(this));


        //Adds status bar height to the topBar height
        topBar.post(() -> topBar.setLayoutParams(new Constraints.LayoutParams(topBar.getWidth(), topBar.getHeight() + getStatusBarHeight())));


        //Populates tracks with the Tracks' JSON
        Intent intent = getIntent();
        String tracksJson = intent.getStringExtra("tracks");
        tracks = (new Gson()).fromJson(tracksJson, new TypeToken<List<Track>>() {
        }.getType());

        /*
          Handles searchBar's touches gestures
          Shows it when swipe down happens
          Hides it when swipe up happens
         */
        topBar.setOnTouchListener(new OnSwipeListener(this) {
            @Override
            void onSwipeDown() {
                openSearchBar();
            }

            @Override
            void onSwipeUp() {
                closeSearchBar();
            }

        });

        //Fast forwards the song to where the user moved the seek bar's progress
        trackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mc != null && fromUser){
                    mc.seekTo(progress);
                }
            }
        });

        if (tracks != null && !tracks.isEmpty()) {
            Collections.sort(tracks, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

            mc = new MediaController(tracks);
            trackAdapter = new TrackAdapter(this, tracks, this);
            rViewTracks.setAdapter(trackAdapter);

            mediaButton.setOnClickListener(v -> {
                if (!mc.isPlaying()) {
                    doMediaAction(MEDIA_ACTION.START, null);
                } else {
                    doMediaAction(MEDIA_ACTION.PAUSE, null);
                }
            });

            nextButton.setOnClickListener(v -> {
                doMediaAction(MEDIA_ACTION.NEXT, null);
            });

            previousButton.setOnClickListener(v -> {
                doMediaAction(MEDIA_ACTION.PREVIOUS, null);
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Track selectedTrack = tracks.get(position);
        selectedTrack.getTitle();
        doMediaAction(MEDIA_ACTION.START, selectedTrack);
    }


    /**
     * Performs a MediaController action
     *
     * @param action The action that is going to be performed
     * @param track  The track that is going to be played. Can be null
     */
    private void doMediaAction(@NonNull MEDIA_ACTION action, @Nullable Track track) {
        if (mc.getState() != MediaController.STATE.IDLE || mc.getState() != MediaController.STATE.STOPPED) {
            switch (action) {
                case START:
                    if (track == null) {
                        //TODO Add last played song before closing app
                        /*trackSeekBar.setMax();
                        mc.prepareTrack();*/
                        mc.start();
                    } else {
                        mc.prepareTrack(track);
                        trackSeekBar.setMax(track.getDuration());
                        txtVcurrentTrack.setText(track.getTitle());
                    }
                    mediaButton.setChecked(true);
                    mediaStatus.setText(MEDIA_STATUS_PLAYING);
                    break;
                case PAUSE:
                    mc.pause();
                    mediaButton.setChecked(false);
                    mediaStatus.setText(MEDIA_STATUS_PAUSED);
                    break;
                case NEXT:
                    mc.next();
                    txtVcurrentTrack.setText(mc.getCurrentTrack().getTitle());
                    mediaButton.setChecked(true);
                    mediaStatus.setText(MEDIA_STATUS_PLAYING);
                    break;
                case PREVIOUS:
                    mc.previous();
                    txtVcurrentTrack.setText(mc.getCurrentTrack().getTitle());
                    mediaButton.setChecked(true);
                    mediaStatus.setText(MEDIA_STATUS_PLAYING);
                    break;
            }
            startSeekBarUpdate();
        }
    }


    /**
     * Sets seek bar's max to the duration of the track and updates its progress every second
     */
    private void startSeekBarUpdate() {
        trackSeekBar.setMax(mc.getCurrentTrack().getDuration());
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackSeekBar.setProgress(mc.getCurrentPosition());
                new Handler().postDelayed(this, UPDATE_SEEKBAR_TIME);
            }
        });
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
                .withEndAction(() -> searchBar.setVisibility(View.GONE));

        rViewTracks.setLayoutParams(new ConstraintLayout.LayoutParams(rViewTracks.getWidth(), rViewTracks.getHeight() + searchBar.getHeight()));
        rViewTracks.animate()
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
                .withEndAction(() -> searchBar.requestFocusFromTouch());

        rViewTracks.setLayoutParams(new ConstraintLayout.LayoutParams(rViewTracks.getWidth(), rViewTracks.getHeight() - searchBar.getHeight()));
        rViewTracks.animate()
                .setDuration(100)
                .translationY(searchBar.getHeight());
    }


}
