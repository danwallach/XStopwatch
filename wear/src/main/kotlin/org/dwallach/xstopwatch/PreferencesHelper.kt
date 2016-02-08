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
        var editor: SharedPreferences.Editor = prefs.edit().let {
            it.putLong(Constants.prefStopwatchStartTime, StopwatchState.startTime)
            it.putLong(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
            it.putBoolean(Constants.prefStopwatchRunning, StopwatchState.isRunning)
            it.putBoolean(Constants.prefStopwatchReset, StopwatchState.isReset)
            it.putLong(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)
        }

        if (!editor.commit())
            Log.v(TAG, "savePreferences commit failed ?!")

        var prefs2 = context.getSharedPreferences(Constants.sharedPrefsTimer, Context.MODE_PRIVATE)
        var editor2 = prefs.edit().let {
            it.putLong(Constants.prefTimerStartTime, TimerState.startTime)
            it.putLong(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
            it.putLong(Constants.prefTimerDuration, TimerState.duration)
            it.putBoolean(Constants.prefTimerRunning, TimerState.isRunning)
            it.putBoolean(Constants.prefTimerReset, TimerState.isReset)
            it.putLong(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)
        }

        if (!editor2.commit())
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
                context.sendBroadcast(Intent(Constants.stopwatchUpdateIntent).let {
                    it.putExtra(Constants.prefStopwatchStartTime, StopwatchState.startTime)
                    it.putExtra(Constants.prefStopwatchBaseTime, StopwatchState.priorTime)
                    it.putExtra(Constants.prefStopwatchRunning, StopwatchState.isRunning)
                    it.putExtra(Constants.prefStopwatchReset, StopwatchState.isReset)
                    it.putExtra(Constants.prefStopwatchUpdateTimestamp, StopwatchState.updateTimestamp)
                })
            }
        }

        if (action == null || action == Constants.timerQueryIntent || action == Constants.timerUpdateIntent) {
            if (!TimerState.isInitialized) {
                Log.e(TAG, "timer state not initialized, can't broadcast preferences")
            } else {
                Log.v(TAG, "broadcasting timer preferences")
                context.sendBroadcast(Intent(Constants.timerUpdateIntent).let {
                    it.putExtra(Constants.prefTimerStartTime, TimerState.startTime)
                    it.putExtra(Constants.prefTimerPauseElapsed, TimerState.elapsedTime)
                    it.putExtra(Constants.prefTimerDuration, TimerState.duration)
                    it.putExtra(Constants.prefTimerRunning, TimerState.isRunning)
                    it.putExtra(Constants.prefTimerReset, TimerState.isReset)
                    it.putExtra(Constants.prefTimerUpdateTimestamp, TimerState.updateTimestamp)
                })
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
