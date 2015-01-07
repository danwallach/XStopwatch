/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class NotificationHelper implements Observer {
    private final static String TAG = "NotificationHelper";

    private int notificationID;
    private int appIcon;
    private String title;
    private Context context;
    private PendingIntent clickPendingIntent;
    private PendingIntent launchPendingIntent;
    private SharedState state;

    public NotificationHelper(Context context, int appIcon, String title, SharedState state) {
        this.context = context;
        this.appIcon = appIcon;
        this.title = title;
        this.state = state;
        this.notificationID = state.getNotificationID();
    }

    public void kill() {
        Log.v(TAG, "nuking any notifications");

        try {
            // Gets an instance of the NotificationManager service
            NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            notifyManager.cancel(notificationID);

            if(clickPendingIntent != null) {
                clickPendingIntent.cancel();
                clickPendingIntent = null;
            }

            if(launchPendingIntent != null) {
                launchPendingIntent.cancel();
                launchPendingIntent = null;
            }

        } catch (Throwable throwable) {
            Log.e(TAG, "failed to cancel notifications", throwable);
        }
    }

    private void initIntents() {
        if(clickPendingIntent == null)
            clickPendingIntent =  PendingIntent.getBroadcast(context, 0, new Intent(state.getActionNotificationClickString()), PendingIntent.FLAG_UPDATE_CURRENT);

        if(launchPendingIntent == null)
            launchPendingIntent = PendingIntent.getActivity(context, 1, new Intent(context, state.getActivity()), 0);
    }

    public void notify(long eventTime, boolean isRunning) {
        // Google docs for this:
        // http://developer.android.com/training/notify-user/build-notification.html

        // Also helpful but not enough:
        // http://mrigaen.blogspot.com/2014/03/the-all-important-setdeleteintent-for.html

        // This seems to explain what I want to do:
        // http://stackoverflow.com/questions/24494663/how-to-add-button-directly-on-notification-on-android-wear

        initIntents();

        Bitmap bg = BitmapFactory.decodeResource(context.getResources(), state.getIconID());

        Notification.Builder builder = new Notification.Builder(context);

        if(!isRunning) {
            String timeString = state.toString();
            builder.addAction(android.R.drawable.ic_media_play, "", clickPendingIntent)
                    .setContentTitle(timeString)
                    .setContentText(title); // deliberately backwards for these two so the peek card has the important stuff above the fold
        }  else {
            builder.addAction(android.R.drawable.ic_media_pause, "", clickPendingIntent)
                    .setWhen(eventTime)
                    .setUsesChronometer(true)
                    .setShowWhen(true);
        }

        Notification notification = builder
                .setOngoing(true)
                .setLocalOnly(true)
                .setSmallIcon(appIcon)
                .addAction(appIcon, title, launchPendingIntent)
                .extend(new Notification.WearableExtender()
                        .setHintHideIcon(true)
                        .setContentAction(0)
                        .setCustomSizePreset(Notification.WearableExtender.SIZE_LARGE)
                        .setBackground(bg))
                .build();


        // launch the notification
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(notificationID, notification);
    }

    @Override
    public void update(Observable observable, Object data) {
//        Log.v(TAG, "updating notification state");
        SharedState sharedState = (SharedState) observable;

        if(sharedState.isVisible() || sharedState.isReset()) kill();
        else notify(sharedState.eventTime(), sharedState.isRunning());
    }
}
