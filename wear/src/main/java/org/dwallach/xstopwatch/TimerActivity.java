package org.dwallach.xstopwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.AlarmClock;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TimePicker;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class TimerActivity extends Activity implements Observer {
    private static final String TAG = "TimerActivity";

    private TimerState timerState = TimerState.getSingleton();
    private ImageButton resetButton;
    private ImageButton playButton;
    private NotificationHelper notificationHelper;
    private StopwatchText stopwatchText;

    private Handler buttonStateHandler;

    public static class MyHandler extends Handler {
        private WeakReference<TimerActivity> timerActivityRef;

        public MyHandler(Looper looper, TimerActivity timerActivity) {
            super(looper);
            timerActivityRef = new WeakReference<>(timerActivity);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            Log.v(TAG, "button state message received");
            TimerActivity timerActivity = timerActivityRef.get();
            if(timerActivity == null) return; // oops, it's gone

            if (timerActivity.playButton != null)
                timerActivity.setPlayButtonIcon();
        }
    }


    // see http://developer.android.com/guide/topics/ui/controls/pickers.html

    /**
     * this uses the built-in TimePickerDialog to ask the user to specify the hours and minutes
     * for the count-down timer. Of course, it works fine on the emulator and on a Moto360, but
     * totally fails on the LG G Watch and G Watch R, apparently trying to show a full-blown
     * Material Design awesome thing that was never tuned to fit on a watch. Instead, see
     * the separate TimePickerFragment class, which might be ugly, but at least it works consistently.
     *
     * TODO: move back to this code and kill TimePickerFragment once they fix the bug in Wear
     */
    public static class FailedTimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private TimerState timerState = TimerState.getSingleton();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            long duration = timerState.getDuration(); // in milliseconds
            int minute = (int) ((duration / 60000) % 60);
            int hour = (int) (duration / 3600000);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_DARK, this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            // Do something with the time chosen by the user
            timerState.setDuration(null, hour * 3600000 + minute * 60000);
        }
    }

    // call to this specified in the layout xml files
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = TimePickerFragment.newInstance();
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

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            Log.i(TAG, "Version: " + versionName + " (" + versionNumber + ")");

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "couldn't read version", e);
        }

        // there's a chance we were launched through a specific intent to set a timer for
        // a particular length; this is how we figure it out
        Intent intent = getIntent();
        String action = intent.getAction();
        int paramLength = intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0);
        boolean skipUI = intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false);

        Log.v(TAG, "intent action: " + action + ", length(" + paramLength + ")");

        Bundle allExtras = getIntent().getExtras();
        if(allExtras != null) {
            Set<String> keySet = allExtras.keySet();

            // because we're trying to figure out what's actually in here
            for (String key : keySet) {
                try {
                    Log.v(TAG, "--- found extra: " + key + " -> " + allExtras.get(key).toString());
                } catch (NullPointerException npe) {
                    // rare chance of failure with get(key) above returning null; ignore
                    // and move on
                }
            }
        } else {
            Log.v(TAG, "--- no extras found!");
        }

        if (paramLength > 0 && paramLength <= 86400) {
            Log.v(TAG, "onCreate, somebody told us a time value: " + paramLength);
            long durationMillis = paramLength * 1000;
            timerState.setDuration(TimerActivity.this, durationMillis);
            timerState.reset(TimerActivity.this);
            if(skipUI)
                timerState.click(TimerActivity.this);

            PreferencesHelper.savePreferences(TimerActivity.this);
            PreferencesHelper.broadcastPreferences(TimerActivity.this, Constants.timerUpdateIntent);
        } else {
            // bring in saved preferences
            PreferencesHelper.loadPreferences(TimerActivity.this);
        }


        setContentView(R.layout.activity_timer);

        // This buttonState business is all about dealing with alarms, which go to
        // NotificationService, on a different thread, which needs to ping us to
        // update the UI, if we exist. This handler will always run on the UI thread.
        // It's invoked from the update() method down below, which may run on other threads.
        buttonStateHandler = new MyHandler(Looper.getMainLooper(), this);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                Log.v(TAG, "onLayoutInflated");
                resetButton = (ImageButton) stub.findViewById(R.id.resetButton);
                playButton = (ImageButton) stub.findViewById(R.id.playButton);
//                setButton = (ImageButton) stub.findViewById(R.id.setButton);
                stopwatchText = (StopwatchText) stub.findViewById(R.id.elapsedTime);
                stopwatchText.setSharedState(timerState);


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
        if(playButton != null) {
            if (timerState.isRunning())
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            else
                playButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }
}
