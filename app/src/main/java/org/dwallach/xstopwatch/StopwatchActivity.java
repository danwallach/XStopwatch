/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.Observable;
import java.util.Observer;

public class StopwatchActivity extends Activity implements Observer {
    private static final String TAG = "StopwatchActivity";

    private StopwatchState stopwatchState = StopwatchState.getSingleton();
    private ImageButton resetButton;
    private ImageButton playButton;
    private NotificationHelper notificationHelper;
    private StopwatchText stopwatchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_stopwatch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.v(TAG, "onLayoutInflated");
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);
                stopwatchText = (StopwatchText) stub.findViewById(R.id.elapsedTime);
                stopwatchText.setSharedState(stopwatchState);

                // bring in saved preferences
                PreferencesHelper.loadPreferences(StopwatchActivity.this);

                // now that we've loaded the state, we know whether we're playing or paused
                setPlayButtonIcon();

                // set up notification helper, and use this as a proxy for whether
                // or not we need to set up everybody who pays attention to the stopwatchState
                if(notificationHelper == null) {
                    notificationHelper = new NotificationHelper(StopwatchActivity.this,
                            R.drawable.stopwatch_trans,
                            getResources().getString(R.string.stopwatch_app_name),
                            stopwatchState);

                    setStopwatchObservers(true);
                }

                resetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopwatchState.reset();
                        PreferencesHelper.savePreferences(StopwatchActivity.this);
                        PreferencesHelper.broadcastPreferences(StopwatchActivity.this, Constants.stopwatchUpdateIntent);
                    }
                });

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopwatchState.click();
                        PreferencesHelper.savePreferences(StopwatchActivity.this);
                        PreferencesHelper.broadcastPreferences(StopwatchActivity.this, Constants.stopwatchUpdateIntent);
                    }
                });
            }
        });
    }

    // call to this specified in the layout xml files
    public void launchTimer(View view) {
        startActivity(new Intent(this, TimerActivity.class));
    }

    /**
     * install the observers that care about the stopwatchState: "this", which updates the
     * visible UI parts of the activity, and the notificationHelper, which deals with the popup
     * notifications elsewhere
     *
     * @param includeActivity If the current activity isn't visible, then make this false and it won't be notified
     */
    private void setStopwatchObservers(boolean includeActivity) {
        stopwatchState.deleteObservers();
        if(notificationHelper != null)
            stopwatchState.addObserver(notificationHelper);
        if(includeActivity) {
            stopwatchState.addObserver(this);

            if (stopwatchText != null)
                stopwatchState.addObserver(stopwatchText);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart");

        stopwatchState.setVisible(true);
        setStopwatchObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "onResume");

        stopwatchState.setVisible(true);
        setStopwatchObservers(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(TAG, "onPause");

        stopwatchState.setVisible(false);
        setStopwatchObservers(false);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.v(TAG, "activity update");
        if(playButton != null)
            setPlayButtonIcon();
    }

    private void setPlayButtonIcon() {
        if (stopwatchState.isRunning() && playButton != null)
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        else
            playButton.setImageResource(android.R.drawable.ic_media_play);
    }
}
