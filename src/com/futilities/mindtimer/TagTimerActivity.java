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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class TagTimerActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private static final String TAG = "TagTimerActivity";
    private LinearLayout mTimerList;
    private Button mCancelButton;
    private TimersDbAdapter mDbAdapter;
    private Long[] mTimerIds;
    private Button mSaveButton;
    private List<String> mPayloadsRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // TODO what should happen here? Alert user that tag is invalid?
                // Then what? Exit?
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }

            if (msgs == null || msgs.length == 0) {
                Toast.makeText(this, "Tag not recognized by MindTimer",
                        Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            NdefRecord[] records = msgs[0].getRecords();

            mPayloadsRead = new ArrayList<String>();

            for (NdefRecord record : records) {
                mPayloadsRead.add(new String(record.getPayload(), Charset
                        .forName("UTF-8")));
                Log.v(TAG,
                        "record with payload: "
                                + mPayloadsRead.get(mPayloadsRead.size() - 1));
            }

            mDbAdapter = new TimersDbAdapter(this);
            mDbAdapter.open();

            // TODO escape single quotes in sql where string
            // TODO if multiple viable records in message, then allow each for
            // associating
            Cursor foundByPayload0 = mDbAdapter
                    .fetchWhere(TimersDbAdapter.KEY_NFC_ID + " = '"
                            + mPayloadsRead.get(mPayloadsRead.size() - 1) + "'");
            startManagingCursor(foundByPayload0);
            
            if (foundByPayload0.getCount() > 0) {
                foundByPayload0.moveToFirst();

                Long timerId = foundByPayload0.getLong(foundByPayload0
            			.getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));
                
                String timerLabel = foundByPayload0.getString(foundByPayload0
                        .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

                String thumbnailAbsolutePath = String
                        .valueOf(foundByPayload0.getString(foundByPayload0
                                .getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(
                        R.layout.timer_started_implicitly_toast,
                        (ViewGroup) findViewById(R.id.toast_layout_root));

                ImageView imageView = (ImageView) layout
                        .findViewById(R.id.toast_icon_for_timer);

                if (thumbnailAbsolutePath != null) {
                    Bitmap bm = BitmapFactory.decodeFile(thumbnailAbsolutePath);

                    if (bm != null) {
                        imageView.setImageBitmap(bm);
                    }
                }

                if (imageView.getBackground() == null) {
                    imageView.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.button_down));
                }

                TextView text = (TextView) layout.findViewById(R.id.text);
                String toastTextString = "Timer started"
                        + ((timerLabel != null && timerLabel.length() > 0) ? ": "
                                + timerLabel
                                : ".");
                text.setText(toastTextString);

                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();

                //TODO figure out why if I do this here then in mintimerlist I get error: 
                mDbAdapter.close();
                
                Intent wantTimerStarted = new Intent(this, MindTimerList.class);
                wantTimerStarted.putExtra(TimersDbAdapter.KEY_ROWID, timerId);
                startActivity(wantTimerStarted);
                
                Log.v(TAG, "After startActivity; now set result");
                
                setResult(RESULT_OK);
                finish();

                Log.v(TAG, "AFTER FINISH CALLED");
                
                return;

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

        // get cursor for all timers without tags
        Cursor timersCursor = mDbAdapter.fetchWhere(TimersDbAdapter.KEY_NFC_ID
                + " IS NULL");
        startManagingCursor(timersCursor);

        // get count of all timers
        long countAll = mDbAdapter.countAll();

        mTimerIds = new Long[timersCursor.getCount()];

        if (countAll == 0) {
            TextView noTimersTextView = new TextView(this);
            noTimersTextView
                    .setText(getResources().getText(R.string.no_timers));
            content.addView(noTimersTextView);
        } else if (timersCursor.getCount() == 0) {
            TextView noTimersTextView = new TextView(this);
            noTimersTextView.setText(getResources().getText(
                    R.string.no_untagged_timers));
            content.addView(noTimersTextView);
        }

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
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.CancelTagForTimerButton:
            setResult(RESULT_CANCELED);
            finish();
            break;
        case R.id.SaveTagForTimerButton:
            RadioButton selected = selectedRadioButton();

            if (selected != null) {
                int timerId = selected.getId();
                Toast.makeText(this, "Scan tag again to start timer",
                        Toast.LENGTH_LONG).show();

                ContentValues cv = new ContentValues(1);
                cv.put(TimersDbAdapter.KEY_NFC_ID, mPayloadsRead.get(0));
                mDbAdapter.update(timerId, cv);

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
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        if(mDbAdapter != null){
            mDbAdapter.close();
            
        }
    }

}
