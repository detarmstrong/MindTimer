package com.futilities.mindtimer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class TagTimerActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private static final String TAG = "TagTimerActivity";
    private LinearLayout mTimerList;
    private Button mCancelButton;
    private TimersDbAdapter mDbHelper;
    private Long[] mTimerIds;
    private Button mSaveButton;
    private List<String> mPayloadsRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // TODO what should happen here? Alert user that tag is invalid? Then what? Exit?
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }

            // get the id for each record in the tag
            if (msgs == null || msgs.length == 0) {
                // TODO notify user that the tag can't be used here
                return;
            }

            NdefRecord[] records = msgs[0].getRecords();

            // Use the payload to identify the records. getId() is
            // non-deterministic
            mPayloadsRead = new ArrayList<String>();

            for (NdefRecord record : records) {
                mPayloadsRead.add(new String(record.getPayload(), Charset
                        .forName("UTF-8")));
                Log.v(TAG,
                        "record with payload: "
                                + mPayloadsRead.get(mPayloadsRead.size() - 1));
            }
            
            mDbHelper = new TimersDbAdapter(this);
            mDbHelper.open();
            
            // TODO escape single quotes in sql string?
            // TODO if multiple viable records in message, then allow   each for associating
            Cursor foundByPayload0 = mDbHelper
                    .fetchWhere(TimersDbAdapter.KEY_NFC_ID + " = '"
                            + mPayloadsRead.get(mPayloadsRead.size() - 1) + "'");

            if (foundByPayload0.getCount() > 0) {
                // TODO finish this activity and start mindtimer
                Toast.makeText(this, "Tag in DB, starting mindtimer",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            setContentView(R.layout.tag_for_timer);
            
            mTimerList = (LinearLayout) findViewById(R.id.timer_list);
            mCancelButton = (Button) findViewById(R.id.CancelTagForTimerButton);
            mCancelButton.setOnClickListener(this);
            mSaveButton = (Button) findViewById(R.id.SaveTagForTimerButton);
            mSaveButton.setOnClickListener(this);
            
            fillData();

        } else {
            Log.e(TAG, "Unknown intent " + intent);
            finish();
            return;
        }
    }

    private void fillData() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTimerList;
        content.removeAllViews();

        // get cursor for all timers
        Cursor timersCursor = mDbHelper.fetchAll();
        startManagingCursor(timersCursor);

        mTimerIds = new Long[timersCursor.getCount()];

        while (timersCursor.moveToNext()) {
            long timerId = timersCursor.getLong(timersCursor
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

            mTimerIds[timersCursor.getPosition()] = timerId;

            String labelText = String.valueOf(timersCursor
                    .getString(timersCursor
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));

            String thumbnailAbsolutePath = String
                    .valueOf(timersCursor.getString(timersCursor
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

            // inflate row layout
            LinearLayout row = (LinearLayout) inflater.inflate(
                    R.layout.tag_for_timer_row, null);

            // set values for views in row layout
            RadioButton radio = (RadioButton) row
                    .findViewById(R.id.selectTimer);
            radio.setId((int) timerId);
            radio.setOnCheckedChangeListener(this);

            TextView labelView = (TextView) row.findViewById(R.id.timerLabel);
            labelView.setText(labelText);

            ImageButton imageView = (ImageButton) row
                    .findViewById(R.id.TimerImageButton);
            Bitmap bm = BitmapFactory.decodeFile(thumbnailAbsolutePath);
            imageView.setImageBitmap(bm);

            // add view to list layout
            content.addView(row);

        }

    }

    @Override
    protected void onPause() {
        // TODO Implement onPause
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.CancelTagForTimerButton:
            setResult(RESULT_CANCELED);
            finish();
            break;
        case R.id.SaveTagForTimerButton:
            // get selected radio button
            RadioButton selected = selectedRadioButton();

            if (selected != null) {
                int timerId = selected.getId();
                Toast.makeText(this, "Associating with timer id " + timerId,
                        Toast.LENGTH_LONG).show();

                mDbHelper.update(timerId, mPayloadsRead.get(0));

                // TODO Start MindTimer activity
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Please select a timer",
                        Toast.LENGTH_SHORT).show();

            }
            break;
        }

    }

    private RadioButton selectedRadioButton() {
        RadioButton selected = null;
        for (long timerId : mTimerIds) {
            RadioButton b = (RadioButton) findViewById((int) timerId);

            if (b.isChecked()) {
                selected = b;
                break;
            }

        }

        return selected;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Set everything to unchecked then check the one that should be
        for (long timerId : mTimerIds) {
            RadioButton b = (RadioButton) findViewById((int) timerId);
            b.setChecked(false);
        }

        buttonView.setChecked(isChecked);

    }

}
