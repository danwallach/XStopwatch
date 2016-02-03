/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View

import java.lang.ref.WeakReference
import java.util.Observable
import java.util.Observer

/**
 * This class acts something like android.widget.Chronometer, but that class only knows
 * how to count up, and we need to be able to go up (for a stopwatch) and down (for a timer).

 * When running, the text is updated once a second, with text derived from the SharedState
 * (which might be either StopwatchState or TimerState).
 */
class StopwatchText : View, Observer {
    private var visible = true
    private var state: SharedState? = null
    private var shortName: String? = null
    private val textPaint: Paint

    /** Handler to update the time once a second in interactive mode. */
    private val updateTimeHandler: MyHandler

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    init {
        setWillNotDraw(false)

        textPaint = Paint(Paint.SUBPIXEL_TEXT_FLAG or Paint.HINTING_ON)
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.setTypeface(Typeface.MONOSPACE)

        updateTimeHandler = MyHandler(this)
    }


    class MyHandler internal constructor(stopwatchText: StopwatchText) : Handler() {
        private val stopwatchTextRef = WeakReference(stopwatchText)

        override fun handleMessage(message: Message) {
            val stopwatchText = stopwatchTextRef.get() ?: return
            // oops, it died

            when (message.what) {
                MSG_UPDATE_TIME -> {
                    stopwatchText.invalidate()
                    if (stopwatchText.visible && (stopwatchText.state?.isRunning ?: false)) {
                        val timeMs = System.currentTimeMillis()
                        val delayMs = 1000 - timeMs % 1000
                        sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
                    } else {
                        Log.v(TAG, "${stopwatchText.shortName}: time handler complete")
                    }
                }
                else -> {
                    Log.e(TAG, "unknown message: ${message}")
                }
            }
        }
    }

    fun setSharedState(sharedState: SharedState) {
        this.state = sharedState
        this.shortName = sharedState.shortName
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        visible = visibility == View.VISIBLE

        Log.v(TAG, "${shortName} visible: " + visible)

        state?.isVisible = visible

        if (visible) {
            updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME) // now, rather than later
        } else {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
        }
    }

    private var textX: Float = 0.toFloat()
    private var textY: Float = 0.toFloat()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.v(TAG, "${shortName} size change: ${w},${h}")
        this._width = w
        this._height = h
        val textSize = h * 3 / 5

        Log.v(TAG, "${shortName} new text size: ${textSize}")

        textPaint.textSize = textSize.toFloat()
        //
        // note: metrics.ascent is a *negative* number while metrics.descent is a *positive* number
        //
        val metrics = textPaint.fontMetrics
        textY = -metrics.ascent
        textX = (w / 2).toFloat()

        //
        // In some weird cases, we get an onSizeChanged but not an onVisibilityChanged
        // event, even though visibility did change; Lovely.
        //
        onVisibilityChanged(null, View.VISIBLE)
    }

    private var _width: Int = 0
    private var _height: Int = 0


    public override fun onDraw(canvas: Canvas) {
        //        Log.v(TAG, shortName + "onDraw -- visible: " + visible + ", running: " + state.isRunning());

        if (state == null) {
            Log.e(TAG, "${shortName} onDraw: no state yet")
            return
        }

        val result = state.toString()

        //        Log.v(TAG, "update text to: " + result);

        if (_width == 0 || _height == 0) {
            Log.e(TAG, "${shortName} zero-width or zero-height, can't draw yet")
            return
        }

        // clear the screen
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawText(result, textX, textY, textPaint)
    }

    override fun update(observable: Observable?, data: Any?) {
        // something changed in the StopwatchState...
        Log.v(TAG, "${shortName} update: invalidating text")
        updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
        updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME) // now, rather than later
    }

    companion object {
        private val TAG = "StopwatchText"

        const val MSG_UPDATE_TIME = 0
    }
}
