/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.app.Activity
import android.content.Context
import android.text.format.DateUtils
import android.util.Log

import java.util.Observable

/**
 * We'll implement this abstract class for StopwatchState and TimerState.
 */
abstract class SharedState: Observable() {
    var isRunning: Boolean = false
        protected set
    var isReset: Boolean = true
        protected set
    var updateTimestamp: Long = 0 // when the last user interaction was
        protected set
    var isVisible: Boolean = false
        set(visible) {
            Log.v(TAG, "${shortName} visible: $visible")
            field = visible
            isInitialized = true

            makeUpdateTimestamp()
            pingObservers()
        }
    var isInitialized: Boolean = false
        protected set

    private fun makeUpdateTimestamp() {
        updateTimestamp = currentTime()
    }

    open fun reset(context: Context?) {
        Log.v(TAG, "${shortName} reset")
        isRunning = false
        isReset = true
        isInitialized = true

        makeUpdateTimestamp()
        pingObservers()
    }

    open fun run(context: Context) {
        Log.v(TAG, "${shortName} run")

        isReset = false
        isRunning = true
        isInitialized = true

        makeUpdateTimestamp()
        pingObservers()
    }

    open fun pause(context: Context) {
        Log.v(TAG, "${shortName} pause")

        isRunning = false
        isInitialized = true

        makeUpdateTimestamp()
        pingObservers()
    }

    fun click(context: Context) {
        Log.v(TAG, "${shortName} click")
        if (isRunning)
            pause(context)
        else
            run(context)
    }

    fun pingObservers() {
        // this incantation will make observers elsewhere aware that there's new content
        Log.v(TAG, "${shortName} pinging")
        setChanged()
        notifyObservers()
        clearChanged()
        Log.v(TAG, "${shortName} ping complete")
    }

    /**
     * Return the time of either when the stopwatch began or when the countdown ends.
     * IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
     * IF PAUSED, this time will be relative to zero and will be what should be displayed.
     * Make sure to call isRunning() to know how to interpret this result.
     * @return GMT time in milliseconds
     */
    abstract fun eventTime(): Long

    /**
     * This converts an absolute time, as returned by eventTime, to a relative time
     * that might be displayed
     */
    fun relativeTimeString(eventTime: Long): String =
    DateUtils.formatElapsedTime(
        if (isRunning)
            Math.abs(currentTime() - eventTime) / 1000
        else
            Math.abs(eventTime) / 1000)

    override fun toString() = relativeTimeString(eventTime())

    abstract val actionNotificationClickString: String

    abstract val notificationID: Int

    abstract val activity: Class<out Activity>

    abstract val iconID: Int

    abstract val shortName: String

    companion object {
        private val TAG = "SharedState"

        fun currentTime() = System.currentTimeMillis()
    }
}
