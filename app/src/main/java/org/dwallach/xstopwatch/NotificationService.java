/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class NotificationService extends IntentService {

    public static final String TAG = "NotificationService";
    public NotificationHelper notificationHelper;

    public NotificationService() {
        super(TAG);
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
        if (Constants.actionTimerComplete.equals(action)) {
            // this came from the alarm when the timer hits zero
            notificationHelper.kill();
        } else if (Constants.timerQueryIntent.equals(action) || Constants.stopwatchQueryIntent.equals(action)) {
            // this came from outside our world; somebody did a startService() on us to ask for our status
            PreferencesHelper.broadcastPreferences(this, action);
        } else {
            throw new IllegalStateException("Undefined constant used: " + action);
        }
    }
}
