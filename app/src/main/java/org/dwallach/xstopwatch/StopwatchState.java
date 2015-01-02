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
    private long priorTime;  // absolute GMT time
    private long startTime;  // absolute GMT time
    private boolean visible;

    private StopwatchState() {
        running = false;
        reset = true;
        priorTime = 0;
        startTime = 0;
        visible = false;
    }

    private static StopwatchState singleton;

    public void setVisible(boolean visible) {
        this.visible = visible;
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

    public void click() {
        Log.v(TAG, "click");
        if(isRunning())
            pause();
        else
            run();
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
