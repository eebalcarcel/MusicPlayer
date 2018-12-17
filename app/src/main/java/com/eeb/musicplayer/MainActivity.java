package com.eeb.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.Constraints;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TouchTypeDetector;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST = 1;
    private Button mediaButton, nextButton, previousButton;
    private TextView mediaStatus, song;
    private Toolbar topBar, bottomBar;
    private SearchView searchBar;
    private List<File> files;
    private File currentlyPlayingTrack;
    private MediaPlayer mp;
    private RelativeLayout rel;
    private Window window;

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

        mediaButton = findViewById(R.id.mediaButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        mediaStatus = findViewById(R.id.mediaStatus);
        song = findViewById(R.id.song);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        searchBar = findViewById(R.id.searchBar);

        //Adds status bar height to the topBar height
        topBar.post(new Runnable() {
            @Override
            public void run() {
                topBar.setLayoutParams(new Constraints.LayoutParams(topBar.getWidth(), topBar.getHeight() + getStatusBarHeight()));
            }
        });

        //Moves searchBar up so slide down animation works flawlessly the first time
        searchBar.post(new Runnable() {
            @Override
            public void run() {
                searchBar.setTranslationY(-searchBar.getHeight());
            }
        });

        //Hides searchBar when it loses focus
        searchBar.setIconifiedByDefault(false);
        searchBar.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    searchBar.animate()
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
                        searchBar.animate()
                                .setDuration(250)
                                .translationY(-searchBar.getHeight())
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
                        searchBar.animate()
                                .setDuration(250)
                                .translationY(0)
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

        mediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mp.isPlaying()) {
                    try {
                        mp.setDataSource(files.get(0).getPath());
                        mp.prepare();
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mp.setDataSource(files.get(0).getPath());
                    mp.prepare();
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String path = Environment.getExternalStorageDirectory() + "/Music";
                    File directory = new File(path);
                    files = Arrays.asList(directory.listFiles());

                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File s1, File s2) {
                            return s1.getName().compareToIgnoreCase(s2.getName());
                        }
                    });

                    if (files != null) {
                        for (int i = 0; i < files.size(); i++) {

                        }
                    }
                    song.setText(getFileNameWithoutExtension(files.get(0)));
                } else {
                    finishAndRemoveTask();
                }
                return;
            }
        }
    }


    private static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        return name;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
