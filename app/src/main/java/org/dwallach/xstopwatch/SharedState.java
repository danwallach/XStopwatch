/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.Activity;
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
        Log.v(TAG, "visible: " + visible);
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

    public void reset() {
        Log.v(TAG, "reset");
        running = false;
        reset = true;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void run() {
        Log.v(TAG, "run");

        reset = false;
        running = true;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void pause() {
        Log.v(TAG, "pause");

        running = false;
        initialized = true;

        makeUpdateTimestamp();
        pingObservers();
    }

    public void click() {
        Log.v(TAG, "click");
        if (isRunning())
            pause();
        else
            run();
    }

    public void pingObservers() {
        // this incantation will make observers elsewhere aware that there's new content
        Log.v(TAG, "pinging");
        setChanged();
        notifyObservers();
        clearChanged();
        Log.v(TAG, "ping complete");
    }

    abstract public String currentTimeString(boolean subSeconds);

    public String toString() {
        return currentTimeString(true);
    }

    abstract public String getActionNotificationClickString();

    abstract public int getNotificationID();

    abstract public Class getActivity();

    abstract public int getIconID();
}
