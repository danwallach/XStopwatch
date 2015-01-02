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

        if(intent.getAction().equals(StopwatchNotificationHelper.ACTION_NOTIFICATION_CLICK)) {
            Log.v(TAG, "remote click");
            StopwatchState.getSingleton().click();
            return;
        }

        if(intent.getAction().equals(Constants.stopwatchQueryIntent)) {
            Log.v(TAG, "remote query!");
            PreferencesHelper.savePreferences(context); // triggers a broadcast
        }
    }
}
