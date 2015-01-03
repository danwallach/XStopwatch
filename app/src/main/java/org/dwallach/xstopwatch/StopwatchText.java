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

    private boolean visible = true;
    private SharedState state;
    Paint textPaint;

    public StopwatchText(Context context, AttributeSet attrs, SharedState state) {
        super(context, attrs);

        this.state = state;

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

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        visible = (visibility == VISIBLE);

        Log.v(TAG, "visible: " + visible);

        state.setVisible(visible);
        if(visible) invalidate();
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
//        Log.v(TAG, "onDraw -- visible: " + visible + ", running: " + isRunning);
        drawCounter++;

        String result = state.currentTimeString(true);

//        Log.v(TAG, "update text to: " + result);

        if(width == 0 || height == 0) {
            if(drawCounter % 1000 == 1)
                Log.e(TAG, "zero-width or zero-height, can't draw yet");
            return;
        }

        // clear the screen
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawText(result, textX, textY, textPaint);

        if(visible & state.isRunning()) {
            invalidate();
        }
    }


    @Override
    public void update(Observable observable, Object data) {
        // something changed in the StopwatchState...
        Log.v(TAG, "update: invalidating text");
        invalidate();
    }
}
