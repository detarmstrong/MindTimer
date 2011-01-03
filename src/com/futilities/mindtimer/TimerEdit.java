package com.futilities.mindtimer;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class TimerEdit extends Activity {

    private TimersDbAdapter mDb;
    private Long mTimerId;
    private TextView mLabelText;
    private Spinner mIntervalHourSpinner;
    private Spinner mIntervalMinuteSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new TimersDbAdapter(this);
        mDb.open();

        setContentView(R.layout.timer_edit);
        setTitle(R.string.edit_timer);

        mLabelText = (TextView) findViewById(R.id.LabelEdit);
        mIntervalHourSpinner = (Spinner) findViewById(R.id.HourSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.hours_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIntervalHourSpinner.setAdapter(adapter);
        
        mIntervalMinuteSpinner = (Spinner) findViewById(R.id.MinuteSpinner);
        ArrayAdapter<CharSequence> minAdapter = ArrayAdapter.createFromResource(
                this, R.array.minutes_array, android.R.layout.simple_spinner_item);
        minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIntervalMinuteSpinner.setAdapter(minAdapter);

        // get timer id from savedInstanceState
        mTimerId = (savedInstanceState == null) ? null
                : (Long) savedInstanceState
                        .getSerializable(TimersDbAdapter.KEY_ROWID);

        if (mTimerId == null) {
            Bundle extras = getIntent().getExtras();
            mTimerId = (extras != null) ? extras
                    .getLong(TimersDbAdapter.KEY_ROWID) : null;
        }

        populateFields();

        Button saveButton = (Button) findViewById(R.id.SaveEdit);
        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
        
        Button cancelButton = (Button) findViewById(R.id.CancelEdit);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                TimerEdit.this.clearFields();
                
                //TODO prevent call to saveState()
                
                finish();
                
            }
        });

    }

    protected void clearFields() {
        Log.v("MindTimer", "clearing fields");
        
        mLabelText.setText("");
        mIntervalMinuteSpinner.setSelection(0);
        mIntervalHourSpinner.setSelection(0);
    }

    private void populateFields() {
        if (mTimerId != null) {
            Cursor timer = mDb.fetchOne(mTimerId);
            startManagingCursor(timer);
            
            mLabelText.setText(timer.getString(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));
            
            mIntervalMinuteSpinner.setSelection(timer.getInt(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL)));
            
            mIntervalHourSpinner.setSelection(timer.getInt(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL)));
            

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveState() {
        String label = mLabelText.getText().toString();
        
        int hourLabel = Integer.parseInt(mIntervalHourSpinner.getSelectedItem()
                .toString());
        
        int minuteLabel = Integer.parseInt(mIntervalMinuteSpinner.getSelectedItem()
                .toString());
        
        int intervalSeconds = (minuteLabel * 60) + (hourLabel * 60 * 60);

        if (mTimerId == null) {
            long id = mDb.create(label, intervalSeconds, minuteLabel, hourLabel);

        } else {
            mDb.update(mTimerId, label, intervalSeconds, minuteLabel, hourLabel);
        }

    }

}
