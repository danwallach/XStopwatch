/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.content.Context
import android.content.Intent
import android.util.Log

import org.jetbrains.anko.*

object PreferencesHelper {
    private val TAG = "PreferencesHelper"

    fun savePreferences(context: Context) {
        Log.v(TAG, "savePreferences")
        context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE).edit().apply {
            putLong(Constants.prefStopwatchStartTime, StopwatchState.startTime)
            putLong(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
            putBoolean(Constants.prefStopwatchRunning, StopwatchState.isRunning)
            putBoolean(Constants.prefStopwatchReset, StopwatchState.isReset)
            putLong(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)

            if (!commit())
                Log.v(TAG, "savePreferences commit failed ?!")
        }

        context.getSharedPreferences(Constants.sharedPrefsTimer, Context.MODE_PRIVATE).edit().apply {
            putLong(Constants.prefTimerStartTime, TimerState.startTime)
            putLong(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
            putLong(Constants.prefTimerDuration, TimerState.duration)
            putBoolean(Constants.prefTimerRunning, TimerState.isRunning)
            putBoolean(Constants.prefTimerReset, TimerState.isReset)
            putLong(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)

            if (!commit())
                Log.v(TAG, "savePreferences commit failed ?!")
        }
    }

    fun broadcastPreferences(context: Context, action: String?) {
        Log.v(TAG, "broadcastPreferences")

        // There's a chance that our app is not running but we received the broadcast intent
        // asking for our state to be sent out again. In that case, we'll need to load up
        // the preferences again. Otherwise we'll be sending out incorrect data.

        if (!StopwatchState.isInitialized || !TimerState.isInitialized)
            loadPreferences(context)

        if (action == null || action == Constants.stopwatchQueryIntent || action == Constants.stopwatchUpdateIntent) {
            if (!StopwatchState.isInitialized) {
                Log.e(TAG, "stopwatch state not initialized, can't broadcast preferences")
            } else {
                Log.v(TAG, "broadcasting stopwatch preferences")
                context.sendBroadcast(Intent(Constants.stopwatchUpdateIntent).apply {
                    putExtra(Constants.prefStopwatchStartTime, StopwatchState.startTime)
                    putExtra(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
                    putExtra(Constants.prefStopwatchRunning, StopwatchState.isRunning)
                    putExtra(Constants.prefStopwatchReset, StopwatchState.isReset)
                    putExtra(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)
                })
            }
        }

        if (action == null || action == Constants.timerQueryIntent || action == Constants.timerUpdateIntent) {
            if (!TimerState.isInitialized) {
                Log.e(TAG, "timer state not initialized, can't broadcast preferences")
            } else {
                Log.v(TAG, "broadcasting timer preferences")
                context.sendBroadcast(Intent(Constants.timerUpdateIntent).apply {
                    putExtra(Constants.prefTimerStartTime, TimerState.startTime)
                    putExtra(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
                    putExtra(Constants.prefTimerDuration, TimerState.duration)
                    putExtra(Constants.prefTimerRunning, TimerState.isRunning)
                    putExtra(Constants.prefTimerReset, TimerState.isReset)
                    putExtra(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)
                })
            }
        }
    }

    fun loadPreferences(context: Context) {
        Log.v(TAG, "loadPreferences")

        // brackets just so that the variables go away when we leave scope
        context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE).apply {
            val priorTime = getLong(Constants.prefStopwatchBaseTime, 0L)
            val startTime = getLong(Constants.prefStopwatchStartTime, 0L)
            val isRunning = getBoolean(Constants.prefStopwatchRunning, false)
            val isReset = getBoolean(Constants.prefStopwatchReset, true)
            val updateTimestamp = getLong(Constants.prefStopwatchUpdateTimestamp, 0L)

            Log.v(TAG, "Stopwatch:: startTime($startTime), priorTime($priorTime), isRunning($isRunning), isReset($isReset), updateTimestamp($updateTimestamp)")

            StopwatchState.restoreState(priorTime, startTime, isRunning, isReset, updateTimestamp)
        }

        context.getSharedPreferences(Constants.sharedPrefsTimer, Context.MODE_PRIVATE).apply {
            val startTime = getLong(Constants.prefTimerStartTime, 0L)
            val pauseDelta = getLong(Constants.prefTimerPauseElapsed, 0L)
            val duration = getLong(Constants.prefTimerDuration, 0L)
            var isRunning = getBoolean(Constants.prefTimerRunning, false)
            var isReset = getBoolean(Constants.prefTimerReset, true)
            val updateTimestamp = getLong(Constants.prefTimerUpdateTimestamp, 0L)

            // sanity checking: if we're coming back from whatever, and we discover that we *used* to
            // be running, but we've gotten way past the deadline, then just reset things.
            val currentTime = System.currentTimeMillis()
            if (isRunning && startTime + duration < currentTime) {
                isReset = true
                isRunning = false
            }

            Log.v(TAG, "Timer:: startTime($startTime), pauseDelta($pauseDelta), duration($duration), isRunning($isRunning), isReset($isReset), updateTimestamp($updateTimestamp)")

            TimerState.restoreState(context, duration, pauseDelta, startTime, isRunning, isReset, updateTimestamp)
        }
    }
}
