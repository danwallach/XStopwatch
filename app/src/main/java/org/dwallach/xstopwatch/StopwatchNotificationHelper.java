package org.dwallach.xstopwatch;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by dwallach on 12/31/14.
 */
public class StopwatchNotificationHelper implements Observer {
    private final static String TAG = "StopwatchNotificationHelper";

    private final int notificationID = 001;
    private int iconID;
    private String title;
    private Context context;
    private PendingIntent pendingIntent;

    public static final String ACTION_NOTIFICATION_CLICK = "intent.action.notification.stopwatch.click";
    private static final int MSG_UPDATE_TIME = 2;

    /**
     * Handler to tick once every second when the timer is running
     * and we need to show the notification.
     */
    private final Handler mUpdateTimeHandler = new Handler() {
        private int counter = 0;
        @Override
        public void handleMessage(Message message) {
            counter++;

            if(message.what == MSG_UPDATE_TIME) {
                if(counter % 60 == 1)
                    Log.v(TAG, "Time update (% 60)");
                update(StopwatchState.getSingleton(), null);
            } else {
                Log.e(TAG, "Unknown message: " + message.toString());
            }
        }
    };

    public StopwatchNotificationHelper(Context context, int iconID, String title) {
        this.context = context;
        this.iconID = iconID;
        this.title = title;
    }


    public void kill() {
        Log.v(TAG, "nuking any notifications");

        try {
            // Gets an instance of the NotificationManager service
            NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            notifyManager.cancel(notificationID);

            if(pendingIntent != null) {
                pendingIntent.cancel();
                pendingIntent = null;
            }

        } catch (Throwable throwable) {
            Log.e(TAG, "failed to cancel notifications", throwable);
        }
    }

    public void notify(String timeString, boolean isRunning) {
        // Google docs for this:
        // http://developer.android.com/training/notify-user/build-notification.html

        // Also helpful but not enough:
        // http://mrigaen.blogspot.com/2014/03/the-all-important-setdeleteintent-for.html

        // This seems to explain what I want to do:
        // http://stackoverflow.com/questions/24494663/how-to-add-button-directly-on-notification-on-android-wear

        if(pendingIntent == null)
            pendingIntent =  PendingIntent.getBroadcast(context, 0, new Intent(ACTION_NOTIFICATION_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);

        int myIcon = (isRunning) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        Notification notification =
                new Notification.Builder(context)
                        .setOngoing(true)
//                        .setAutoCancel(true)
//                        .setContentIntent(pendingIntent)
//                        .setDeleteIntent(pendingIntent)
                        .setSmallIcon(iconID)
//                        .setSmallIcon(myIcon)
                        .setContentTitle(timeString)
//                        .setContentText(timeString)
                        .addAction(myIcon, timeString, pendingIntent)
                        .extend(new Notification.WearableExtender()
                                .setContentAction(0))
                .build();


        // Gets an instance of the NotificationManager service
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notifyManager.notify(notificationID, notification);

        // if we're running, then we'll need to update the counter in a second, so we'll do
        // this delayed message thing
        if(isRunning) {
            long timeMs = System.currentTimeMillis();
            long delayMs = 1000 - (timeMs % 1000);
            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        StopwatchState stopwatchState = (StopwatchState) observable;

        if(stopwatchState.isVisible() || stopwatchState.isReset()) kill();
        else notify(stopwatchState.currentTimeString(false), stopwatchState.isRunning());
    }
}
