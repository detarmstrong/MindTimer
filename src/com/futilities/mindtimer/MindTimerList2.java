package com.futilities.mindtimer;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MindTimerList2 extends Activity implements OnClickListener {
    private static final int ACTIVITY_CREATE = 1;
    private static final int ACTIVITY_EDIT = 0;
    private final int INSERT_ID = 1;
    private TimersDbAdapter mDbHelper;
    private MindTimerList2 mCtx;
    private LinearLayout mTimerList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timers_list_2);

        mTimerList = (LinearLayout) findViewById(R.id.timer_list);

        mDbHelper = new TimersDbAdapter(this);
        mDbHelper.open();

        mCtx = this;

        fillData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor timersWithProgress = mDbHelper
                .fetchWhere(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT
                        + " > 0");

        while (timersWithProgress.moveToNext()) {
            long timerId = timersWithProgress.getLong(timersWithProgress
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

            long startedAtRealtime = timersWithProgress
                    .getLong(timersWithProgress
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT));

            long timerDurationSeconds = timersWithProgress
                    .getLong(timersWithProgress
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_SECONDS));

            float secondsElapsed = (SystemClock.elapsedRealtime() - startedAtRealtime) / 1000;

        }

    }

    private void fillData() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTimerList;
        content.removeAllViews();

        // get cursor for all timers without tags
        Cursor timersCursor = mDbHelper.fetchAll();
        startManagingCursor(timersCursor);

        while (timersCursor.moveToNext()) {
            long timerId = timersCursor.getLong(timersCursor
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

            String labelText = String.valueOf(timersCursor
                    .getString(timersCursor
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));

            String thumbnailAbsolutePath = String
                    .valueOf(timersCursor.getString(timersCursor
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

            // inflate row layout
            LinearLayout row = (LinearLayout) inflater.inflate(
                    R.layout.tag_for_timer_row, null);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case INSERT_ID:
            createTimer();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void createTimer() {
        // create new intent and register here for callback
        Intent i = new Intent(this, TimerEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("mindtimer", "in onActivityResult");
        fillData();
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(MindTimerList2.this, TimerEdit.class);
        //i.putExtra(TimersDbAdapter.KEY_ROWID, timerId);
        startActivityForResult(i, ACTIVITY_EDIT);        
    }

}