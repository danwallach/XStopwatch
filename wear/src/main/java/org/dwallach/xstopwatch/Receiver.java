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
            // In manifest.xml, we register for a boot notification. If that works on Wear, this is where it
            // will arrive. The purpose of the notification is to start up our NotificationService. It's
            // job is to sit around waiting for queries from other apps for the state of the stopwatch
            // and timer.
            NotificationService.kickStart(context);
            return;
        }

        if(action.equals(Constants.stopwatchStartStopIntent)) {
            // When we display notifications, after the user swipes away the stopwatch or timer app,
            // those notifications are running in a completely separate process. They're loaded with
            // two different actions. One launches an activity back here again. The other one, which
            // we stick up front in the notification, does the play/pause action. When the user clicks
            // that button, it comes here. Then, all we do is internally record the click (which will
            // have other side effects like changing the notification). We save our state and send
            // out a new broadcast to anybody listening.
            Log.v(TAG, "stopwatch remote click");
            StopwatchState.getSingleton().click(context);
            PreferencesHelper.savePreferences(context);
            PreferencesHelper.broadcastPreferences(context, Constants.stopwatchQueryIntent);
            return;
        }

        if (action.equals(Constants.actionTimerComplete)) {
            // this isn't supposed to happen here: the timer is supposed to launch the service instead
            Log.v(TAG, "timer complete!");
            TimerState.getSingleton().handleTimerComplete(context);
            return;
        }

        if(action.equals(Constants.timerStartStopIntent)) {
            // See discussion above for StopwatchState. Same deal.
            Log.v(TAG, "timer remote click");
            TimerState.getSingleton().click(context);
            PreferencesHelper.savePreferences(context);
            PreferencesHelper.broadcastPreferences(context, Constants.timerQueryIntent);
            return;
        }

        if(action.equals(Constants.stopwatchQueryIntent) || action.equals(Constants.timerQueryIntent)) {
            // When another app wants to learn the state of the stopwatch or timer, it needs to do
            // two things. First, it needs to be listening for our broadcast intents. Second, if it's
            // newly running and it wants to get old state, it needs to ask for it. Those requests,
            // by sending broadcast intents that this app is looking for, arrive here.
            Log.v(TAG, "remote query!");
            PreferencesHelper.broadcastPreferences(context, action);
            return;
        }
    }
}
