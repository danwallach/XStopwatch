/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Log

import java.util.Observable
import java.util.Observer

class NotificationHelper(private val context: Context, private val appIcon: Int, private val title: String, private val state: SharedState) : Observer {

    private val notificationID = state.notificationID
    private var clickPendingIntent: PendingIntent? = null
    private var launchPendingIntent: PendingIntent? = null

    init {
        // launch any notifications right away, if for example we just restarted and there's a
        // running stopwatch or timer
        update(state, null)
    }

    fun kill() {
        Log.v(TAG, "nuking any notifications")

        try {
            // Gets an instance of the NotificationManager service
            val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Builds the notification and issues it.
            notifyManager.cancel(notificationID)

            clickPendingIntent?.cancel()
            clickPendingIntent = null

            launchPendingIntent?.cancel()
            launchPendingIntent = null

        } catch (throwable: Throwable) {
            Log.e(TAG, "failed to cancel notifications", throwable)
        }

    }

    private fun initIntents() {
        if (clickPendingIntent == null)
            clickPendingIntent = PendingIntent.getBroadcast(context, 0, Intent(state.actionNotificationClickString), PendingIntent.FLAG_UPDATE_CURRENT)

        if (launchPendingIntent == null)
            launchPendingIntent = PendingIntent.getActivity(context, 1, Intent(context, state.activity), 0)
    }

    fun notify(eventTime: Long, isRunning: Boolean) {
        // Google docs for this:
        // http://developer.android.com/training/notify-user/build-notification.html

        // Also helpful but not enough:
        // http://mrigaen.blogspot.com/2014/03/the-all-important-setdeleteintent-for.html

        // This seems to explain what I want to do:
        // http://stackoverflow.com/questions/24494663/how-to-add-button-directly-on-notification-on-android-wear

        initIntents()

        val bg = BitmapFactory.decodeResource(context.resources, state.iconID)

        val builder = Notification.Builder(context)

        if (!isRunning) {
            builder.addAction(context, android.R.drawable.ic_media_play, "", clickPendingIntent)
                    .setContentTitle(state.toString())
                    .setContentText(title) // deliberately backwards for these two so the peek card has the important stuff above the fold
        } else {
            builder.addAction(context, android.R.drawable.ic_media_pause, "", clickPendingIntent)
                    .setWhen(eventTime)
                    .setUsesChronometer(true)
                    .setShowWhen(true)
        }

        val notification = builder
                .setOngoing(true)
                .setLocalOnly(true)
                .setSmallIcon(appIcon)
                .addAction(context, appIcon, title, launchPendingIntent)
                .extend(Notification.WearableExtender()
                        .setHintHideIcon(true)
                        .setContentAction(0)
                        //                        .setCustomSizePreset(Notification.WearableExtender.SIZE_LARGE)
                        .setBackground(bg))
                .build()

        // launch the notification
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(notificationID, notification)
    }

    override fun update(observable: Observable?, data: Any?) {
        //        Log.v(TAG, "updating notification state");
        if(observable != null) {
            val sharedState = observable as SharedState

            if (sharedState.isVisible || sharedState.isReset)
                kill()
            else
                notify(sharedState.eventTime(), sharedState.isRunning)
        }
    }

    companion object {
        private val TAG = "NotificationHelper"
    }
}

/**
 * The addAction builder that we want to use has been deprecated, "because reasons", so this brings
 * it back for us. Let's hear it for Kotlin extension methods!
 */
fun Notification.Builder.addAction(context: Context, iconId: Int, title: String, intent: PendingIntent?): Notification.Builder =
    if(intent == null)
        this
    else
        // The only real difference between this and the original Android method, from Notification.Builder,
        // is the addition of the Context argument, which isn't there in the original and seems necessary for the internal
        // call to Icon.createWithResources.

        this.addAction(
                Notification.Action.Builder(
                        Icon.createWithResource(context, iconId),
                        title, intent) .build() )
