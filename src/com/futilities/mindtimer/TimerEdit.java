package com.futilities.mindtimer;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TimerEdit extends Activity {

    private TimersDbAdapter mDb;
    private Long mTimerId;
    private TextView mLabelText;
    private TextView mIntervalSecondsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new TimersDbAdapter(this);
        mDb.open();

        setContentView(R.layout.timer_edit);
        setTitle(R.string.edit_timer);

        mLabelText = (TextView) findViewById(R.id.LabelEdit);
        mIntervalSecondsText = (TextView) findViewById(R.id.IntervalEdit);

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
        mIntervalSecondsText.setText("");
        
    }

    private void populateFields() {
        if (mTimerId != null) {
            Cursor timer = mDb.fetchOne(mTimerId);
            startManagingCursor(timer);
            
            mLabelText.setText(timer.getString(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));
            
            mIntervalSecondsText.setText(timer.getString(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_SECONDS)));

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
        
        int intervalSeconds = Integer.parseInt(mIntervalSecondsText.getText()
                .toString());

        if (mTimerId == null) {
            long id = mDb.create(label, intervalSeconds);

        } else {
            mDb.update(mTimerId, label, intervalSeconds);
        }

    }

}
