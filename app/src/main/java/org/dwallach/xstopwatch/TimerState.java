/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

public class TimerState extends SharedState {
    private final static String TAG = "TimerState";

    private long elapsedTime;  // if the timer's not running, this says how far we got (i.e., we're at startTime + elapsedTime, and 0 <= elapsedTime <= duration)
    private long startTime;  // when the timer started running
    private long duration;   // when the timer ends (i.e., the timer completes at startTime + duration, assuming it's running)

    private TimerState() {
        super();
        elapsedTime = 0;
        duration = 60000; // one minute
        startTime = 0;
    }

    private static TimerState singleton;

    public static TimerState getSingleton() {
        if(singleton == null)
            singleton = new TimerState();
        return singleton;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(Context context, long duration) {
        this.duration = duration;
        reset(context);
    }

    public long getStartTime() {
        return startTime;
    }

    public void reset(Context context) {
        Log.v(TAG, "reset");

        // don't overwrite duration -- that's a user setting
        elapsedTime = startTime = 0;

        super.reset(context);
        updateBuzzTimer(context);
    }

    public void run(Context context) {
        Log.v(TAG, "run");

        if(duration == 0) return; // don't do anything unless there's a non-zero duration

        if(isReset())
            startTime = currentTime();
        else {
            // we're resuming from a pause, so we need to shove up the start time
            long pauseTime = startTime + elapsedTime;
            startTime += currentTime() - pauseTime;
        }

        super.run(context);
        updateBuzzTimer(context);
    }

    public void pause(Context context) {
        Log.v(TAG, "pause");

        long pauseTime = currentTime();
        elapsedTime = (pauseTime - startTime);
        if(elapsedTime > duration) elapsedTime = duration;

        super.pause(context);
        updateBuzzTimer(context);
    }

    public void restoreState(Context context, long duration, long elapsedTime, long startTime, boolean running, boolean reset, long updateTimestamp) {
        Log.v(TAG, "restoring state");
        this.duration = duration;
        this.elapsedTime = elapsedTime;
        this.startTime = startTime;
        this.running = running;
        this.reset = reset;
        this.updateTimestamp = updateTimestamp;
        initialized = true;

        updateBuzzTimer(context);
        pingObservers();
    }

    @Override
    public long eventTime() {
        // IF RUNNING, this time will be consistent with System.currentTimeMillis(), i.e., in GMT.
        // IF PAUSED, this time will be relative to zero and will be what should be displayed.

        if(reset) return duration;
        if(!running) return duration - elapsedTime;

        // running
        return duration + startTime;

    }

    public static final String ACTION_NOTIFICATION_CLICK_STRING = "intent.action.notification.timer.click";

    public String getActionNotificationClickString() {
        return ACTION_NOTIFICATION_CLICK_STRING;
    }

    public int getNotificationID() {
        return 2;
    }

    public Class getActivity() {
        return TimerActivity.class;
    }

    public void setBuzzTimer(Context context) {
    }

    private void updateBuzzTimer(Context context) {
        if (isRunning()) {
            long timeNow = currentTime();
            long delayTime = duration - timeNow + startTime;
            if (delayTime > 0) {
                Log.v(TAG, "setting alarm: " + delayTime + " ms in the future");
                registerTimerCompleteAlarm(context, delayTime);
            } else {
                Log.v(TAG, "alarm in the past, not setting");
            }
        } else {
            Log.v(TAG, "removing alarm");
            clearTimerCompleteAlarm(context);
        }
    }

    private void registerTimerCompleteAlarm(Context context, long wakeupTime) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create intent that gets fired when the timer expires.
        Intent intent = new Intent(Constants.actionTimerComplete, null, context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule an alarm.
        alarm.setExact(AlarmManager.RTC_WAKEUP, wakeupTime, pendingIntent);
    }

    private void clearTimerCompleteAlarm(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(Constants.actionTimerComplete, null, context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(pendingIntent);
    }


    /**
     * Handler to vibrate when the timer hits zero
     */
    public void handleTimerComplete(Context context) {
        // four short buzzes within one second total time
        long vibratorPattern[] = {100, 200, 100, 200, 100, 200, 100, 200};

        Log.v(TAG, "buzzing!");
        reset(context); // timer state
        PreferencesHelper.savePreferences(context);
        PreferencesHelper.broadcastPreferences(context, Constants.timerUpdateIntent);

        Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        vibrator.vibrate(vibratorPattern, -1, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
    }

    public int getIconID() {
        return R.drawable.sandwatch_selected_400;
    }

    public String getShortName() {
        return "[Timer] ";
    }
}
