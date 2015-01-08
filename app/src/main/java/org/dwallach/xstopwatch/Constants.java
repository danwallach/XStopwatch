/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

public class Constants {
    public static final String prefStopwatchRunning = "running";
    public static final String prefStopwatchReset = "reset";
    public static final String prefStopwatchStartTime = "start";
    public static final String prefStopwatchBaseTime = "base";
    public static final String prefStopwatchUpdateTimestamp = "updateTimestamp";

    public static final String sharedPrefsStopwatch = "org.dwallach.x.stopwatch.prefs";
    public static final String stopwatchUpdateIntent = "org.dwallach.x.stopwatch.update";
    public static final String stopwatchQueryIntent = "org.dwallach.x.stopwatch.query";

    public static final String prefTimerRunning = "running";
    public static final String prefTimerReset = "reset";
    public static final String prefTimerStartTime = "start";
    public static final String prefTimerPauseElapsed = "elapsed";
    public static final String prefTimerDuration = "duration";
    public static final String prefTimerUpdateTimestamp = "updateTimestamp";

    public static final String sharedPrefsTimer = "org.dwallach.x.timer.prefs";
    public static final String timerUpdateIntent = "org.dwallach.x.timer.update";
    public static final String timerQueryIntent = "org.dwallach.x.timer.query";

    public static final String actionTimerComplete = "org.dwallach.x.timer.ACTION_TIMER_COMPLETE";

}
