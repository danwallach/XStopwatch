/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Observable;

abstract class SharedState extends Observable {
    private final static String TAG = "SharedState";

    protected boolean running;
    protected boolean reset;
    protected long updateTimestamp;  // when the last user interaction was
    protected boolean visible;
    protected boolean initialized;

    protected SharedState() {
        running = false;
        reset = true;
        visible = false;
        initialized = false;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    private void makeUpdateTimestamp() {
        updateTimestamp = currentTime();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setVisible(boolean visible) {
        Log.v(TAG, getShortName() + "visible: " + visible);
        this.visible = visible;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public boolean isVisible() {
        return visible;
    }

    public static long currentTime() {
        return System.currentTimeMillis();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isReset() {
        return reset;
    }

    public void reset(Context context) {
        Log.v(TAG, getShortName() + "reset");
        running = false;
        reset = true;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void run(Context context) {
        Log.v(TAG, getShortName() + "run");

        reset = false;
        running = true;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void pause(Context context) {
        Log.v(TAG, getShortName() + "pause");

        running = false;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void click(Context context) {
        Log.v(TAG, getShortName() + "click");
        if (isRunning())
            pause(context);
        else
            run(context);
    }

    public void pingObservers() {
        // this incantation will make observers elsewhere aware that there's new content
        Log.v(TAG, getShortName() + "pinging");
        setChanged();
        notifyObservers();
        clearChanged();
        Log.v(TAG, getShortName() + "ping complete");
    }

    /**
     * Return the time of either when the stopwatch began or when the countdown ends.
     * IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
     * IF PAUSED, this time will be relative to zero and will be what should be displayed.
     * Make sure to call isRunning() to know how to interpret this result.
     * @return GMT time in milliseconds
     */
    abstract public long eventTime();

    /**
     * This converts an absolute time, as returned by eventTime, to a relative time
     * that might be displayed
     */
    public String relativeTimeString(long eventTime) {
        if(running) {
            long timeNow = System.currentTimeMillis();
            long delta = timeNow - eventTime;
            if (delta < 0) delta = -delta;
            return DateUtils.formatElapsedTime(delta / 1000);
        } else {
            if (eventTime < 0) eventTime = -eventTime;
            return DateUtils.formatElapsedTime(eventTime / 1000);
        }
    }

    public String toString() {
        return relativeTimeString(eventTime());
    }

    abstract public String getActionNotificationClickString();

    abstract public int getNotificationID();

    abstract public Class getActivity();

    abstract public int getIconID();

    abstract public String getShortName();
}
