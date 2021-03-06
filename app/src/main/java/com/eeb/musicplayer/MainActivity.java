package com.eeb.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;
    private final static int UPDATE_SEEKBAR_TIME = 100;
    private boolean tracksExist = true;
    private Button nextButton, previousButton;
    private ToggleButton mediaButton;
    private TextView mediaStatus, txtVcurrentTrack, mc_trackDuration, elapsedTime;
    private Toolbar topBar;
    private RecyclerView rViewTracks;
    private SearchView searchBar;
    private TrackAdapter trackAdapter;
    private MediaPlayerController mpc;
    private ConstraintLayout search_layout;
    private SeekBar trackSeekBar;
    private Window window;
    private ArrayList<Track> tracks, allTracks; //Tracks can change its elements. The elements of allTracks will always be the same
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private Runnable updateTrackProgressRunnable;
    private Handler updateTrackProgressHandler;
    private FileManager fileManager;
    private Track lastTrackPlayed;

    public enum MEDIA_ACTION {
        START,
        PAUSE,
        NEXT,
        PREVIOUS,
        STOP
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        window = getWindow();

        fileManager = new FileManager(this);
        lastTrackPlayed = fileManager.getLastTrackPlayed();

        //If granted gets tracks, initializes mcp and trackAdapter and sets trackAdapter to rViewTracks
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        afChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    doMediaAction(MEDIA_ACTION.PAUSE, null);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    doMediaAction(MEDIA_ACTION.START, null);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    doMediaAction(MEDIA_ACTION.STOP, null);
                    break;
            }
        };

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
        rViewTracks = findViewById(R.id.rViewTracks);
        search_layout = findViewById(R.id.search);
        trackSeekBar = findViewById(R.id.trackSeekBar);
        mc_trackDuration = findViewById(R.id.mc_trackDuration);
        elapsedTime = findViewById(R.id.mc_currentDuration);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setIconifiedByDefault(false);

        //Makes it move horizontally
        txtVcurrentTrack.setSelected(true);

        rViewTracks.setHasFixedSize(true);
        rViewTracks.setLayoutManager(new LinearLayoutManager(this));


        //Adds status bar height to the topBar height
        topBar.post(() -> topBar.setLayoutParams(new Constraints.LayoutParams(topBar.getWidth(), topBar.getHeight() + getStatusBarHeight())));

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
                if (tracks != null && mpc != null && mpc.getState() != MediaPlayerController.STATE.IDLE && mpc.getState() != MediaPlayerController.STATE.INITIALIZED && fromUser) {
                    mpc.seekTo(progress);
                    elapsedTime.setText(Track.getFormattedDuration(mpc.getCurrentPosition()));
                }
            }
        });

        mediaButton.setOnClickListener(v -> {
            if (!mpc.isPlaying()) {
                doMediaAction(MEDIA_ACTION.START, null);
            } else {
                doMediaAction(MEDIA_ACTION.PAUSE, null);
            }
        });

        nextButton.setOnClickListener(v -> doMediaAction(MEDIA_ACTION.NEXT, null));
        previousButton.setOnClickListener(v -> doMediaAction(MEDIA_ACTION.PREVIOUS, null));
        mpc = new MediaPlayerController(null);
        mpc.setOnCompletionListener(mp -> doMediaAction(MEDIA_ACTION.NEXT, null));
        mpc.setOnPreparedListener(mediaPlayer -> {
            if (mpc.getCurrentTrack().getElapsedTime() == 0) {
                mediaPlayer.start();
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (tracks != null) {
                    tracks.clear();
                    tracks.addAll(allTracks);

                    filterTracks(newText);

                    arrangeTracks(tracks);
                    trackAdapter.setTracks(tracks);
                    trackAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //Initializes mpc when it has been stopped by another app
            if (mpc != null && mpc.getState() == MediaPlayerController.STATE.STOPPED && checkTracks()) {
                mpc.setTracks(tracks);
            }
        }
    }

    @Override
    public void onStop() {
        fileManager.storePinnedTracks(tracks);
        fileManager.storeLastTrackPlayed(mpc.getCurrentTrack());
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkTracks()) {
                        mpc.setTracks(tracks);
                        trackAdapter = new TrackAdapter(this, tracks, (v, position) -> {
                            if (v.getId() == R.id.pin) {
                                Track trackClicked = tracks.get(position);
                                trackClicked.setPinned(!trackClicked.isPinned());
                                arrangeTracks(tracks);
                                trackAdapter.setTracks(tracks);
                                trackAdapter.notifyDataSetChanged();
                            } else {
                                doMediaAction(MEDIA_ACTION.START, tracks.get(position));
                            }
                        });
                        rViewTracks.setAdapter(trackAdapter);

                        prepareLastPlayedTrack(lastTrackPlayed);
                    }
                } else {
                    mediaButton.setEnabled(false);
                    nextButton.setEnabled(false);
                    previousButton.setEnabled(false);
                    mediaStatus.setText(getString(R.string.media_status_permissions_not_granted));
                    txtVcurrentTrack.setText(getString(R.string.current_track_permissions_not_granted));
                }
                break;
            }
        }
    }

    /**
     * Performs a MediaPlayerController action
     *
     * @param action The action that is going to be performed
     * @param track  The track that is going to be played. Can be null
     */
    private void doMediaAction(@NonNull MEDIA_ACTION action, @Nullable Track track) {
        if (checkTracks()) {
            if (mpc.getState() != MediaPlayerController.STATE.IDLE || mpc.getState() != MediaPlayerController.STATE.STOPPED) {
                switch (action) {
                    case START:
                        if (requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            if (track != null) {
                                mpc.prepareTrack(track);
                            } else {
                                //If there is no last track played, it plays a random one
                                if (lastTrackPlayed == null) {
                                    mpc.prepareTrack(tracks.get((int) (Math.random() * (tracks.size() - 1))));
                                } else {
                                    mpc.start();
                                }
                            }
                        }
                        break;

                    case PAUSE:
                        mpc.pause();
                        break;

                    case NEXT:
                        mpc.next();
                        break;

                    case PREVIOUS:
                        mpc.previous();
                        break;

                    case STOP:
                        updateTrackProgressHandler.removeCallbacks(updateTrackProgressRunnable);
                        mpc.reset();
                        mpc.release();
                        abandonAudioFocus();
                        break;
                }

                txtVcurrentTrack.setText(mpc.getCurrentTrack().getTitle());
                //Checks if the track isn't the last played and if the action isn't pause nor stop
                if ((track == null || track.getElapsedTime() == 0) && (action != MEDIA_ACTION.PAUSE && action != MEDIA_ACTION.STOP)) {
                    mediaButton.setChecked(true);
                    mediaStatus.setText(getString(R.string.media_status_playing));
                    setTrackProgressUpdater();
                } else {
                    mediaButton.setChecked(false);
                    mediaStatus.setText(getString(R.string.media_status_paused));
                }

            }
        }
    }

    /**
     * Sets the total duration of the track duration and updates the elapsed time
     * Sets the max progress of the seek bar to the duration of the track and updates its progress every second
     */
    private void setTrackProgressUpdater() {
        mc_trackDuration.setText(Track.getFormattedDuration(mpc.getCurrentTrack().getDuration()));
        trackSeekBar.setMax(mpc.getCurrentTrack().getDuration());

        updateTrackProgressHandler = new Handler();
        updateTrackProgressRunnable = () -> {
            trackSeekBar.setProgress(mpc.getCurrentPosition());
            elapsedTime.setText(Track.getFormattedDuration(mpc.getCurrentPosition()));
            updateTrackProgressHandler.postDelayed(updateTrackProgressRunnable, UPDATE_SEEKBAR_TIME);
        };

        MainActivity.this.runOnUiThread(mpc.getElapsedTimeToTrackRunnable());
        MainActivity.this.runOnUiThread(updateTrackProgressRunnable);
    }

    /**
     * Checks if the ArrayList of tracks is empty and changes views accordingly
     *
     * @return True if tracks is not null
     */
    private boolean checkTracks() {
        ArrayList<Track> updatedTracks = fileManager.getTracksFromFiles();

        if (tracksExist) {
            if ((tracks == null || !tracks.isEmpty()) && updatedTracks != null && !updatedTracks.isEmpty()) {
                tracksExist = true;
                arrangeTracks(updatedTracks);
                tracks = updatedTracks;
                allTracks = new ArrayList<>(updatedTracks);
            } else {
                tracksExist = false;
                disableApp();
            }
        }

        return updatedTracks != null && !updatedTracks.isEmpty();
    }

    /**
     * Sets {@link MainActivity#mediaStatus} to {@link com.eeb.musicplayer.R.string#media_status_no_tracks}
     * and {@link MainActivity#txtVcurrentTrack} to {@link com.eeb.musicplayer.R.string#current_track_no_tracks}<br>
     *
     * <br>
     * Disables or hides everything related to the tracks
     * (
     * {@link MainActivity#mpc},
     * {@link MainActivity#trackSeekBar},
     * {@link MainActivity#mc_trackDuration},
     * {@link MainActivity#elapsedTime},
     * {@link MainActivity#rViewTracks},
     * {@link MainActivity#mediaButton},
     * {@link MainActivity#nextButton},
     * {@link MainActivity#previousButton}
     * )
     */
    private void disableApp() {
        mpc.reset();
        mpc.release();

        closeSearchBar();
        if (updateTrackProgressHandler != null) {
            updateTrackProgressHandler.removeCallbacks(updateTrackProgressRunnable);
        }
        trackSeekBar.setEnabled(false);
        trackSeekBar.setProgress(0);
        trackSeekBar.setVisibility(View.GONE);
        mc_trackDuration.setVisibility(View.GONE);
        elapsedTime.setVisibility(View.GONE);
        rViewTracks.setVisibility(View.GONE);
        mediaButton.setChecked(false);
        mediaButton.setEnabled(false);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        mediaStatus.setText(getString(R.string.media_status_no_tracks));
        txtVcurrentTrack.setText(getString(R.string.current_track_no_tracks, Environment.DIRECTORY_MUSIC));
    }

    private void arrangeTracks(ArrayList<Track> tracksToArrange) {
        ArrayList<Track> pinnedTracks = new ArrayList<>();
        ArrayList<Track> notPinnedTracks = new ArrayList<>();

        for (Track track : tracksToArrange) {
            if (track.isPinned()) {
                pinnedTracks.add(track);
            } else {
                notPinnedTracks.add(track);
            }
        }

        tracksToArrange.clear();
        Collections.sort(pinnedTracks, Track::compareTo);
        Collections.sort(notPinnedTracks, Track::compareTo);
        tracksToArrange.addAll(pinnedTracks);
        tracksToArrange.addAll(notPinnedTracks);
    }

    private void filterTracks(String text) {
        if (text != null && !text.isEmpty()) {
            ArrayList<Track> tracksSearched = new ArrayList<>();

            for (Track t : tracks) {
                if ((t.getTitle().toLowerCase()).contains(text.toLowerCase())) {
                    tracksSearched.add(t);
                }
            }

            if (!tracksSearched.isEmpty()) {
                tracks.clear();
                tracks.addAll(tracksSearched);
            }
        }
    }

    private void prepareLastPlayedTrack(Track lastPlayedTrack) {
        if (lastPlayedTrack != null) {
            trackSeekBar.setMax(lastPlayedTrack.getDuration());
            trackSeekBar.setProgress(lastPlayedTrack.getElapsedTime());
            txtVcurrentTrack.setText(lastPlayedTrack.getTitle());
            mc_trackDuration.setText(Track.getFormattedDuration(lastPlayedTrack.getDuration()));
            elapsedTime.setText(Track.getFormattedDuration(lastPlayedTrack.getElapsedTime()));
            doMediaAction(MEDIA_ACTION.START, lastPlayedTrack);
        }
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    private void closeSearchBar() {
        searchBar.setQuery("", false);
        searchBar.clearFocus();
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
        if (tracksExist && checkTracks()) { //Checks if there are tracks, so it doesn't open when they don't exist
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

    private int requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(afChangeListener).build();
            return audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            return audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(afChangeListener);
        }
    }
}
