package org.dwallach.xstopwatch;

import android.text.format.DateUtils;
import android.util.Log;

import java.util.Observable;

/**
 * Created by dwallach on 12/30/14.
 */
public class StopwatchState extends Observable {
    private final static String TAG = "StopwatchState";

    private boolean running;
    private boolean reset;
    private long priorTime;  // extra time to add in (accounting for prior pause/restart cycles)
    private long startTime;  // when the stopwatch started running
    private long updateTimestamp;  // when the last user interaction was
    private boolean visible;
    private boolean initialized;

    private StopwatchState() {
        running = false;
        reset = true;
        priorTime = 0;
        startTime = 0;
        visible = false;

        initialized = false;
    }

    private static StopwatchState singleton;

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
        initialized = true;

        makeUpdateTimestamp();

        pingObservers();
    }

    public void run() {
        Log.v(TAG, "run");

        reset = false;
        startTime = currentTime();
        running = true;
        initialized = true;

        makeUpdateTimestamp();

        pingObservers();
    }

    public void pause() {
        Log.v(TAG, "pause");
        running = false;

        long pauseTime = currentTime();
        priorTime += (pauseTime - startTime);
        initialized = true;

        makeUpdateTimestamp();

        pingObservers();
    }

    public void click() {
        Log.v(TAG, "click");
        if(isRunning())
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

    public String toString() {
        return currentTimeString(true);
    }
}
