package org.dwallach.xstopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.Observable;
import java.util.Observer;

public class TimerActivity extends Activity implements Observer {
    private static final String TAG = "TimerActivity";

    private TimerState timerState = TimerState.getSingleton();
    private ImageButton resetButton;
    private ImageButton playButton;
    private ImageButton setButton;
    private NotificationHelper notificationHelper;
    private StopwatchText stopwatchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_timer);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.v(TAG, "onLayoutInflated");
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);
                setButton = (ImageButton) stub.findViewById(R.id.setButton);
                stopwatchText = (StopwatchText) stub.findViewById(R.id.elapsedTime);

                // bring in saved preferences
                PreferencesHelper.loadPreferences(TimerActivity.this);

                // now that we've loaded the state, we know whether we're playing or paused
                setPlayButtonIcon();

                // set up notification helper, and use this as a proxy for whether
                // or not we need to set up everybody who pays attention to the timerState
                if(notificationHelper == null) {
                    notificationHelper = new NotificationHelper(TimerActivity.this,
                            R.drawable.sandwatch_trans_ic_launcher,
                            getResources().getString(R.string.timer_app_name),
                            timerState);

                    setStopwatchObservers(true);
                }

                resetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timerState.reset();
                        PreferencesHelper.savePreferences(TimerActivity.this);
                    }
                });

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timerState.click();
                        PreferencesHelper.savePreferences(TimerActivity.this);
                    }
                });
            }
        });
    }

    /**
     * install the observers that care about the timerState: "this", which updates the
     * visible UI parts of the activity, and the notificationHelper, which deals with the popup
     * notifications elsewhere
     *
     * @param includeActivity If the current activity isn't visible, then make this false and it won't be notified
     */
    private void setStopwatchObservers(boolean includeActivity) {
        timerState.deleteObservers();
        if(notificationHelper != null)
            timerState.addObserver(notificationHelper);
        if(includeActivity) {
            timerState.addObserver(this);

            if (stopwatchText != null)
                timerState.addObserver(stopwatchText);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart");

        timerState.setVisible(true);
        setStopwatchObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "onResume");

        timerState.setVisible(true);
        setStopwatchObservers(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(TAG, "onPause");

        timerState.setVisible(false);
        setStopwatchObservers(false);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.v(TAG, "activity update");
        if(playButton != null)
            setPlayButtonIcon();
    }

    private void setPlayButtonIcon() {
        if (timerState.isRunning() && playButton != null)
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        else
            playButton.setImageResource(android.R.drawable.ic_media_play);
    }
}
