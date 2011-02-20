package com.futilities.mindtimer;

import java.io.File;
import java.util.ArrayList;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class MindTimerList extends ListActivity implements OnClickListener{
	private static final int ACTIVITY_CREATE = 1;
	private static final int ACTIVITY_EDIT = 0;
	private final int INSERT_ID = 1;
	private TimersDbAdapter mDbHelper;
	private MindTimerList mContext;
	protected ArrayList<ToggleTimerClickListener> mToggleTimerClickListenerArrayList = new ArrayList<ToggleTimerClickListener>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.timers_list);
		
        mDbHelper = new TimersDbAdapter(this);
        mDbHelper.open();
        
		Cursor cursor = mDbHelper.fetchAll();
		
		MindTimerCursorAdapter adapter = new MindTimerCursorAdapter(this,
				cursor);

		setListAdapter(adapter);

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

		Log.i("MindTimer", "mind timer activity stopped");

	}

	@Override
	protected void onResume() {
		super.onResume();

		

	}

	private void fillData() {

		Log.i("mindtimer", "in start of fillData");

		// get cursor for all timers
		Cursor timersCursor = mDbHelper.fetchAll();

		startManagingCursor(timersCursor);

		String[] from = new String[] { TimersDbAdapter.KEY_LABEL,
				TimersDbAdapter.KEY_SECONDS };
		int[] to = new int[] { R.id.TimerLabel, R.id.Duration };

		// TODOFORV2 Create new class that extends CursorAdapter, a la
		// CountdownCursorAdapter
		// TODOFORV2 Also create new view class extending LinearLayout, that
		// represents a row
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
				R.layout.timers_row, timersCursor, from, to);

		// TODOFORV2 since we are now extending cursoradapter, in that class
		// override newView and bindView methods, newView returns an instance of
		// the view represents a row, extends LinearLayout
		android.widget.SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String colName = cursor.getColumnName(columnIndex);

				if (colName.equals("seconds")) {
					final long id = cursor.getLong(cursor
							.getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

					int minuteLabel = cursor
							.getInt(cursor
									.getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL));

					int hourLabel = cursor
							.getInt(cursor
									.getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL));

					((TextView) view).setText(((hourLabel < 10) ? "0"
							+ hourLabel : hourLabel)
							+ ":"
							+ ((minuteLabel < 10) ? "0" + minuteLabel
									: minuteLabel));

					int interval_seconds = cursor.getInt(columnIndex);

					// bind to the play button and set its value
					View test = (View) view.getParent();

					ImageButton toggleBtn = (ImageButton) test
							.findViewById(R.id.ToggleTimerOnOff);

					toggleBtn.setTag(id);

					Log.i("mindtimer", "in fillData, set tag " + id);

					ToggleTimerClickListener toggleTimerClickListener = new ToggleTimerClickListener(
							mContext, id, interval_seconds, -1, 0);

					mToggleTimerClickListenerArrayList
							.add(toggleTimerClickListener);

					toggleBtn.setOnClickListener(toggleTimerClickListener);

					// also set the button to edit the timer since
					// onListItemClick doesn't work anymore
					ImageButton leftButton = (ImageButton) test
							.findViewById(R.id.TimerImageButton);

					leftButton.setOnClickListener(new EditClickListener(id));

					String thumbnailAbsolutePath = String
							.valueOf(cursor.getString(cursor
									.getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

					File file = new File(thumbnailAbsolutePath);

					if (file.exists()) {
						// set background image for button
						Bitmap bm = BitmapFactory
								.decodeFile(thumbnailAbsolutePath);

						leftButton.setImageBitmap(bm);
					}

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
		Log.i("mindtimer", "in onActivityResult");
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}