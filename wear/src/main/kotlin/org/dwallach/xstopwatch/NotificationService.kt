/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log

import org.jetbrains.anko.*

class NotificationService : IntentService(NotificationService.TAG) {
    init {
        singletonService = this
    }

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate")
    }

    override fun onHandleIntent(intent: Intent) {
        Log.v(TAG, "onHandleIntent: ${intent.action}")

        if (!TimerState.isInitialized)
            PreferencesHelper.loadPreferences(this)

        val action = intent.action
        if (Intent.ACTION_DEFAULT == action) {
            // we got launched by the kickstarter (see below)
            Log.v(TAG, "kickstart launch!")
        } else if (Constants.actionTimerComplete == action) {
            // The timer completed and we got launched and/or woken back up again.
            // To make this more complicated, we're going to be on a different thread
            // from the UI thread. Right now, this came down to a bug in the update()
            // method in TimerActivity(), which tried to set the icon on the play/pause
            // button. Solution over there: play games with handlers. If the bug arises
            // elsewhere, similar effort will be necessary.

            TimerState.handleTimerComplete(this)
        } else if (Constants.timerQueryIntent == action || Constants.stopwatchQueryIntent == action) {
            // We're making this an externally facing service, in case somebody wants to launch us to announce
            // the stopwatch or timer status. This shouldn't actually be necessary. If the user starts
            // the stopwatch or timer, then the service will be persistent (or as persistent as Android
            // Wear is willing to be when somebody calls startService()) and will respond to broadcast
            // intents that don't particularly target us.

            Log.v(TAG, "broadcast request!")
            PreferencesHelper.broadcastPreferences(this, action)
        } else {
            throw IllegalStateException("Undefined constant used: $action")
        }
    }

    companion object {
        val TAG = "NotificationService"

        var singletonService: NotificationService? = null


        /**
         * Start the notification service, if it's not already running. This is the service
         * that waits for alarms, when a timer runs out. By having it running, we'll also
         * be around for broadcast intents, supporting all the communication goodness in
         * Receiver
         */
        fun kickStart(ctx: Context) {
            if (singletonService == null) {
                Log.v(TAG, "launching watch calendar service")
                // Anko alternative to the three lines below
                ctx.startService(ctx.intentFor<NotificationService>().setAction(Intent.ACTION_DEFAULT))
//                val serviceIntent = Intent(ctx, NotificationService::class.java)
//                serviceIntent.setAction(Intent.ACTION_DEFAULT)
//                ctx.startService(serviceIntent)
            }
        }
    }
}
