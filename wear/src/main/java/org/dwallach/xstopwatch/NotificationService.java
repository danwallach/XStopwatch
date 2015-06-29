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

        String action = intent.getAction();
        if (Intent.ACTION_DEFAULT.equals(action)) {
            // we got launched by the kickstarter (see below)
            Log.v(TAG, "kickstart launch!");
        } else if (Constants.actionTimerComplete.equals(action)) {
            // The timer completed and we got launched and/or woken back up again.
            // To make this more complicated, we're going to be on a different thread
            // from the UI thread. Right now, this came down to a bug in the update()
            // method in TimerActivity(), which tried to set the icon on the play/pause
            // button. Solution over there: play games with handlers. If the bug arises
            // elsewhere, similar effort will be necessary.

            TimerState.getSingleton().handleTimerComplete(this);
        } else if (Constants.timerQueryIntent.equals(action) || Constants.stopwatchQueryIntent.equals(action)) {
            // We're making this an externally facing service, in case somebody wants to launch us to announce
            // the stopwatch or timer status. This shouldn't actually be necessary. If the user starts
            // the stopwatch or timer, then the service will be persistent (or as persistent as Android
            // Wear is willing to be when somebody calls startService()) and will respond to broadcast
            // intents that don't particularly target us.
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


    /**
     * Start the notification service, if it's not already running. This is the service
     * that waits for alarms, when a timer runs out. By having it running, we'll also
     * be around for broadcast intents, supporting all the communication goodness in
     * Receiver
     */
    public static void kickStart(Context ctx) {
        NotificationService service = NotificationService.getSingletonService();

        if(service == null) {
            Log.v(TAG, "launching watch calendar service");
            Intent serviceIntent = new Intent(ctx, NotificationService.class);
            serviceIntent.setAction(Intent.ACTION_DEFAULT);
            ctx.startService(serviceIntent);
        }
    }
}
