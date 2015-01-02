package org.dwallach.xstopwatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
    private int appIcon;
    private String title;
    private Context context;
    private PendingIntent clickPendingIntent;
    private PendingIntent launchPendingIntent;

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

    public StopwatchNotificationHelper(Context context, int appIcon, String title) {
        this.context = context;
        this.appIcon = appIcon;
        this.title = title;
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

    public void notify(String timeString, boolean isRunning) {
        // Google docs for this:
        // http://developer.android.com/training/notify-user/build-notification.html

        // Also helpful but not enough:
        // http://mrigaen.blogspot.com/2014/03/the-all-important-setdeleteintent-for.html

        // This seems to explain what I want to do:
        // http://stackoverflow.com/questions/24494663/how-to-add-button-directly-on-notification-on-android-wear

        if(clickPendingIntent == null)
            clickPendingIntent =  PendingIntent.getBroadcast(context, 0, new Intent(ACTION_NOTIFICATION_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);

        if(launchPendingIntent == null)
            launchPendingIntent = PendingIntent.getActivity(context, 1, new Intent(context, StopwatchActivity.class), 0);

        int playPauseIcon = (isRunning) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        Resources resources = context.getResources();
        int accentColor = resources.getColor(R.color.accent);

        Notification notification =
                new Notification.Builder(context)
//                        .setStyle(new Notification.MediaStyle())   // this doesn't do anything, which is too bad; trying to add some style to the buttons
                        .setOngoing(true)
                        .setSmallIcon(appIcon)
                        .setContentTitle(timeString)
//                        .setContentTitle(title)
//                        .setContentText(timeString)
//                        .setColor(accentColor)                     // this doesn't do anything either; doesn't appear to be any way to customize the colors!
                        .addAction(playPauseIcon, timeString, clickPendingIntent)
                        .addAction(appIcon, title, launchPendingIntent)
                        .extend(new Notification.WearableExtender()
                                                    .setHintHideIcon(true)
                                                    .setContentAction(0))
                .build();


        // launch the notification
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
