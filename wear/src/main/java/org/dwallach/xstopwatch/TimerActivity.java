package org.dwallach.xstopwatch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.util.Calendar;
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

    private Handler buttonStateHandler;

    // see http://developer.android.com/guide/topics/ui/controls/pickers.html
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private TimerState timerState = TimerState.getSingleton();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            long duration = timerState.getDuration(); // in milliseconds
            int minute = (int) ((duration / 60000) % 60);
            int hour = (int) (duration / 3600000);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            // Do something with the time chosen by the user
            timerState.setDuration(null, hour * 3600000 + minute * 60000);
        }
    }

    // call to this specified in the layout xml files
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    // call to this specified in the layout xml files
    public void launchStopwatch(View view) {
        startActivity(new Intent(this, StopwatchActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate");

        setContentView(R.layout.activity_timer);

        // This buttonState business is all about dealing with alarms, which go to
        // NotificationService, on a different thread, which needs to ping us to
        // update the UI, if we exist. This handler will always run on the UI thread.
        // It's invoked from the update() method down below, which may run on other threads.
        buttonStateHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Log.v(TAG, "button state message received");
                if (playButton != null)
                    setPlayButtonIcon();
            }
        };

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.v(TAG, "onLayoutInflated");
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);
                setButton = (ImageButton) stub.findViewById(R.id.setButton);
                stopwatchText = (StopwatchText) stub.findViewById(R.id.elapsedTime);
                stopwatchText.setSharedState(timerState);


                String action = getIntent().getAction();

                int paramLength = getIntent().getIntExtra(AlarmClock.EXTRA_LENGTH, 0);
                Log.v(TAG, "intent action: " + action);

                if (paramLength > 0 && paramLength <= 86400) {
                    Log.v(TAG, "onCreate, somebody told us a time value: " + paramLength);
                    long durationMillis = paramLength * 1000;
                    TimerState.getSingleton().setDuration(TimerActivity.this, durationMillis);
                    PreferencesHelper.savePreferences(TimerActivity.this);
                    PreferencesHelper.broadcastPreferences(TimerActivity.this, Constants.timerUpdateIntent);
                } else {
                    // bring in saved preferences
                    PreferencesHelper.loadPreferences(TimerActivity.this);
                }

                // now that we've loaded the state, we know whether we're playing or paused
                setPlayButtonIcon();

                // get the notification service running as well; it will stick around to make sure
                // the broadcast receiver is alive
                NotificationService.kickStart(TimerActivity.this);

                // set up notification helper, and use this as a proxy for whether
                // or not we need to set up everybody who pays attention to the timerState
                if (notificationHelper == null) {
                    notificationHelper = new NotificationHelper(TimerActivity.this,
                            R.drawable.sandwatch_trans,
                            getResources().getString(R.string.timer_app_name),
                            TimerState.getSingleton());
                    setStopwatchObservers(true);
                }

                resetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timerState.reset(TimerActivity.this);
                        PreferencesHelper.savePreferences(TimerActivity.this);
                        PreferencesHelper.broadcastPreferences(TimerActivity.this, Constants.timerUpdateIntent);
                    }
                });

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timerState.click(TimerActivity.this);
                        PreferencesHelper.savePreferences(TimerActivity.this);
                        PreferencesHelper.broadcastPreferences(TimerActivity.this, Constants.timerUpdateIntent);
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

        // We might be called on the UI thread or on a service thread; we need to dispatch this
        // entirely on the UI thread, since we're ultimately going to be monkeying with the UI.
        // Thus this nonsense.
        buttonStateHandler.sendEmptyMessage(0);
    }

    private void setPlayButtonIcon() {
        Log.v(TAG, "setPlayButtonIcon");
        if (timerState.isRunning() && playButton != null)
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        else
            playButton.setImageResource(android.R.drawable.ic_media_play);
    }
}
