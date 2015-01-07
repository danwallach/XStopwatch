/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.os.Handler;
import android.util.Log;

public class TimerState extends SharedState {
    private final static String TAG = "TimerState";

    private long elapsedTime;  // if the timer's not running, this says how far we got (i.e., we're at startTime + elapsedTime, and 0 <= elapsedTime <= duration)
    private long startTime;  // when the timer started running
    private long duration;   // when the timer ends (i.e., the timer completes at startTime + duration, assuming it's running)

    private TimerState() {
        super();
        elapsedTime = 0;
        duration = 0;
        startTime = 0;
    }

    private static TimerState singleton;

    public static TimerState getSingleton() {
        if(singleton == null)
            singleton = new TimerState();
        return singleton;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        reset();
    }

    public long getStartTime() {
        return startTime;
    }

    public void reset() {
        Log.v(TAG, "reset");

        // don't overwrite duration -- that's a user setting
        elapsedTime = startTime = 0;

        super.reset();
        updateBuzzHandler();
    }

    public void run() {
        Log.v(TAG, "run");

        if(duration == 0) return; // don't do anything unless there's a non-zero duration

        if(isReset())
            startTime = currentTime();
        else {
            // we're resuming from a pause, so we need to shove up the start time
            long pauseTime = startTime + elapsedTime;
            startTime += currentTime() - pauseTime;
        }

        super.run();
        updateBuzzHandler();
    }

    public void pause() {
        Log.v(TAG, "pause");

        long pauseTime = currentTime();
        elapsedTime = (pauseTime - startTime);
        if(elapsedTime > duration) elapsedTime = duration;

        super.pause();
        updateBuzzHandler();
    }

    public void restoreState(long duration, long elapsedTime, long startTime, boolean running, boolean reset, long updateTimestamp) {
        Log.v(TAG, "restoring state");
        this.duration = duration;
        this.elapsedTime = elapsedTime;
        this.startTime = startTime;
        this.running = running;
        this.reset = reset;
        this.updateTimestamp = updateTimestamp;
        initialized = true;

        updateBuzzHandler();
        pingObservers();
    }

    @Override
    public long eventTime() {
        // IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
        // IF PAUSED, this time will be relative to zero and will be what should be displayed.

        if(reset) return duration;
        if(!running) return duration - elapsedTime;

        // running
        return duration + startTime;

    }

    public static final String ACTION_NOTIFICATION_CLICK_STRING = "intent.action.notification.timer.click";

    public String getActionNotificationClickString() {
        return ACTION_NOTIFICATION_CLICK_STRING;
    }

    public int getNotificationID() {
        return 2;
    }

    public Class getActivity() {
        return TimerActivity.class;
    }

    private Handler buzzHandler;

    public void setBuzzHandler(Handler buzzHandler) {
        this.buzzHandler = buzzHandler;
    }

    private void updateBuzzHandler() {
        if(buzzHandler != null) {
            if(isRunning()) {
                long timeNow = currentTime();
                long delayTime = duration - timeNow + startTime;
                if (delayTime > 0) {
                    Log.v(TAG, "setting buzz handler: " + delayTime + " ms in the future");
                    buzzHandler.sendEmptyMessageDelayed(TimerActivity.MSG_BUZZ_TIME, delayTime);
                } else {
                    Log.v(TAG, "buzz handler in the past, not setting");
                }
            } else {
                Log.v(TAG, "removing buzz handler");
                buzzHandler.removeMessages(TimerActivity.MSG_BUZZ_TIME);
            }
        }
    }

    public int getIconID() {
        return R.drawable.sandwatch_selected_400;
    }

    public String getShortName() {
        return "[Timer] ";
    }
}
