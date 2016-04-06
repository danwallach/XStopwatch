package org.dwallach.xstopwatch

import android.app.DialogFragment
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import org.jetbrains.anko.find

class TimePickerFragment : DialogFragment() {
    private var hours: Int
    private var minutes: Int

    /**
     * This is a totally cheesy time picker. We'd rather use the system one, but it doesn't work.
     */
    init {
        val duration = TimerState.duration // in milliseconds
        minutes = (duration / 60000 % 60).toInt()
        hours = (duration / 3600000).toInt()
//        arguments = Bundle().apply {
//            putInt(HOURS_PARAM, hours)
//            putInt(MINUTES_PARAM, minutes)
//        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if(inflater == null) {
            Log.e(TAG, "no inflater, can't do anything!")
            return null
        }

        // nuke the frame around the fragment
        setStyle(DialogFragment.STYLE_NO_FRAME, 0)

        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_time_picker, container, false)

        mainView.apply {
            val pickerOkButton = find<ImageButton>(R.id.pickerOkButton)
            val pickerResetButton = find<ImageButton>(R.id.pickerResetButton)
            val hoursPicker = find<NumberPicker>(R.id.hoursPicker)
            val minutesPicker = find<NumberPicker>(R.id.minutesPicker)

            hoursPicker.apply {
                setTextColor(-1)
                minValue = 0
                maxValue = 23
                wrapSelectorWheel = false
                value = hours
            }

            minutesPicker.apply {
                setTextColor(-1)
                minValue = 0
                maxValue = 59
                value = minutes
                wrapSelectorWheel = false
                setFormatter { "%02d".format(it) } // numbers less than ten will have a leading zero
            }

            pickerOkButton.setOnClickListener {
                hours = hoursPicker.value
                minutes = minutesPicker.value
                TimerState.setDuration(null, (hours * 3600000 + minutes * 60000).toLong()) // also resets the timer
                PreferencesHelper.savePreferences(context)
                PreferencesHelper.broadcastPreferences(context, Constants.timerUpdateIntent)

                // okay, we're done!
                dismiss()
            }

            pickerResetButton.setOnClickListener { dismiss() }
        }

        return mainView
    }

    companion object {
        private const val TAG = "TimePickerFragment"
//        const val HOURS_PARAM = "hours"
//        const val MINUTES_PARAM = "minutes"
    }
}

// this solution adapted from: http://stackoverflow.com/questions/18120840/numberpicker-textcolour
fun NumberPicker.setTextColor(color: Int) {
    val TAG = "NumberPicker.setTextColor"
    Log.v(TAG, "setting number picker color")

    val count = childCount
    for (i in 0..count - 1) {
        val child = getChildAt(i)
        if (child is EditText) {
            try {
                Log.v(TAG, "found an edit text field ($i)")
                val selectorWheelPaintField = javaClass.getDeclaredField("mSelectorWheelPaint")
                selectorWheelPaintField.isAccessible = true
                (selectorWheelPaintField.get(this) as Paint).color = color
                val oldColor = child.currentTextColor
                Log.v(TAG, "oldColor(%x), newColor(%x)".format(oldColor, color))
                child.setTextColor(color)
                invalidate()
                return
            } catch (e: NoSuchFieldException) {
                Log.w(TAG, e)
            } catch (e: IllegalAccessException) {
                Log.w(TAG, e)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, e)
            }
        }
    }
}
