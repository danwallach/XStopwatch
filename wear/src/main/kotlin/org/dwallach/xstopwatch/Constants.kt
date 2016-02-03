/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

object Constants {
    const val prefStopwatchRunning = "running"
    const val prefStopwatchReset = "reset"
    const val prefStopwatchStartTime = "start"
    const val prefStopwatchBaseTime = "base"
    const val prefStopwatchUpdateTimestamp = "updateTimestamp"

    const val sharedPrefsStopwatch = "org.dwallach.x.stopwatch.prefs"
    const val stopwatchUpdateIntent = "org.dwallach.x.stopwatch.update"
    const val stopwatchQueryIntent = "org.dwallach.x.stopwatch.query"
    const val stopwatchStartStopIntent = "org.dwallach.x.stopwatch.startstop"

    const val prefTimerRunning = "running"
    const val prefTimerReset = "reset"
    const val prefTimerStartTime = "start"
    const val prefTimerPauseElapsed = "elapsed"
    const val prefTimerDuration = "duration"
    const val prefTimerUpdateTimestamp = "updateTimestamp"

    const val sharedPrefsTimer = "org.dwallach.x.timer.prefs"
    const val timerUpdateIntent = "org.dwallach.x.timer.update"
    const val timerQueryIntent = "org.dwallach.x.timer.query"
    const val timerStartStopIntent = "org.dwallach.x.timer.startstop"

    const val actionTimerComplete = "org.dwallach.x.timer.ACTION_TIMER_COMPLETE"
}
