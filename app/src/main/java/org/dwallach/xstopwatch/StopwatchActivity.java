package org.dwallach.xstopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class StopwatchActivity extends Activity {
    private static final String TAG = "StopwatchActivity";

    private StopwatchText stopwatchText;
    private StopwatchState stopwatchState = StopwatchState.getSingleton();
    private ImageButton resetButton;
    private ImageButton playButton;

    private boolean playIconShown = true;

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
                stopwatchText = (StopwatchText) stub.findViewById(R.id.elapsedTime);
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);

                // bring in saved preferences
                PreferencesHelper.loadPreferences(StopwatchActivity.this);

                resetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopwatchState.reset();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                        playIconShown = true;
                        PreferencesHelper.savePreferences(StopwatchActivity.this);
                    }
                });

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(playIconShown) {
                            stopwatchState.run();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);
                            playIconShown = false;
                        } else {
                            stopwatchState.pause();
                            playButton.setImageResource(android.R.drawable.ic_media_play);
                            playIconShown = true;
                        }
                        PreferencesHelper.savePreferences(StopwatchActivity.this);
                    }
                });
            }
        });
    }
}
