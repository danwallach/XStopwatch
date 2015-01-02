package org.dwallach.xstopwatch;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesHelper {
    private final static String TAG = "PreferencesHelper";

    public static void savePreferences(Context context) {
        Log.v(TAG, "savePreferences");
        SharedPreferences prefs = context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StopwatchState stopwatchState = StopwatchState.getSingleton();

        editor.putLong(Constants.prefStopwatchStartTime, stopwatchState.getStartTime());
        editor.putLong(Constants.prefStopwatchPriorTime, stopwatchState.getPriorTime());
        editor.putBoolean(Constants.prefStopwatchRunning, stopwatchState.isRunning());
        editor.putBoolean(Constants.prefStopwatchReset, stopwatchState.isReset());

        if (!editor.commit())
            Log.v(TAG, "savePreferences commit failed ?!");

        broadcastPreferences(context);
    }

    public static void broadcastPreferences(Context context) {
        Log.v(TAG, "broadcastPreferences");
        StopwatchState stopwatchState = StopwatchState.getSingleton();

        // There's a chance that our app is not running but we received the broadcast intent
        // asking for our state to be sent out again. In that case, we'll need to load up
        // the preferences again. Otherwise we'll be sending out incorrect data.

        if(!stopwatchState.isInitialized())
            loadPreferences(context);

        Intent broadcast = new Intent(Constants.stopwatchUpdateIntent);
        broadcast.putExtra(Constants.prefStopwatchStartTime, stopwatchState.getStartTime());
        broadcast.putExtra(Constants.prefStopwatchPriorTime, stopwatchState.getPriorTime());
        broadcast.putExtra(Constants.prefStopwatchRunning, stopwatchState.isRunning());
        broadcast.putExtra(Constants.prefStopwatchReset, stopwatchState.isReset());
        context.sendBroadcast(broadcast);
    }

    public static void loadPreferences(Context context) {
        Log.v(TAG, "loadPreferences");

        StopwatchState stopwatchState = StopwatchState.getSingleton();

        SharedPreferences prefs = context.getSharedPreferences(Constants.sharedPrefsStopwatch, Context.MODE_PRIVATE);

        long priorTime = prefs.getLong(Constants.prefStopwatchPriorTime, 0L);
        long startTime = prefs.getLong(Constants.prefStopwatchStartTime, 0L);
        boolean isRunning = prefs.getBoolean(Constants.prefStopwatchRunning, false);
        boolean isReset = prefs.getBoolean(Constants.prefStopwatchReset, true);

        Log.v(TAG, "Stopwatch:: startTime(" + startTime + "), priorTime(" + priorTime + "), isRunning(" + isRunning + "), isReset(" + isReset + ")" );

        stopwatchState.restoreState(priorTime, startTime, isRunning, isReset);
    }
}
