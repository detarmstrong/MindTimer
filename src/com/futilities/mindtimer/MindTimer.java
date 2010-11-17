package com.futilities.mindtimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MindTimer extends ListActivity {
    private final int INSERT_ID = 1;
    private TimersDbAdapter mDbHelper;
    private MindTimer mCtx;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timers_list);

        mDbHelper = new TimersDbAdapter(this);
        mDbHelper.open();

        mDbHelper.truncate();
        mDbHelper.create("Laundry", "1 min");
        mDbHelper.create("Brainwashing", "3 min");

        mCtx = this;

        fillData();
    }

    private View inflateView(int resource) {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return vi.inflate(resource, null);
    }

    private void fillData() {
        // get cursor for all timers
        Cursor timersCursor = mDbHelper.fetchAll();

        startManagingCursor(timersCursor);

        // set vars for from[], to[] of CursorAdapter
        String[] from = new String[] { TimersDbAdapter.KEY_LABEL,
                TimersDbAdapter.KEY_INTERVAL };
        int[] to = new int[] { R.id.TimerLabel, R.id.Duration };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mCtx,
                R.layout.timers_row, timersCursor, from, to);

        android.widget.SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor,
                    int columnIndex) {
                String colName = cursor.getColumnName(columnIndex);

                if (colName.equals("interval")) {
                    ((TextView) view).setText("hip hop");

                    int id = cursor.getInt(columnIndex);

                    // bind to the play button and set its value
                    View test = (View) view.getParent();

                    Button toggleBtn = (Button) test
                            .findViewById(R.id.ToggleTimerOnOff);

                    toggleBtn.setOnClickListener(new ToggleTimerClickListener(
                            id));

                    ShapeDrawable drabble = new PieShapeDrawable(-90, 0, 0, 0,
                            50, 50, 0xff74AC23);

                    toggleBtn.setBackgroundDrawable(drabble);

                    return true;
                }

                return false;
            }
        };

        adapter.setViewBinder(viewBinder);

        setListAdapter(adapter);

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
        // TODO Auto-generated method stub

    }

    private OnClickListener mOneShotListener = new OnClickListener() {
        public void onClick(View v) {
            // When the alarm goes off, we want to broadcast an Intent to our
            // BroadcastReceiver. Here we make an Intent with an explicit class
            // name to have our own receiver (which has been published in
            // AndroidManifest.xml) instantiated and called, and then create an
            // IntentSender to have the intent executed as a broadcast.
            Intent intent = new Intent(MindTimer.this, MindTimerAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(MindTimer.this,
                    0, intent, 0);

            // Schedule the alarm!
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            // TODO use SystemClock.getElapsedRealTime() instead of
            // System.currentTimeMillis(). System.currentTimeMillis() may change
            // if the user
            // changes the time on the clock. It is wall clock time
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (1000 * 30), sender);

            // Tell the user about what we did.
            // if (mToast != null) {
            // mToast.cancel();
            // }
            // mToast = Toast.makeText(MindTimer.this,
            // R.string.one_shot_scheduled,
            // Toast.LENGTH_LONG);
            // mToast.show();
        }
    };

}