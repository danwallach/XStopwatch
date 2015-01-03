/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.text.format.DateUtils;
import android.util.Log;

public class StopwatchState extends SharedState {
    private final static String TAG = "StopwatchState";

    private long priorTime;  // extra time to add in (accounting for prior pause/restart cycles)
    private long startTime;  // when the stopwatch started running

    private StopwatchState() {
        super();
        priorTime = 0;
        startTime = 0;
    }

    private static StopwatchState singleton;

    public static StopwatchState getSingleton() {
        if(singleton == null)
            singleton = new StopwatchState();
        return singleton;
    }

    public long getPriorTime() {
        return priorTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void reset() {
        Log.v(TAG, "reset");
        priorTime = startTime = 0;

        super.reset();
    }

    public void run() {
        Log.v(TAG, "run");

        startTime = currentTime();

        super.run();
    }

    public void pause() {
        Log.v(TAG, "pause");

        long pauseTime = currentTime();
        priorTime += (pauseTime - startTime);

        super.pause();
    }

    public void restoreState(long priorTime, long startTime, boolean running, boolean reset, long updateTimestamp) {
        Log.v(TAG, "restoring state");
        this.priorTime = priorTime;
        this.startTime = startTime;
        this.running = running;
        this.reset = reset;
        this.updateTimestamp = updateTimestamp;
        initialized = true;

        pingObservers();
    }

    private static String timeString(long deltaTime, boolean subSeconds) {
        int cent = (int)((deltaTime /     10L) % 100L);

        String secondsResult = DateUtils.formatElapsedTime(deltaTime / 1000);
        if(subSeconds)
            return String.format("%s.%02d", secondsResult, cent);
        else
            return secondsResult;

    }

    private static final String zeroString = timeString(0, true);
    private static final String zeroStringNoSubSeconds = timeString(0, false);

    public String currentTimeString(boolean subSeconds) {
        long priorTime = getPriorTime();
        long startTime = getStartTime();
        long currentTime = currentTime();

        if (isReset()) {
            return (subSeconds)? zeroString: zeroStringNoSubSeconds;
        } else if (!isRunning()) {
            return timeString(priorTime, subSeconds);
        } else {
            long timeNow = currentTime;
            return timeString(timeNow - startTime + priorTime, subSeconds);
        }
    }

    public static final String ACTION_NOTIFICATION_CLICK_STRING = "intent.action.notification.stopwatch.click";

    public String getActionNotificationClickString() {
        return ACTION_NOTIFICATION_CLICK_STRING;
    }

    public int getNotificationID() {
        return 1;
    }

    public Class getActivity() {
        return StopwatchActivity.class;
    }

    public int getIconID() {
        return R.drawable.stopwatch_selected_400;
    }
}
