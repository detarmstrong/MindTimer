package com.futilities.mindtimer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TimerEdit extends Activity {
    private static final String TAG = "TimerEdit";
    private static final int CREATE_INTERVAL = 1;
    private static final int TAKE_PICTURE = 2;
    protected static final int CONFIRM_DELETE_DIALOG = 1;
    private TimersDbAdapter mDb;
    private Long mTimerId;
    private TextView mLabelText;
    private TextView mIntervalText;
    private String mHourPart = "0";
    private String mMinutePart = "0";
    private ImageButton mTimerIconView;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    protected boolean mResultCanceled = false;
    private ViewGroup mNfcRegionLayout;
    private LinearLayout mTagFoundLayout;
    private LinearLayout mTagNotFoundLayout;
    private LinearLayout mAppropriateNFCLayout;
    private String mThumbnailAbsolutePath;
    private String mThumbnailStorageDir = Environment
            .getExternalStorageDirectory().toString();
    private boolean mIsTempThumbnail = false;
    private boolean mApiSupportsNFC;
    private NfcAdapter mNfcAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion < android.os.Build.VERSION_CODES.GINGERBREAD) {
            mApiSupportsNFC = false;
        } else {
            mApiSupportsNFC = true;
            mNfcAdapter = NfcAdapter.getDefaultAdapter();
        }

        Log.i(TAG, "Api Supports NFC:" + mApiSupportsNFC);
        Log.i(TAG, "NFC enabled: " + mNfcAdapter);
        
        // Magic (to me) to prevent the softkeyboard from coming up on activity
        // start
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDb = new TimersDbAdapter(this);
        mDb.open();

        setContentView(R.layout.timer_edit_2);
        setTitle(R.string.edit_timer);

        mLabelText = (TextView) findViewById(R.id.LabelEdit);
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

        mIntervalText = (TextView) findViewById(R.id.Interval2);
        String intervalText = (savedInstanceState == null) ? null
                : (String) savedInstanceState.getString("intervalText");

        if (intervalText != null) {
            mIntervalText.setText(intervalText);
        }

        mThumbnailAbsolutePath = (savedInstanceState == null) ? null
                : (String) savedInstanceState.getString("thumbnailTempPath");

        mHourPart = (savedInstanceState == null) ? "0"
                : (String) savedInstanceState.getString("hourPart");
        mHourPart = (mHourPart == null) ? "0" : mHourPart;

        mMinutePart = (savedInstanceState == null) ? "0"
                : (String) savedInstanceState.getString("minutePart");
        mMinutePart = (mMinutePart == null) ? "0" : mMinutePart;

        populateFields();

        Button saveButton = (Button) findViewById(R.id.SaveEdit);
        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                saveState();

                setResult(RESULT_OK);
                finish();

                Log.v(TAG, "AFTER FINISH"); // Never called because finish()
                                            // means it's finished!
            }

        });

        Button deleteButton = (Button) findViewById(R.id.DeleteTimer);
        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTimerId != null) {
                    // prompt for deletion confirmation
                    showDialog(CONFIRM_DELETE_DIALOG);

                } else {
                    setResult(RESULT_OK);
                    finish();
                }

            }
        });

        ImageButton takePicButton = (ImageButton) findViewById(R.id.TimerIcon);
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

        // mLabelText.setText("");
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

            Log.v(TAG, "Path to thumb:" + mThumbnailAbsolutePath);

            File file = new File(mThumbnailAbsolutePath);

            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(mThumbnailAbsolutePath);
                mTimerIconView.setImageBitmap(bm);
            }

            Log.i(TAG, "apisupport" + mApiSupportsNFC);
            
            if (mApiSupportsNFC) {
                showTagInfo(timer);
            } else {
                hideMentionOfNfc();
            }

        }

    }

    private void hideMentionOfNfc() {
        findViewById(R.id.nfc_tag_title).setVisibility(View.GONE);
    }

    private void showTagInfo(Cursor timer) {
        // If nfc tag is attached, then show the tag_found_layout under
        // edit_timer_nfc_layout
        String nfcTagPayload = timer.getString(timer
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_NFC_ID));

        mNfcRegionLayout = (ViewGroup) findViewById(R.id.edit_timer_nfc_layout);

        mTagFoundLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.tag_found_layout, null);

        mTagNotFoundLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.tag_not_found_layout, null);

        TextView helpText = (TextView) mTagNotFoundLayout
                .findViewById(R.id.what_is_a_nfc_tag_text_view);

        // activate links in text
        helpText.setMovementMethod(LinkMovementMethod.getInstance());

        if (nfcTagPayload != null) {
            mAppropriateNFCLayout = mTagFoundLayout;

            Button forgetTag = (Button) mAppropriateNFCLayout
                    .findViewById(R.id.forget_tag_button);

            forgetTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues cv = new ContentValues();
                    cv.putNull(TimersDbAdapter.KEY_NFC_ID);
                    mDb.update(mTimerId, cv);

                    Toast.makeText(getApplicationContext(),
                            "NFC tag forgotten", Toast.LENGTH_SHORT).show();

                    mTagFoundLayout.removeAllViews();

                    mNfcRegionLayout.addView(mTagNotFoundLayout);
                }
            });

            TextView tv = (TextView) mAppropriateNFCLayout
                    .findViewById(R.id.tag_payload);
            tv.setText(nfcTagPayload);

        } else {
            mAppropriateNFCLayout = mTagNotFoundLayout;

        }

        mNfcRegionLayout.addView(mAppropriateNFCLayout);
    }

    public static String toColonFormat(String hours, String minutes) {

        hours = pad(hours, "0", 2);
        minutes = pad(minutes, "0", 2);

        return hours + ":" + minutes;
    }

    protected static String pad(String numberPart, String pad, int desiredWidth) {
        // TODO fix me! I should use desiredWidth
        if (numberPart.length() == 1) {
            numberPart = "0" + numberPart;
        }

        return numberPart;
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v(TAG, "in onPause");

        if (!mResultCanceled) {
            // saveState();
        }
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

                            int height = (90 * thumbnail.getHeight())
                                    / thumbnail.getWidth();
                            thumbnail = Bitmap.createScaledBitmap(thumbnail,
                                    90, height, true);

                            ImageButton icon = (ImageButton) findViewById(R.id.TimerIcon);
                            icon.setImageBitmap(thumbnail);

                            // TODO create folder for MindTimer
                            // TODO add .nomedia file to this dir
                            Random rando = new Random();

                            String fileName;
                            if (mTimerId == null) {
                                fileName = "MindTimer_" + rando.nextInt()
                                        + ".png";
                                mIsTempThumbnail = true;
                            } else {
                                fileName = "MindTimer_" + mTimerId + ".png";
                                mIsTempThumbnail = false;
                            }

                            OutputStream fOut = null;
                            File file = new File(mThumbnailStorageDir, fileName);

                            try {
                                fOut = new FileOutputStream(file);
                                mThumbnailAbsolutePath = file.getAbsolutePath();
                                thumbnail.compress(Bitmap.CompressFormat.PNG,
                                        100, fOut);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            try {
                                fOut.flush();
                                fOut.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

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
            mTimerId = id;

        } else {
            mDb.update(mTimerId, label, intervalSeconds, minuteLabel,
                    hourLabel, mThumbnailAbsolutePath);
        }

        // TODO does it make sense to always save like this?
        if (mThumbnailAbsolutePath != null && mIsTempThumbnail) {
            File srcFile = new File(mThumbnailAbsolutePath);
            File destFile = new File(mThumbnailStorageDir, "MindTimer_"
                    + mTimerId + ".png");
            srcFile.renameTo(destFile);

            ContentValues cv = new ContentValues();
            cv.put(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH,
                    destFile.getAbsolutePath());
            mDb.update(mTimerId, cv);
        }

        mDb.close();

    }

    private void getThumbailPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            mResultCanceled = true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("intervalText", (String) mIntervalText.getText());
        outState.putString("hourPart", mHourPart);
        outState.putString("minutePart", mMinutePart);
        outState.putString("thumbnailTempPath", mThumbnailAbsolutePath);
        // outState.putLong(TimersDbAdapter.KEY_ROWID, mTimerId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new AlertDialog.Builder((Context) TimerEdit.this)
                .setIcon(R.drawable.button_down)
                .setTitle(R.string.confirm_delete_title)
                .setNegativeButton(R.string.confirm_delete_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.confirm_delete_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                // stop AlarmManager for timer if it's running
                                HourGlass.cancelAlarm(TimerEdit.this, mTimerId);

                                // delete timer
                                mDb.delete(mTimerId);

                                dialog.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }).create();
    }

}
