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

class TimePickerFragment private constructor() : DialogFragment() {
    private var hours: Int = 0
    private var minutes: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        // nuke the frame around the fragment
        setStyle(DialogFragment.STYLE_NO_FRAME, 0)

        // Inflate the layout for this fragment
        val mainView = inflater.inflate(R.layout.fragment_time_picker, container, false)


        if (arguments != null) {
            hours = arguments.getInt(HOURS_PARAM)
            minutes = arguments.getInt(MINUTES_PARAM)
        }
        val okButton = mainView.findViewById(R.id.pickerOkButton) as ImageButton
        val resetButton = mainView.findViewById(R.id.pickerResetButton) as ImageButton
        val hoursPicker = mainView.findViewById(R.id.hoursPicker) as NumberPicker
        val minutesPicker = mainView.findViewById(R.id.minutesPicker) as NumberPicker

        // it's a kludge: not allowed in the styles anywhere so we have to do this awful thing instead
        setNumberPickerTextColor(hoursPicker, -1)
        setNumberPickerTextColor(minutesPicker, -1)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 23
        hoursPicker.wrapSelectorWheel = false
        hoursPicker.value = hours
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.value = minutes
        minutesPicker.wrapSelectorWheel = false
        minutesPicker.setFormatter { "%02d".format(it) } // numbers less than ten will have a leading zero

        okButton.setOnClickListener {
            hours = hoursPicker.value
            minutes = minutesPicker.value
            TimerState.setDuration(null, (hours * 3600000 + minutes * 60000).toLong())

            // okay, we're done!
            dismiss()
        }

        resetButton.setOnClickListener { dismiss() }

        return mainView
    }

    companion object {
        private val TAG = "TimePickerFragment"

        const val HOURS_PARAM = "hours"
        const val MINUTES_PARAM = "minutes"

        /**
         * This is a totally cheesy time picker. We'd rather use the system one, but it doesn't work.
         */
        fun newInstance(): TimePickerFragment {
            val fragment = TimePickerFragment()
            val args = Bundle()
            val duration = TimerState.duration // in milliseconds
            val minutes = (duration / 60000 % 60).toInt()
            val hours = (duration / 3600000).toInt()
            args.putInt(HOURS_PARAM, hours)
            args.putInt(MINUTES_PARAM, minutes)
            fragment.arguments = args
            return fragment
        }

        // this solution adapted from: http://stackoverflow.com/questions/18120840/numberpicker-textcolour
        fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
            Log.v(TAG, "setting number picker color")

            val count = numberPicker.childCount
            for (i in 0..count - 1) {
                val child = numberPicker.getChildAt(i)
                if (child is EditText) {
                    try {
                        Log.v(TAG, "found an edit text field ($i)")
                        val selectorWheelPaintField = numberPicker.javaClass.getDeclaredField("mSelectorWheelPaint")
                        selectorWheelPaintField.isAccessible = true
                        (selectorWheelPaintField.get(numberPicker) as Paint).color = color
                        val oldColor = child.currentTextColor
                        Log.v(TAG, "oldColor(%x), newColor(%x)".format(oldColor, color))
                        child.setTextColor(color)
                        numberPicker.invalidate()
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
    }
}
