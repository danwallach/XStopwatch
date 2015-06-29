/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.content.Context;
import android.util.Log;

public class StopwatchState extends SharedState {
    private final static String TAG = "StopwatchState";

    private long priorTime;  // extra time to add in (accounting for prior pause/restart cycles) -- analogous to the "base" time in android.widget.Chronometer
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

    public void reset(Context context) {
        Log.v(TAG, "reset");
        priorTime = startTime = 0;

        super.reset(context);
    }

    public void run(Context context) {
        Log.v(TAG, "run");

        startTime = currentTime();

        super.run(context);
    }

    public void pause(Context context) {
        Log.v(TAG, "pause");

        long pauseTime = currentTime();
        priorTime += (pauseTime - startTime);

        super.pause(context);
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

    @Override
    public long eventTime() {
        // IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
        // IF PAUSED, this time will be relative to zero and will be what should be displayed.

        if(running) {
            return startTime - priorTime;
        } else {
            return priorTime;
        }
    }

    public String getActionNotificationClickString() {
        return Constants.stopwatchStartStopIntent;
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

    public String getShortName() {
        return "[Stopwatch] ";
    }
}
