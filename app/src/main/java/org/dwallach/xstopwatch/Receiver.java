/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    private final static String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "got intent: " + intent.toString());

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // If the service isn't running, set it up so we'll be around to receive other messages.
            // (We're registering for a boot notification. If that works on Wear, this is where we'll
            // find out about it.)
            NotificationService.kickStart(context);
            return;
        }

        if(action.equals(StopwatchState.ACTION_NOTIFICATION_CLICK_STRING)) {
            Log.v(TAG, "stopwatch remote click");
            StopwatchState.getSingleton().click(context);
            PreferencesHelper.savePreferences(context);
            PreferencesHelper.broadcastPreferences(context, Constants.stopwatchQueryIntent);
            return;
        }

        if(action.equals(TimerState.ACTION_NOTIFICATION_CLICK_STRING)) {
            Log.v(TAG, "timer remote click");
            TimerState.getSingleton().click(context);
            PreferencesHelper.savePreferences(context);
            PreferencesHelper.broadcastPreferences(context, Constants.timerQueryIntent);
            return;
        }

        if(action.equals(Constants.stopwatchQueryIntent) || action.equals(Constants.timerQueryIntent)) {
            Log.v(TAG, "remote query!");
            PreferencesHelper.broadcastPreferences(context, action);
            return;
        }
    }
}
