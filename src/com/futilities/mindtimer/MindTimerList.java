package com.futilities.mindtimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.futilities.mindtimer.HourGlass.TimerState;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class MindTimerList extends ListActivity {
	protected final static int ACTIVITY_CREATE = 1;
	protected final static int ACTIVITY_EDIT = 0;
	private static final String TAG = "MINDTIMERLIST";
	private final int INSERT_ID = 1;
	private TimersDbAdapter mDbHelper;
	private MindTimerCursorAdapter mCursorAdapter;
	private HashMap<Long, HourGlass> mRunningTimers;
	private Timer mTimer;
	private ElapsationTask mElapsationTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.timers_list);

		mDbHelper = new TimersDbAdapter(this);
		mDbHelper.open();

		Cursor cursor = mDbHelper.fetchAll();

		mCursorAdapter = new MindTimerCursorAdapter(this, cursor);

		setListAdapter(mCursorAdapter);
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.i("mindtimer", "in onPause");

		// TODO unregister handlers for updating ui

	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.i("MindTimer", "in onStop");

	}

	@Override
	protected void onResume() {
		super.onResume();

		//TODO load running timers
		mRunningTimers = new HashMap<Long, HourGlass>();
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

		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case ACTIVITY_EDIT:
			case ACTIVITY_CREATE:
				Log.i(TAG, "About to call requery");
	
				requery();
				
				break;
			}
		}
	}

	private void requery() {
		CursorAdapter adapter = (CursorAdapter) getListAdapter();
		adapter.getCursor().requery(); // causes bindView() to run again
	}

	public void toggleTimerState(long id) {
		HourGlass glass;
		
		// set to running state
		if(!mRunningTimers.containsKey(id)){
			glass = new HourGlass(this, id, 0, 0, 0);
			
			// register listitemview for updates
			mRunningTimers.put(id, glass);
		}
		else {
			glass = mRunningTimers.get(id);
		}
		
		glass.transitionTimerState();
		
		// Rebind views via requery, on rebinding views will have updating state
		requery(); 
		
		if(mRunningTimers.size() > 0){
			startElapsationTask();
		}
		
	}

	// If any active timers, then this should kick off the timer task
	private void startElapsationTask() {
		
		// foreground UI updates depend on this timer thread
        mTimer = new Timer();
        mElapsationTask = new ElapsationTask();
        mTimer.schedule(mElapsationTask, 1000, 1000);
		
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
    		
    		int first = view.getFirstVisiblePosition();
    		int count = view.getChildCount();
    		
    		for (int i=0; i<count; i++) {
    			MindTimerListItemView itemView = (MindTimerListItemView) view.getChildAt(i);
    			
    			long id = itemView.getId();
    			
    			HourGlass glass = mRunningTimers.get(id);
    			TimerState state = glass.getTimerState();
    			long secondsElapsed = glass.getSecondsElapsed();
    			
    			itemView.updateProgress(state, secondsElapsed);
    		}
            
        }
    };

}