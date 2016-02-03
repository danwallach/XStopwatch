package org.dwallach.xstopwatch

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.AlarmClock
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TimePicker

import java.lang.ref.WeakReference
import java.util.Observable
import java.util.Observer

import kotlinx.android.synthetic.main.activity_timer.*

class TimerActivity : Activity(), Observer {
    private var notificationHelper: NotificationHelper? = null
    private var buttonStateHandler: Handler? = null
    private var playButton: ImageButton? = null
    private var resetButton: ImageButton? = null
    private var stopwatchText: StopwatchText? = null

    class MyHandler(looper: Looper, timerActivity: TimerActivity) : Handler(looper) {
        private val timerActivityRef = WeakReference(timerActivity)

        override fun handleMessage(inputMessage: Message) {
            Log.v(TAG, "button state message received")
            timerActivityRef.get()?.setPlayButtonIcon()
        }
    }


    // see http://developer.android.com/guide/topics/ui/controls/pickers.html

    /**
     * this uses the built-in TimePickerDialog to ask the user to specify the hours and minutes
     * for the count-down timer. Of course, it works fine on the emulator and on a Moto360, but
     * totally fails on the LG G Watch and G Watch R, apparently trying to show a full-blown
     * Material Design awesome thing that was never tuned to fit on a watch. Instead, see
     * the separate TimePickerFragment class, which might be ugly, but at least it works consistently.

     * TODO: move back to this code and kill TimePickerFragment once they fix the bug in Wear
     */
    class FailedTimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker
            val duration = TimerState.duration // in milliseconds
            val minute = (duration / 60000 % 60).toInt()
            val hour = (duration / 3600000).toInt()

            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity, R.style.Theme_Wearable_Modal, this, hour, minute, true)
        }

        override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
            // Do something with the time chosen by the user
            Log.v(TAG, "User selected time: %d:%02d".format(hour, minute))
            TimerState.setDuration(null, hour * 3600000L + minute * 60000L)
        }
    }

    // call to this specified in the layout xml files
    fun showTimePickerDialog(v: View) =
        TimePickerFragment.newInstance().show(fragmentManager, "timePicker")
//        FailedTimePickerFragment().show(fragmentManager, "timePicker")

    // call to this specified in the layout xml files
    fun launchStopwatch(view: View) =
        startActivity(Intent(this, StopwatchActivity::class.java))

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

        // there's a chance we were launched through a specific intent to set a timer for
        // a particular length; this is how we figure it out
        val intent = intent
        val action = intent.action
        val paramLength = intent.getIntExtra(AlarmClock.EXTRA_LENGTH, 0)
        val skipUI = intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false)

        Log.v(TAG, "intent action: $action, length($paramLength)")

        val allExtras = intent.extras
        if (allExtras != null) {
            val keySet = allExtras.keySet()

            // because we're trying to figure out what's actually in here
            for (key in keySet) {
                try {
                    Log.v(TAG, "--- found extra: %s -> %s".format(key, allExtras.get(key).toString()))
                } catch (npe: NullPointerException) {
                    // rare chance of failure with get(key) above returning null; ignore
                    // and move on
                }

            }
        } else {
            Log.v(TAG, "--- no extras found!")
        }

        if (paramLength > 0 && paramLength <= 86400) {
            Log.v(TAG, "onCreate, somebody told us a time value: $paramLength")
            val durationMillis = (paramLength * 1000).toLong()
            TimerState.setDuration(this@TimerActivity, durationMillis)
            TimerState.reset(this@TimerActivity)
            if (skipUI)
                TimerState.click(this@TimerActivity)

            PreferencesHelper.savePreferences(this@TimerActivity)
            PreferencesHelper.broadcastPreferences(this@TimerActivity, Constants.timerUpdateIntent)
        } else {
            // bring in saved preferences
            PreferencesHelper.loadPreferences(this@TimerActivity)
        }


        setContentView(R.layout.activity_timer)

        // This buttonState business is all about dealing with alarms, which go to
        // NotificationService, on a different thread, which needs to ping us to
        // update the UI, if we exist. This handler will always run on the UI thread.
        // It's invoked from the update() method down below, which may run on other threads.
        buttonStateHandler = MyHandler(Looper.getMainLooper(), this)

        watch_view_stub.setOnLayoutInflatedListener {
            Log.v(TAG, "onLayoutInflated")

            // note to the Kotlin reader: it would have been preferable to use the Kotlin "synthetic"
            // support to avoid this whole findViewById nonsense, as we did above for watch_view_stub,
            // but since we've got two different layouts (round and square), the synthetic support
            // isn't smart enough to let us do the right thing. So instead, we get the old-school version.

            resetButton = it.findViewById(R.id.resetButton) as ImageButton
            playButton = it.findViewById(R.id.playButton) as ImageButton
            stopwatchText = it.findViewById(R.id.elapsedTime) as StopwatchText

            stopwatchText?.setSharedState(TimerState)

            // now that we've loaded the state, we know whether we're playing or paused
            setPlayButtonIcon()

            // get the notification service running as well; it will stick around to make sure
            // the broadcast receiver is alive
            NotificationService.kickStart(this@TimerActivity)

            // set up notification helper, and use this as a proxy for whether
            // or not we need to set up everybody who pays attention to the timerState
            if (notificationHelper == null) {
                notificationHelper = NotificationHelper(this@TimerActivity,
                        R.drawable.sandwatch_trans,
                        resources.getString(R.string.timer_app_name),
                        TimerState)
                setStopwatchObservers(true)
            }

            resetButton?.setOnClickListener {
                TimerState.reset(this@TimerActivity)
                PreferencesHelper.savePreferences(this@TimerActivity)
                PreferencesHelper.broadcastPreferences(this@TimerActivity, Constants.timerUpdateIntent)
            }

            playButton?.setOnClickListener {
                TimerState.click(this@TimerActivity)
                PreferencesHelper.savePreferences(this@TimerActivity)
                PreferencesHelper.broadcastPreferences(this@TimerActivity, Constants.timerUpdateIntent)
            }
        }
    }

    /**
     * install the observers that care about the timerState: "this", which updates the
     * visible UI parts of the activity, and the notificationHelper, which deals with the popup
     * notifications elsewhere

     * @param includeActivity If the current activity isn't visible, then make this false and it won't be notified
     */
    private fun setStopwatchObservers(includeActivity: Boolean) {
        TimerState.deleteObservers()
        if (notificationHelper != null)
            TimerState.addObserver(notificationHelper)
        if (includeActivity) {
            TimerState.addObserver(this)

            if (stopwatchText != null)
                TimerState.addObserver(stopwatchText)
        }
    }

    override fun onStart() {
        super.onStart()

        Log.v(TAG, "onStart")

        TimerState.isVisible = true
        setStopwatchObservers(true)
    }

    override fun onResume() {
        super.onResume()

        Log.v(TAG, "onResume")

        TimerState.isVisible = true
        setStopwatchObservers(true)
    }

    override fun onPause() {
        super.onPause()

        Log.v(TAG, "onPause")

        TimerState.isVisible = false
        setStopwatchObservers(false)
    }

    override fun update(observable: Observable?, data: Any?) {
        Log.v(TAG, "activity update")

        // We might be called on the UI thread or on a service thread; we need to dispatch this
        // entirely on the UI thread, since we're ultimately going to be monkeying with the UI.
        // Thus this nonsense.
        buttonStateHandler?.sendEmptyMessage(0)
    }

    private fun setPlayButtonIcon() {
        Log.v(TAG, "setPlayButtonIcon")
        playButton?.setImageResource(
                if(TimerState.isRunning)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play)
    }

    companion object {
        private val TAG = "TimerActivity"
    }
}
