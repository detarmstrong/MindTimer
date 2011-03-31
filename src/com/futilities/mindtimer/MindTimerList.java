package com.futilities.mindtimer;

import java.util.Timer;
import java.util.TimerTask;

import com.futilities.mindtimer.HourGlass.TimerState;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class MindTimerList extends ListActivity {
    protected final static int ACTIVITY_CREATE = 1;
    protected final static int ACTIVITY_EDIT = 0;
    private static final String TAG = "MINDTIMERLIST";
    private static final Long NO_TIMER_TO_START = -1L;
    private final int INSERT_ID = 1;
    private TimersDbAdapter mDbAdapter;
    private MindTimerCursorAdapter mCursorAdapter;
    private Timer mTimer;
    private ElapsationTask mElapsationTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "in onCreate");

        setContentView(R.layout.timers_list);

        initDbAdapter();

        // Get timerToStart id out of Intent
        Intent intent = getIntent();
        Long timerToStart = intent.getLongExtra(TimersDbAdapter.KEY_ROWID,
                NO_TIMER_TO_START);

        HourGlass glass = null;
        if (timerToStart != NO_TIMER_TO_START) {
            Cursor oneTimer = mDbAdapter.fetchOne(timerToStart);
            oneTimer.moveToFirst();
            long secondsDuration = oneTimer.getLong(oneTimer
                    .getColumnIndexOrThrow(TimersDbAdapter.KEY_SECONDS));

            // set the deadline in the db
            glass = new HourGlass(this, timerToStart, 0, 0, secondsDuration);
            glass.setTimerState(TimerState.NOT_STARTED);
            glass.transitionTimerState();
            oneTimer.close();
            
            // clear this extra from the intent
            intent.removeExtra(TimersDbAdapter.KEY_ROWID);
        }

        Cursor cursor = mDbAdapter.fetchAll();

        mCursorAdapter = new MindTimerCursorAdapter(this, cursor);

        if (glass != null) {
            mCursorAdapter.getHourGlassMap().put(timerToStart, glass);
        }

        setListAdapter(mCursorAdapter);

        initElapsationTask();

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "in onPause");

        cancelElapsationTask();
        

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "in onStop");
        mDbAdapter.close();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "in onDestroy");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "in onResume");
        
        initDbAdapter();

        mTimer = null;
        initElapsationTask();
        startElapsationTask();


    }

    private void initDbAdapter() {
        if(mDbAdapter != null && mDbAdapter.isDbOpen()){
            // Do nothing
        }
        else{
            mDbAdapter = new TimersDbAdapter(this);
            mDbAdapter.open();
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
        Intent i = new Intent(this, TimerEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_EDIT:
                case ACTIVITY_CREATE:
                    Log.i(TAG, "About to call requery");

                    // onCreate is not always called before this;
                    // so the db adapter isn't open
                    mDbAdapter.open();

                    requery();

                    break;
            }
        }
    }

    void requery() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();

        Cursor cursor = mDbAdapter.fetchAll();
        adapter.changeCursor(cursor);

        adapter.notifyDataSetChanged();
    }

    private void initElapsationTask() {
        // foreground UI updates depend on this timer thread
        mTimer = new Timer();
        mElapsationTask = new ElapsationTask();

    }

    private void startElapsationTask() {
        mTimer.schedule(mElapsationTask, 1000, 1000);

    }

    private void cancelElapsationTask() {
        mTimer.cancel();
        mTimer.purge();

    }

    private class ElapsationTask extends TimerTask {
        public void run() {
            mUiUpdateHandler.sendMessage(mUiUpdateHandler.obtainMessage(0));

        }
    }

    private Handler mUiUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // Update all visible views.
            ListView view = getListView();

            int first = view.getFirstVisiblePosition() - 2;
            int last = view.getLastVisiblePosition() + 1;
            int count = last - first;
            for (int i = 0; i < count; i++) {
                MindTimerListItemView itemView = (MindTimerListItemView) view
                        .getChildAt(first + i);

                if (itemView != null) {
                    itemView.updateProgress();
                }

            }

        }
    };

}