package org.dwallach.xstopwatch;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesHelper {
    private final static String TAG = "PreferencesHelper";

    public static void savePreferences(Context context) {
        Log.v(TAG, "savePreferences");
        SharedPreferences prefs = context.getSharedPreferences("org.dwallach.xstopwatch.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StopwatchState stopwatchState = StopwatchState.getSingleton();

        editor.putLong("stopwatch.startTime", stopwatchState.getStartTime());
        editor.putLong("stopwatch.priorTime", stopwatchState.getPriorTime());
        editor.putBoolean("stopwatch.running", stopwatchState.isRunning());
        editor.putBoolean("stopwatch.reset", stopwatchState.isReset());

        if(!editor.commit())
            Log.v(TAG, "savePreferences commit failed ?!");
    }

    public static void loadPreferences(Context context) {
        Log.v(TAG, "loadPreferences");

        StopwatchState stopwatchState = StopwatchState.getSingleton();

        SharedPreferences prefs = context.getSharedPreferences("org.dwallach.xstopwatch.prefs", Context.MODE_PRIVATE);

        long priorTime = prefs.getLong(Constants.prefStopwatchPriorTime, 0L);
        long startTime = prefs.getLong(Constants.prefStopwatchStartTime, 0L);
        boolean isRunning = prefs.getBoolean(Constants.prefStopwatchRunning, false);
        boolean isReset = prefs.getBoolean(Constants.prefStopwatchReset, true);

        Log.v(TAG, "Stopwatch:: startTime(" + startTime + "), priorTime(" + priorTime + "), isRunning(" + isRunning + "), isReset(" + isReset + ")" );

        stopwatchState.restoreState(priorTime, startTime, isRunning, isReset);
    }
}
