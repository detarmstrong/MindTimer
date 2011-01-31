package com.futilities.mindtimer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TimerEdit extends Activity {
    private static final int CREATE_INTERVAL = 1;
    private static final int TAKE_PICTURE = 2;
    private Uri outputFileUri;
    private TimersDbAdapter mDb;
    private Long mTimerId;
    private TextView mLabelText;
    private TextView mIntervalText;
    private String mHourPart = "0";
    private String mMinutePart = "0";
    private String mThumbnailAbsolutePath;
    private ImageButton mTimerIconView;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = new TimersDbAdapter(this);
        mDb.open();

        setContentView(R.layout.timer_edit);
        setTitle(R.string.edit_timer);

        mLabelText = (TextView) findViewById(R.id.LabelEdit);
        mIntervalText = (TextView) findViewById(R.id.Interval2);
        mTimerIconView = (ImageButton) findViewById(R.id.TimerIcon);

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

                // TODO prevent call to saveState()

                finish();

            }
        });

        Button takePicButton = (Button) findViewById(R.id.TakePicButton);
        takePicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimerEdit.this.getThumbailPicture(); // Handle in
                                                     // onActivityResult

            }
        });

        View intervalTextView = findViewById(R.id.Interval2);
        intervalTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intervalIntent = new Intent(TimerEdit.this,
                        IntervalPicker.class);

                Bundle extras = new Bundle();
                extras.putString("hourPart", pad(mHourPart, "0", 2));
                extras.putString("minutePart", pad(mMinutePart, "0", 2));

                intervalIntent.putExtras(extras);

                startActivityForResult(intervalIntent, CREATE_INTERVAL);

            }

        });

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

    }

    protected void clearFields() {
        Log.v("MindTimer", "clearing fields");

        mLabelText.setText("");
        // TODO clear interval
    }

    private void populateFields() {
        if (mTimerId != null) {
            Cursor timer = mDb.fetchOne(mTimerId);
            startManagingCursor(timer);

            mLabelText.setText(timer.getString(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));

            mMinutePart = String.valueOf(timer.getInt(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL)));

            mHourPart = String.valueOf(timer.getInt(timer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL)));

            String colonFormatted = toColonFormat(mHourPart, mMinutePart);

            mIntervalText.setText(colonFormatted);

            mThumbnailAbsolutePath = String
                    .valueOf(timer.getString(timer
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

            //TODO check that file really exists; if not show headless dummy
            // set background image for button
            Bitmap bm = BitmapFactory.decodeFile(mThumbnailAbsolutePath);

            mTimerIconView.setImageBitmap(bm);
        }

    }

    private String toColonFormat(String hours, String minutes) {

        hours = pad(hours, "0", 2);
        minutes = pad(minutes, "0", 2);

        return hours + ":" + minutes;
    }

    protected String pad(String numberPart, String pad, int desiredWidth) {
        // TODO fix me! I should use desiredWidth
        if (numberPart.length() == 1) {
            numberPart = "0" + numberPart;
        }

        return numberPart;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("timeredit", "in onActivityResult");

        switch (requestCode) {
        case TAKE_PICTURE:
            // Check if the result includes a thumbnail Bitmap
            if (data != null) {
                if (data.hasExtra("data")) {
                    if (mExternalStorageWriteable) {
                        Bitmap thumbnail = data.getParcelableExtra("data");

                        int height = (90 * thumbnail.getHeight()) / thumbnail.getWidth();
                        thumbnail = Bitmap.createScaledBitmap(thumbnail, 90, height, true); 

                        ImageButton icon = (ImageButton) findViewById(R.id.TimerIcon);
                        icon.setImageBitmap(thumbnail);

                        String path = Environment.getExternalStorageDirectory()
                                .toString();

                        // TODO create folder for MindTimer
                        //TODO add .nomedia file to this dir

                        OutputStream fOut = null;
                        File file = new File(path, "MindTimer_" + mTimerId
                                + ".png");
                        try {
                            fOut = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        thumbnail
                                .compress(Bitmap.CompressFormat.PNG, 100, fOut);

                        try {
                            fOut.flush();
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mThumbnailAbsolutePath = file.getAbsolutePath();
                    } else {
                        Toast.makeText(
                                this,
                                "External storage not available. Thumbnail not saved.",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
            break;
        case CREATE_INTERVAL:
            if (resultCode == RESULT_CANCELED) {
                // oh well
            } else if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                mHourPart = (String) extras.get("hourPart");
                mMinutePart = (String) extras.get("minutePart");

                TextView intervalTextView = (TextView) findViewById(R.id.Interval2);
                intervalTextView.setText(mHourPart + ":" + mMinutePart);

            }
            break;

        }

    }

    private void saveState() {
        String label = mLabelText.getText().toString();

        int hourLabel = Integer.parseInt(mHourPart);

        int minuteLabel = Integer.parseInt(mMinutePart);

        int intervalSeconds = (minuteLabel * 60) + (hourLabel * 60 * 60);

        if (mTimerId == null) {
            long id = mDb.create(label, intervalSeconds, minuteLabel,
                    hourLabel, mThumbnailAbsolutePath);

        } else {
            if (mThumbnailAbsolutePath.isEmpty())
                Toast.makeText(this, mThumbnailAbsolutePath, Toast.LENGTH_SHORT)
                        .show();

            mDb.update(mTimerId, label, intervalSeconds, minuteLabel,
                    hourLabel, mThumbnailAbsolutePath);
        }

    }

    private void getThumbailPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE);
    }

}
