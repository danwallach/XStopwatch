package org.dwallach.xstopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by dwallach on 12/28/14.
 */
public class StopwatchText extends SurfaceView implements Observer {
    private final static String TAG = "StopwatchText";

    private boolean isVisible = true;
    private StopwatchState state = StopwatchState.getSingleton();
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

        state.addObserver(this);
    }

    public StopwatchText(Context context) {
        super(context);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        isVisible = (visibility == VISIBLE);

        Log.v(TAG, "visible: " + isVisible);

        if(isVisible) invalidate();
    }

    private float textX, textY;

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        Log.v(TAG, "size change: " + w + ", " + h);
        this.width = w;
        this.height = h;
        int textSize = (h*3)/5;

        Log.v(TAG, "new text size: " + textSize);

        textPaint.setTextSize(textSize);
        //
        // note: metrics.ascent is a *negative* number while metrics.descent is a *positive* number
        //
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        textY = -metrics.ascent;
        textX = w/2;
    }

    private int width, height, drawCounter;


    @Override
    public void onDraw(Canvas canvas) {
//        Log.v(TAG, "onDraw -- visible: " + isVisible + ", running: " + isRunning);
        drawCounter++;

        long priorTime = state.getPriorTime();
        long startTime = state.getStartTime();
        long currentTime = StopwatchState.currentTime();

        String result;
        if(state.isReset()) {
            result = zeroString;
        } else if (!state.isRunning()) {
            result = timeString(priorTime);
        } else {
            long timeNow = currentTime;
            result = timeString(timeNow - startTime + priorTime);
        }

//        Log.v(TAG, "update text to: " + result);

        if(width == 0 || height == 0) {
            if(drawCounter % 1000 == 1)
                Log.e(TAG, "zero-width or zero-height, can't draw yet");
            return;
        }

        // clear the screen
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawText(result, textX, textY, textPaint);

        if(isVisible & state.isRunning()) {
            invalidate();
        }
    }

    private static final String zeroString = "00:00:00.00";

    private String drawString = zeroString;


    private String timeString(long deltaTime) {
        int cent = (int)((deltaTime /     10L) % 100L);
        int sec = (int)((deltaTime /    1000L) % 60L);
        int min = (int)((deltaTime /   60000L) % 60L);
        int hrs = (int)((deltaTime / 3600000L) % 100L); // wrap to two digits

        return String.format("%02d:%02d:%02d.%02d", hrs, min, sec, cent);
    }

    @Override
    public void update(Observable observable, Object data) {
        // something changed in the StopwatchState...
        invalidate();
    }
}
