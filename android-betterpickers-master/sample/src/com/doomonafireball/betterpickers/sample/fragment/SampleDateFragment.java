package com.doomonafireball.betterpickers.sample.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.doomonafireball.betterpickers.sample.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * User: derek Date: 4/30/13 Time: 7:43 PM
 */
public class SampleDateFragment extends SherlockFragment
        implements DatePickerDialogFragment.DatePickerDialogHandler {

    private TextView text;
    private Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_and_button, container, false);

        text = (TextView) view.findViewById(R.id.text);
        button = (Button) view.findViewById(R.id.button);

        text.setText("--");
        button.setText("Set Date");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerBuilder dpb = new DatePickerBuilder()
                        .setFragmentManager(getChildFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setTargetFragment(SampleDateFragment.this);
                dpb.show();
            }
        });

        return view;
    }


    @Override
    public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
        text.setText("Year: " + year + "\nMonth: " + monthOfYear + "\nDay: " + dayOfMonth);
    }
}
