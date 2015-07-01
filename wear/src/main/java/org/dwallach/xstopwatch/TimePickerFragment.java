package org.dwallach.xstopwatch;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import java.lang.reflect.Field;


public class TimePickerFragment extends DialogFragment {
    private final static String TAG = "TimePickerFragment";
    private int hours, minutes;

    public final static String HOURS_PARAM = "hours";
    public final static String MINUTES_PARAM = "minutes";

    /**
     * This is a totally cheesy time picker. We'd rather use the system one, but it doesn't work.
     */
    public static TimePickerFragment newInstance() {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        long duration = TimerState.getSingleton().getDuration(); // in milliseconds
        int minutes = (int) ((duration / 60000) % 60);
        int hours = (int) (duration / 3600000);
        args.putInt(HOURS_PARAM, hours);
        args.putInt(MINUTES_PARAM, minutes);
        fragment.setArguments(args);
        return fragment;
    }

    public TimePickerFragment() {
        // Required empty public constructor
    }

    // this solution adapted from: http://stackoverflow.com/questions/18120840/numberpicker-textcolour
    public static void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        Log.v(TAG, "setting number picker color");

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try {
                    Log.v(TAG, "found an edit text field (" + i + ")");
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    int oldColor = ((EditText)child).getCurrentTextColor();
                    Log.v(TAG, String.format("oldColor(%x), newColor(%x)", oldColor, color));
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return;
                }
                catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // nuke the frame around the fragment
        setStyle(STYLE_NO_FRAME, 0);

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_time_picker, container, false);


        if (getArguments() != null) {
            hours = getArguments().getInt(HOURS_PARAM);
            minutes = getArguments().getInt(MINUTES_PARAM);
        }
        ImageButton okButton = (ImageButton) mainView.findViewById(R.id.pickerOkButton);
        ImageButton resetButton = (ImageButton) mainView.findViewById(R.id.pickerResetButton);
        final NumberPicker hoursPicker = (NumberPicker) mainView.findViewById(R.id.hoursPicker);
        final NumberPicker minutesPicker = (NumberPicker) mainView.findViewById(R.id.minutesPicker);

        // it's a kludge: not allowed in the styles anywhere so we have to do this awful thing instead
        setNumberPickerTextColor(hoursPicker, 0xffffffff);
        setNumberPickerTextColor(minutesPicker, 0xffffffff);

        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(23);
        hoursPicker.setWrapSelectorWheel(false);
        hoursPicker.setValue(hours);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        minutesPicker.setValue(minutes);
        minutesPicker.setWrapSelectorWheel(false);
        minutesPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);   // numbers less than ten will have a leading zero
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hours = hoursPicker.getValue();
                minutes = minutesPicker.getValue();
                TimerState timerState = TimerState.getSingleton();
                timerState.setDuration(null, hours * 3600000 + minutes * 60000);

                // okay, we're done!
                dismiss();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return mainView;
    }
}
