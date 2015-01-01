package org.dwallach.xstopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dwallach on 1/1/15.
 */
public class Receiver extends BroadcastReceiver {
    private final static String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "got intent: " + intent.toString());

        if(intent.getAction().equals(StopwatchNotificationHelper.ACTION_NOTIFICATION_CLICK)) {
            StopwatchState.getSingleton().click();
        }
    }
}
