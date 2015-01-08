/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationService extends IntentService {
    public static final String TAG = "NotificationService";
    public NotificationHelper notificationHelper;

    public NotificationService() {
        super(TAG);
        singleton = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent: " + intent.getAction());

        TimerState timerState = TimerState.getSingleton();

        if(!timerState.isInitialized())
            PreferencesHelper.loadPreferences(this);

        if(notificationHelper == null) {
            notificationHelper = TimerActivity.getNewNotificationHelper(this);
        }

        String action = intent.getAction();
        if (Intent.ACTION_DEFAULT.equals(action)) {
            // we got launched by the kickstarter
            Log.v(TAG, "kickstart launch!");
        } else if (Constants.actionTimerComplete.equals(action)) {
            // this came from the alarm when the timer hits zero
            Log.v(TAG, "timer complete!");
            notificationHelper.kill();
        } else if (Constants.timerQueryIntent.equals(action) || Constants.stopwatchQueryIntent.equals(action)) {
            // We're making this an externally facing service, in case somebody wants to launch us to announce
            // the stopwatch or timer status. This shouldn't actually be necessary. If the user starts
            // the stopwatch or timer, then the service will be persistent (or as persistent as Android
            // Wear is willing to be when somebody calls startService()) and will respond to broadcast
            // intents.
            Log.v(TAG, "broadcast request!");
            PreferencesHelper.broadcastPreferences(this, action);
        } else {
            throw new IllegalStateException("Undefined constant used: " + action);
        }
    }

    private static NotificationService singleton;

    public static NotificationService getSingletonService() {
        return singleton;
    }


    public static void kickStart(Context ctx) {
        // start the calendar service, if it's not already running
        NotificationService service = NotificationService.getSingletonService();

        if(service == null) {
            Log.v(TAG, "launching watch calendar service");
            Intent serviceIntent = new Intent(ctx, NotificationService.class);
            serviceIntent.setAction(Intent.ACTION_DEFAULT);
            ctx.startService(serviceIntent);
        }
    }
}
