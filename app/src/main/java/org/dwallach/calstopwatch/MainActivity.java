package org.dwallach.calstopwatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private TimeView timeView;
    private ImageButton resetButton;
    private ImageButton playButton;

    private boolean playIconShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                timeView = (TimeView) stub.findViewById(R.id.elapsedTime);
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);

                resetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(TAG, "reset click");
                        timeView.reset();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                        playIconShown = true;
                    }
                });

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(playIconShown) {
                            Log.v(TAG, "play click");
                            timeView.run();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);
                            playIconShown = false;
                        } else {
                            Log.v(TAG, "pause click");
                            timeView.pause();
                            playButton.setImageResource(android.R.drawable.ic_media_play);
                            playIconShown = true;
                        }
                    }
                });
            }
        });
    }
}
