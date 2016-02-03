/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.app.Activity
import android.content.Context
import android.util.Log

object StopwatchState: SharedState() {
    /**
     * extra time to add in (accounting for prior pause/restart cycles) -- analogous to the "base" time in android.widget.Chronometer
     */
    var priorTime: Long = 0
        private set

    /**
     * When the stopwatch started running
     */
    var startTime: Long = 0
        private set

    init {
        priorTime = 0
        startTime = 0
    }

    override fun reset(context: Context) {
        Log.v(TAG, "reset")
        priorTime = 0
        startTime = 0

        super.reset(context)
    }

    override fun run(context: Context) {
        Log.v(TAG, "run")

        startTime = SharedState.currentTime()

        super.run(context)
    }

    override fun pause(context: Context) {
        Log.v(TAG, "pause")

        val pauseTime = SharedState.currentTime()
        priorTime += pauseTime - startTime

        super.pause(context)
    }

    fun restoreState(priorTime: Long, startTime: Long, running: Boolean, reset: Boolean, updateTimestamp: Long) {
        Log.v(TAG, "restoring state")
        this.priorTime = priorTime
        this.startTime = startTime
        this.isRunning = running
        this.isReset = reset
        this.updateTimestamp = updateTimestamp
        this.isInitialized = true

        pingObservers()
    }

    override fun eventTime(): Long =
        // IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
        // IF PAUSED, this time will be relative to zero and will be what should be displayed.

        if (isRunning) {
            startTime - priorTime
        } else {
            priorTime
        }

    override val actionNotificationClickString: String
        get() = Constants.stopwatchStartStopIntent

    override val notificationID: Int
        get() = 1

    override val activity: Class<out Activity>
        get() = StopwatchActivity::class.java

    override val iconID: Int
        get() = R.drawable.stopwatch_selected_400

    override val shortName: String
        get() = "[Stopwatch] "

    private val TAG = "StopwatchState"
}
