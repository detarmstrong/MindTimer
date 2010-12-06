package com.futilities.mindtimer;

import java.util.ArrayList;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MindTimerList extends ListActivity {
    private static final int ACTIVITY_CREATE = 1;
    private static final int ACTIVITY_EDIT = 0;
    private final int INSERT_ID = 1;
    private TimersDbAdapter mDbHelper;
    private MindTimerList mCtx;
    protected ImageButton mToggleBtn;
    protected ArrayList<ToggleTimerClickListener> mToggleTimerClickListenerArrayList = new ArrayList<ToggleTimerClickListener>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timers_list);

        mDbHelper = new TimersDbAdapter(this);
        mDbHelper.open();

        mCtx = this;

        fillData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("MindTimer", "mind timer activity paused");

        for (ToggleTimerClickListener listener : mToggleTimerClickListenerArrayList) {
            TimerTask t = listener.getTimerTask();
            long timerId = listener.getTimerId();
            long startedAtMillisSinceBoot = SystemClock.elapsedRealtime()
                    - (long) (listener.getSecondsElapsed() * 1000);

            if (t != null) { // save state of timer

                mDbHelper.update(timerId, startedAtMillisSinceBoot);

                t.cancel();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("MindTimer", "mind timer activity stopped");

    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO find timers that are still progressing
        // if elapsedRealtime is less than the started at time, then a reboot
        // occured. Invalidate the
        // started at time

        // TODO find timers that have fully elapse and the toast came up - these
        // ones show as completed
    }

    private void fillData() {
        // get cursor for all timers
        Cursor timersCursor = mDbHelper.fetchAll();

        startManagingCursor(timersCursor);

        String[] from = new String[] { TimersDbAdapter.KEY_LABEL,
                TimersDbAdapter.KEY_SECONDS };
        int[] to = new int[] { R.id.TimerLabel, R.id.Duration };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mCtx,
                R.layout.timers_row, timersCursor, from, to);

        android.widget.SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor,
                    int columnIndex) {
                String colName = cursor.getColumnName(columnIndex);

                if (colName.equals("seconds")) {

                    // TODO format seconds in hh:mm
                    int interval_seconds = cursor.getInt(columnIndex);
                    ((TextView) view).setText(interval_seconds + "s");

                    final long id = cursor.getLong(cursor
                            .getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

                    Cursor startedAt = mDbHelper.fetchOne(id);

                    final long startedAtRealtime = startedAt
                            .getLong(startedAt
                                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT));

                    // bind to the play button and set its value
                    View test = (View) view.getParent();

                    mToggleBtn = (ImageButton) test
                            .findViewById(R.id.ToggleTimerOnOff);

                    mToggleBtn.setTag(id);

                    ToggleTimerClickListener toggleTimerClickListener = new ToggleTimerClickListener(
                            mCtx, id, interval_seconds, startedAtRealtime);

                    mToggleTimerClickListenerArrayList
                            .add(toggleTimerClickListener);

                    mToggleBtn.setOnClickListener(toggleTimerClickListener);

                    // also set the button to edit the timer since
                    // onListItemClick doesn't work anymore
                    ImageButton leftButton = (ImageButton) test
                            .findViewById(R.id.TimerImageButton);
                    leftButton.setOnClickListener(new EditClickListener(id));

                    return true;
                }
                // returning false here allows the other cols to be
                // handled as by the default cursoradapter behavior
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
        // create new intent and register here for callback
        Intent i = new Intent(this, TimerEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, TimerEdit.class);
        i.putExtra(TimersDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fillData();
    }

    private class EditClickListener implements OnClickListener {
        private Long mId;

        EditClickListener(Long id) {
            this.mId = id;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(MindTimerList.this, TimerEdit.class);
            i.putExtra(TimersDbAdapter.KEY_ROWID, mId);
            startActivityForResult(i, ACTIVITY_EDIT);
        }
    }

}