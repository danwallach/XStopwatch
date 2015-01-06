/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

/**
 * This class acts something like android.widget.Chronometer, but that class only knows
 * how to count up, and we need to be able to go up (for a stopwatch) and down (for a timer).
 *
 * When running, the text is updated once a second, with text derived from the SharedState
 * (which might be either StopwatchState or TimerState).
 */
public class StopwatchText extends SurfaceView implements Observer {
    private final static String TAG = "StopwatchText";

    private boolean visible = true;
    private SharedState state;
    private String shortName;
    Paint textPaint;

    public StopwatchText(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        textPaint = new Paint(Paint.SUBPIXEL_TEXT_FLAG | Paint.HINTING_ON);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.MONOSPACE);
    }

    public StopwatchText(Context context) {
        super(context);
    }

    static final int MSG_UPDATE_TIME = 0;

    /** Handler to update the time once a second in interactive mode. */
    private final Handler updateTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_UPDATE_TIME:
                    invalidate();
                    if (visible && state.isRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = 1000 - (timeMs % 1000);
                        updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                    } else {
                        Log.v(TAG, shortName + "time handler complete");
                    }
                    break;
            }
        }
    };


    public void setSharedState(SharedState sharedState) {
        this.state = sharedState;
        this.shortName = sharedState.getShortName();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        visible = (visibility == VISIBLE);

        Log.v(TAG, shortName + "visible: " + visible);

        if(state != null)
            state.setVisible(visible);

        if(visible) {
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME); // now, rather than later
        } else {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        }
    }

    private float textX, textY;

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        Log.v(TAG, shortName + "size change: " + w + ", " + h);
        this.width = w;
        this.height = h;
        int textSize = (h*3)/5;

        Log.v(TAG, shortName + "new text size: " + textSize);

        textPaint.setTextSize(textSize);
        //
        // note: metrics.ascent is a *negative* number while metrics.descent is a *positive* number
        //
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        textY = -metrics.ascent;
        textX = w/2;

        //
        // In some weird cases, we get an onSizeChanged but not an onVisibilityChanged
        // event, even though visibility did change; Lovely.
        //
        onVisibilityChanged(null, VISIBLE);
    }

    private int width, height;


    @Override
    public void onDraw(Canvas canvas) {
//        Log.v(TAG, shortName + "onDraw -- visible: " + visible + ", running: " + state.isRunning());

        if(state == null) {
            Log.e(TAG, shortName + "onDraw: no state yet");
            return;
        }

        String result = state.toString();

//        Log.v(TAG, "update text to: " + result);

        if(width == 0 || height == 0) {
            Log.e(TAG, shortName + "zero-width or zero-height, can't draw yet");
            return;
        }

        // clear the screen
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        canvas.drawText(result, textX, textY, textPaint);
    }


    @Override
    public void update(Observable observable, Object data) {
        // something changed in the StopwatchState...
        Log.v(TAG, shortName + "update: invalidating text");
        updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME); // now, rather than later
    }
}
