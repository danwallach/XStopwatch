package org.dwallach.xstopwatch;

import android.util.Log;

import java.util.Observable;

/**
 * Created by dwallach on 12/30/14.
 */
public class StopwatchState extends Observable {
    private final static String TAG = "StopwatchState";

    private boolean running;
    private boolean reset;
    private long priorTime;  // absolute GMT time
    private long startTime;  // absolute GMT time

    private StopwatchState() {
        running = false;
        reset = true;
        priorTime = 0;
        startTime = 0;
    }

    private static StopwatchState singleton;

    public static StopwatchState getSingleton() {
        if(singleton == null)
            singleton = new StopwatchState();
        return singleton;
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

    public long getPriorTime() {
        return priorTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void reset() {
        Log.v(TAG, "reset");
        running = false;
        reset = true;
        priorTime = startTime = 0;

        pingObservers();
    }

    public void run() {
        Log.v(TAG, "run");

        reset = false;
        startTime = currentTime();
        running = true;

        pingObservers();
    }

    public void pause() {
        Log.v(TAG, "pause");
        running = false;

        long pauseTime = currentTime();
        priorTime += (pauseTime - startTime);

        pingObservers();
    }

    public void pingObservers() {
        // this incantation will make observers elsewhere aware that there's new content
        setChanged();
        notifyObservers();
        clearChanged();
    }

    public void restoreState(long priorTime, long startTime, boolean running, boolean reset) {
        this.priorTime = priorTime;
        this.startTime = startTime;
        this.running = running;
        this.reset = reset;

        pingObservers();
    }
}
