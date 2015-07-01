package org.dwallach.xstopwatch;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;


public class TimePickerFragment extends DialogFragment {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
