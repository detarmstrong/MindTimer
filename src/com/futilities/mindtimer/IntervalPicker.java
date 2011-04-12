/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futilities.mindtimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class IntervalPicker extends Activity implements OnClickListener {
    private final static String TAG = "numpicker";
    private final static int TIMEPART_HOURS_SELECTED = R.id.SelectHoursButton;
    private final static int TIMEPART_MINUTES_SELECTED = R.id.SelectMinutesButton;

    private int mTimepartSelected = TIMEPART_MINUTES_SELECTED;
    private TextView mTimepartTextViewSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate master layout
        LayoutInflater factory = LayoutInflater.from(this);
        View masterLayout = factory.inflate(R.layout.interval_picker, null);

        // add numpad to viewGroup by id
        IntervalPickerNumPadView intervalPickerView = new IntervalPickerNumPadView(
                this);

        int count = ((ViewGroup) masterLayout).getChildCount();
        for (int i = 0; i < count; i++) {
            View t = ((ViewGroup) masterLayout).getChildAt(i);
            if (t.getId() == R.id.NumPadLayout) {
                ((ViewGroup) t).addView(intervalPickerView);
            }
        }

        setContentView(masterLayout);

        View hourToggleButton = findViewById(R.id.SelectHoursButton);
        hourToggleButton.setOnClickListener(this);

        View minuteToggleButton = findViewById(R.id.SelectMinutesButton);
        minuteToggleButton.setOnClickListener(this);

        View cancelActivityButton = findViewById(R.id.CancelButton);
        cancelActivityButton.setOnClickListener(this);

        View doneActivityButton = findViewById(R.id.DoneButton);
        doneActivityButton.setOnClickListener(this);

        View hourPartText = findViewById(R.id.HourPartText);
        hourPartText.setOnClickListener(this);

        View minutePartText = findViewById(R.id.MinutePartText);
        minutePartText.setOnClickListener(this);

        // set interval from intent
        Bundle extras = getIntent().getExtras();
        
        if(extras.containsKey("hourPart")){
            TextView hourText = (TextView) findViewById(R.id.HourPartText);
            hourText.setText(extras.getString("hourPart"));
        }

        if(extras.containsKey("minutePart")){
            TextView minuteText = (TextView) findViewById(R.id.MinutePartText);
            minuteText.setText(extras.getString("minutePart"));
        }
        
        // set default selection
        toggleTimePartSelected(mTimepartSelected);
    }

    private void toggleTimePartSelected(int timepartSelected) {
        mTimepartSelected = timepartSelected;

        switch (mTimepartSelected) {
        case TIMEPART_HOURS_SELECTED:
            mTimepartTextViewSelected = (TextView) findViewById(R.id.HourPartText);
            break;
        case TIMEPART_MINUTES_SELECTED:
            mTimepartTextViewSelected = (TextView) findViewById(R.id.MinutePartText);
            break;
        default:
            throw new Error("Not handling this timepart");
        }

        Button minutesButton;
        Button hoursButton;

        int deselectedColor = getResources()
                .getColor(R.color.button_deselected);
        int selectedColor = getResources().getColor(R.color.button_selected);

        switch (timepartSelected) {
        case TIMEPART_HOURS_SELECTED:
            hoursButton = (Button) findViewById(R.id.SelectHoursButton);
            hoursButton.setBackgroundColor(selectedColor);

            minutesButton = (Button) findViewById(R.id.SelectMinutesButton);
            minutesButton.setBackgroundColor(deselectedColor);

            break;

        case TIMEPART_MINUTES_SELECTED:
            minutesButton = (Button) findViewById(R.id.SelectMinutesButton);
            minutesButton.setBackgroundColor(selectedColor);

            hoursButton = (Button) findViewById(R.id.SelectHoursButton);
            hoursButton.setBackgroundColor(deselectedColor);
            break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.SelectHoursButton:
        case R.id.SelectMinutesButton:
            toggleTimePartSelected(v.getId());
            break;
        case R.id.DoneButton:
            // return hour and minute part from activity
            CharSequence hours = ((TextView) findViewById(R.id.HourPartText))
                    .getText();
            CharSequence minutes = ((TextView) findViewById(R.id.MinutePartText))
                    .getText();

            Intent returnIntent = new Intent();
            Bundle bundle = new Bundle();

            bundle.putString("hourPart", (String) hours);
            bundle.putString("minutePart", (String) minutes);

            returnIntent.putExtras(bundle);

            setResult(RESULT_OK, returnIntent);
            finish();
            break;
        case R.id.CancelButton:
            setResult(RESULT_CANCELED);
            finish();
            break;
        case R.id.HourPartText:
            toggleTimePartSelected(TIMEPART_HOURS_SELECTED);
            break;
        case R.id.MinutePartText:
            toggleTimePartSelected(TIMEPART_MINUTES_SELECTED);
            break;
        }

    }

    public void handleNumPadSelection(String rotateInString) {
        String newText;
        if (rotateInString == "<<") {
            String temp = (String) mTimepartTextViewSelected.getText();

            try {
                newText = temp.substring(0, temp.length() - 1);
                if (newText.length() == 1)
                    newText = "0" + newText;

            } catch (StringIndexOutOfBoundsException e) {
                newText = "00";
            }

        } else {
            newText = rotateText(mTimepartTextViewSelected.getText(),
                    rotateInString, 2);
        }

        mTimepartTextViewSelected.setText(newText);

    }

    private String rotateText(CharSequence text, String rotateInString, int len) {
        String interim = text + rotateInString;
        return (interim.length() < len) ? interim : interim.substring(
                interim.length() - len, interim.length());
    }
}