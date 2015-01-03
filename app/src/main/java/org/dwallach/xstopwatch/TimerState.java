/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Observable;

public class TimerState extends SharedState {
    private final static String TAG = "TimerState";

    private long pauseDelta;  // if the timer's not running, this says how far we got (i.e., we're at startTime + pauseDelta, and 0 <= pauseDelta <= duration)
    private long startTime;  // when the stopwatch started running
    private long duration;   // when the timer ends (i.e., stop at startTime + duration, assuming it's running)

    private TimerState() {
        super();
        pauseDelta = 0;
        duration = 0;
        startTime = 0;
    }

    private static TimerState singleton;

    public static TimerState getSingleton() {
        if(singleton == null)
            singleton = new TimerState();
        return singleton;
    }

    public long getPauseDelta() {
        return pauseDelta;
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
        pauseDelta = startTime = 0;

        super.reset();
        updateBuzzHandler();
    }

    public void run() {
        Log.v(TAG, "run");

        if(isReset())
            startTime = currentTime();
        else {
            // we're resuming from a pause, so we need to shove up the start time
            long pauseTime = startTime + pauseDelta;
            startTime += currentTime() - pauseTime;
        }

        super.run();
        updateBuzzHandler();
    }

    public void pause() {
        Log.v(TAG, "pause");

        long pauseTime = currentTime();
        pauseDelta = (pauseTime - startTime);
        if(pauseDelta > duration) pauseDelta = duration;

        super.pause();
        updateBuzzHandler();
    }

    public void restoreState(long duration, long pauseDelta, long startTime, boolean running, boolean reset, long updateTimestamp) {
        Log.v(TAG, "restoring state");
        this.duration = duration;
        this.pauseDelta = pauseDelta;
        this.startTime = startTime;
        this.running = running;
        this.reset = reset;
        this.updateTimestamp = updateTimestamp;
        initialized = true;

        updateBuzzHandler();
        pingObservers();
    }

    private static String timeString(long deltaTime, boolean subSeconds) {
        if(deltaTime < 0) deltaTime = 0;
        int cent = (int)((deltaTime /     10L) % 100L);

        String secondsResult = DateUtils.formatElapsedTime(deltaTime / 1000);
        if(subSeconds)
            return String.format("%s.%02d", secondsResult, cent);
        else
            return secondsResult;

    }

    public String currentTimeString(boolean subSeconds) {
        long pauseDelta = getPauseDelta();
        long duration = getDuration();
        long startTime = getStartTime();

        if (isReset()) {
            return timeString(duration, subSeconds);
        } else if (!isRunning()) {
            return timeString(duration - pauseDelta, subSeconds);
        } else {
            long timeNow = currentTime();
            return timeString(duration - timeNow + startTime, subSeconds);
        }
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
}
