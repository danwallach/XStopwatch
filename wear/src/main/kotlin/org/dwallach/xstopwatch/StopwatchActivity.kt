/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton

import java.util.Observable
import java.util.Observer

import kotlinx.android.synthetic.main.activity_stopwatch.*
import org.jetbrains.anko.*

class StopwatchActivity : Activity(), Observer {

    private var resetButton: ImageButton? = null
    private var playButton: ImageButton? = null
    private var notificationHelper: NotificationHelper? = null
    private var stopwatchText: StopwatchText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v(TAG, "onCreate")

        try {
            val pinfo = packageManager.getPackageInfo(packageName, 0)
            val versionNumber = pinfo.versionCode
            val versionName = pinfo.versionName

            Log.i(TAG, "Version: $versionName ($versionNumber)")

        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "couldn't read version", e)
        }

        intent.log(TAG) // dumps info from the intent into the log

        // if the user said "OK Google, start stopwatch", then this is how we can tell
        if(intent.action == "com.google.android.wearable.action.STOPWATCH") {
            Log.v(TAG, "user voice action detected: starting the stopwatch")
            StopwatchState.run(this@StopwatchActivity)
            PreferencesHelper.savePreferences(this@StopwatchActivity)
            PreferencesHelper.broadcastPreferences(this@StopwatchActivity, Constants.stopwatchUpdateIntent)
        }

        setContentView(R.layout.activity_stopwatch)

        watch_view_stub.setOnLayoutInflatedListener {
            Log.v(TAG, "onLayoutInflated")

            // note to the Kotlin reader: it would have been preferable to use the Kotlin "synthetic"
            // support to avoid this whole findViewById nonsense, as we did above for watch_view_stub,
            // but since we've got two different layouts (round and square), the synthetic support
            // isn't smart enough to let us do the right thing. So instead, we get the old-school version.

            resetButton = it.findViewById(R.id.resetButton) as ImageButton
            playButton = it.findViewById(R.id.playButton) as ImageButton
            stopwatchText = it.findViewById(R.id.elapsedTime) as StopwatchText
            stopwatchText?.setSharedState(StopwatchState)

            // bring in saved preferences
            PreferencesHelper.loadPreferences(this@StopwatchActivity)

            // now that we've loaded the state, we know whether we're playing or paused
            setPlayButtonIcon()

            // set up notification helper, and use this as a proxy for whether
            // or not we need to set up everybody who pays attention to the stopwatchState
            if (notificationHelper == null) {
                notificationHelper = NotificationHelper(this@StopwatchActivity,
                        R.drawable.stopwatch_trans,
                        resources.getString(R.string.stopwatch_app_name),
                        StopwatchState)

                setStopwatchObservers(true)
            }

            // get the notification service running as well; it will stick around to make sure
            // the broadcast receiver is alive
            NotificationService.kickStart(this@StopwatchActivity)

            resetButton?.setOnClickListener {
                StopwatchState.reset(this@StopwatchActivity)
                PreferencesHelper.savePreferences(this@StopwatchActivity)
                PreferencesHelper.broadcastPreferences(this@StopwatchActivity, Constants.stopwatchUpdateIntent)
            }

            playButton?.setOnClickListener {
                StopwatchState.click(this@StopwatchActivity)
                PreferencesHelper.savePreferences(this@StopwatchActivity)
                PreferencesHelper.broadcastPreferences(this@StopwatchActivity, Constants.stopwatchUpdateIntent)
            }
        }
    }

    // call to this specified in the layout xml files
    fun launchTimer(view: View) = startActivity<TimerActivity>()

    /**
     * install the observers that care about the stopwatchState: "this", which updates the
     * visible UI parts of the activity, and the notificationHelper, which deals with the popup
     * notifications elsewhere

     * @param includeActivity If the current activity isn't visible, then make this false and it won't be notified
     */
    private fun setStopwatchObservers(includeActivity: Boolean) {
        StopwatchState.deleteObservers()
        if (notificationHelper != null)
            StopwatchState.addObserver(notificationHelper)
        if (includeActivity) {
            StopwatchState.addObserver(this)

            if (stopwatchText != null)
                StopwatchState.addObserver(stopwatchText)
        }
    }

    override fun onStart() {
        super.onStart()

        Log.v(TAG, "onStart")

        StopwatchState.isVisible = true
        setStopwatchObservers(true)
    }

    override fun onResume() {
        super.onResume()

        Log.v(TAG, "onResume")

        StopwatchState.isVisible = true
        setStopwatchObservers(true)
    }

    override fun onPause() {
        super.onPause()

        Log.v(TAG, "onPause")

        StopwatchState.isVisible = false
        setStopwatchObservers(false)
    }

    override fun update(observable: Observable?, data: Any?) {
        Log.v(TAG, "activity update")
        setPlayButtonIcon()
    }

    private fun setPlayButtonIcon() =
        playButton?.setImageResource(
                if(StopwatchState.isRunning)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play)

    companion object {
        private val TAG = "StopwatchActivity"
    }
}

