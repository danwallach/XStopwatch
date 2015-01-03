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

        if(action.equals(StopwatchState.ACTION_NOTIFICATION_CLICK_STRING)) {
            Log.v(TAG, "stopwatch remote click");
            StopwatchState.getSingleton().click();
            PreferencesHelper.savePreferences(context);
            return;
        }

        if(action.equals(TimerState.ACTION_NOTIFICATION_CLICK_STRING)) {
            Log.v(TAG, "timer remote click");
            TimerState.getSingleton().click();
            PreferencesHelper.savePreferences(context);
            return;
        }

        if(action.equals(Constants.stopwatchQueryIntent) || action.equals(Constants.timerQueryIntent)) {
            Log.v(TAG, "remote query!");
            PreferencesHelper.broadcastPreferences(context);
            return;
        }
    }
}
