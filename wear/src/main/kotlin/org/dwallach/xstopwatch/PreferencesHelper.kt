/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log

object PreferencesHelper {
    private val TAG = "PreferencesHelper"

    fun savePreferences(context: Context) {
        Log.v(TAG, "savePreferences")
        var prefs = context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = prefs.edit()

        editor.putLong(Constants.prefStopwatchStartTime, StopwatchState.startTime)
        editor.putLong(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
        editor.putBoolean(Constants.prefStopwatchRunning, StopwatchState.isRunning)
        editor.putBoolean(Constants.prefStopwatchReset, StopwatchState.isReset)
        editor.putLong(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)

        if (!editor.commit())
            Log.v(TAG, "savePreferences commit failed ?!")

        prefs = context.getSharedPreferences(Constants.sharedPrefsTimer, Context.MODE_PRIVATE)
        editor = prefs.edit()

        editor.putLong(Constants.prefTimerStartTime, TimerState.startTime)
        editor.putLong(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
        editor.putLong(Constants.prefTimerDuration, TimerState.duration)
        editor.putBoolean(Constants.prefTimerRunning, TimerState.isRunning)
        editor.putBoolean(Constants.prefTimerReset, TimerState.isReset)
        editor.putLong(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)

        if (!editor.commit())
            Log.v(TAG, "savePreferences commit failed ?!")
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
                val broadcast = Intent(Constants.stopwatchUpdateIntent)
                broadcast.putExtra(Constants.prefStopwatchStartTime, StopwatchState.startTime)
                broadcast.putExtra(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
                broadcast.putExtra(Constants.prefStopwatchRunning, StopwatchState.isRunning)
                broadcast.putExtra(Constants.prefStopwatchReset, StopwatchState.isReset)
                broadcast.putExtra(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)
                context.sendBroadcast(broadcast)
            }
        }

        if (action == null || action == Constants.timerQueryIntent || action == Constants.timerUpdateIntent) {
            if (!TimerState.isInitialized) {
                Log.e(TAG, "timer state not initialized, can't broadcast preferences")
            } else {
                Log.v(TAG, "broadcasting timer preferences")
                val broadcast = Intent(Constants.timerUpdateIntent)
                broadcast.putExtra(Constants.prefTimerStartTime, TimerState.startTime)
                broadcast.putExtra(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
                broadcast.putExtra(Constants.prefTimerDuration, TimerState.duration)
                broadcast.putExtra(Constants.prefTimerRunning, TimerState.isRunning)
                broadcast.putExtra(Constants.prefTimerReset, TimerState.isReset)
                broadcast.putExtra(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)
                context.sendBroadcast(broadcast)
            }
        }
    }

    fun loadPreferences(context: Context) {
        Log.v(TAG, "loadPreferences")

        // brackets just so that the variables go away when we leave scope
        run {
            val prefs = context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE)

            val priorTime = prefs.getLong(Constants.prefStopwatchBaseTime, 0L)
            val startTime = prefs.getLong(Constants.prefStopwatchStartTime, 0L)
            val isRunning = prefs.getBoolean(Constants.prefStopwatchRunning, false)
            val isReset = prefs.getBoolean(Constants.prefStopwatchReset, true)
            val updateTimestamp = prefs.getLong(Constants.prefStopwatchUpdateTimestamp, 0L)

            Log.v(TAG, "Stopwatch:: startTime($startTime), priorTime($priorTime), isRunning($isRunning), isReset($isReset), updateTimestamp($updateTimestamp)")

            StopwatchState.restoreState(priorTime, startTime, isRunning, isReset, updateTimestamp)
        }

        run {
            val prefs = context.getSharedPreferences(Constants.sharedPrefsTimer, Context.MODE_PRIVATE)

            val startTime = prefs.getLong(Constants.prefTimerStartTime, 0L)
            val pauseDelta = prefs.getLong(Constants.prefTimerPauseElapsed, 0L)
            val duration = prefs.getLong(Constants.prefTimerDuration, 0L)
            var isRunning = prefs.getBoolean(Constants.prefTimerRunning, false)
            var isReset = prefs.getBoolean(Constants.prefTimerReset, true)
            val updateTimestamp = prefs.getLong(Constants.prefTimerUpdateTimestamp, 0L)

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
