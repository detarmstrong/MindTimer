package com.futilities.mindtimer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IntervalPicker extends Activity {
    private final static String TAG = "numpicker";
    private int mHours;
    private int mMinutes;
    private String mFormattedInterval;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate master layout
        LayoutInflater factory = LayoutInflater.from(this);
        View masterLayout = factory.inflate(R.layout.interval_picker, null);
        
        // add numpad to viewGroup by id
        IntervalPickerNumPadView intervalPickerView = new IntervalPickerNumPadView(this);
        
        int count = ((ViewGroup) masterLayout).getChildCount();
        for (int i=0; i<count; i++) {
            View t = ((ViewGroup) masterLayout).getChildAt(i);
            if (t.getId() == R.id.NumPadLayout) {
                ((ViewGroup) t).addView(intervalPickerView);
            }
        }

        setContentView(masterLayout);
        
    }
}
