package com.doomonafireball.betterpickers.sample.activity.numberpicker;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.doomonafireball.betterpickers.sample.R;
import com.doomonafireball.betterpickers.sample.activity.BaseSampleActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * User: derek Date: 3/17/13 Time: 3:59 PM
 */
public class SampleNumberMultipleHandlers extends BaseSampleActivity
        implements NumberPickerDialogFragment.NumberPickerDialogHandler {

    private TextView text;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_and_button);

        text = (TextView) findViewById(R.id.text);
        button = (Button) findViewById(R.id.button);

        text.setText("--");
        button.setText("Set Number");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerBuilder npb = new NumberPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .addNumberPickerDialogHandler(new MyCustomHandler());
                npb.show();
            }
        });
    }

    class MyCustomHandler implements NumberPickerDialogFragment.NumberPickerDialogHandler {

        @Override
        public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative,
                double fullNumber) {
            Toast.makeText(SampleNumberMultipleHandlers.this, "MyCustomHandler onDialogNumberSet!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        text.setText("Number: " + number + "\nDecimal: " + decimal + "\nIs negative: " + isNegative + "\nFull number: "
                + fullNumber);
    }
}
