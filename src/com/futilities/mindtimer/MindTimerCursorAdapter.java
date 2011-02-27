package com.futilities.mindtimer;

import com.futilities.mindtimer.HourGlass.TimerState;

import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class MindTimerCursorAdapter extends CursorAdapter {

	private static final String TAG = "MINDTIMERCURSORADAPTER";
	private Context mContext;
	private OnClickListener mListener;

	public MindTimerCursorAdapter(Context context, Cursor c) {
		super(context, c);

		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.i(TAG, "in BindView");
		
		MindTimerListItemView rowLayout = (MindTimerListItemView) view;

		long id = cursor.getLong(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

		String label = cursor.getString(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

		int secondsDuration = cursor.getInt(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_SECONDS));
		
		String minutePart = String.valueOf(cursor.getInt(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL)));

		String hourPart = String.valueOf(cursor.getInt(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL)));

		// TODO toColonFormat should be somewhere else, a "util" class? A Timer
		// class?
		String formattedDuration = TimerEdit
				.toColonFormat(hourPart, minutePart);

		String thumbnailFilePath = String
				.valueOf(cursor.getString(cursor
						.getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

		long startedAtMillisSinceBoot = cursor
				.getLong(cursor
						.getColumnIndexOrThrow(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT));
		long deadline = cursor
		.getLong(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT));

		// Call methods in view used to populate fields
		rowLayout.setTimerId(id);
		rowLayout.setLabel(label);
		rowLayout.setDurationLabel(formattedDuration);
		rowLayout.setSecondsDuration(secondsDuration);
		rowLayout.setThumbnail(thumbnailFilePath);
		
		rowLayout.setDeadline(deadline);

		// set onclick handlers
		rowLayout.setEditClickListener();
		rowLayout.setTimerControlClickListener();
		
		TimerState state = TimerState.NOT_STARTED;
		long elapsedSeconds = (SystemClock.elapsedRealtime() - startedAtMillisSinceBoot) * 1000;
		if(startedAtMillisSinceBoot > 0){
			state = TimerState.RUNNING;
		}
		
		rowLayout.setTimerState(state);
		
		rowLayout.updateProgress();

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new MindTimerListItemView(context);
	}

	// Commented because requery() causes bindView(), which updates views
	// with latest data
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// MindTimerListItemView timerView;
	//
	// if (convertView != null) {
	// Cursor timer = (Cursor) getItem(position);
	// timerView = (MindTimerListItemView) convertView;
	// timerView.setLabel(timer.getString(timer
	// .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));
	//
	// return timerView;
	//
	// } else {
	// return super.getView(position, convertView, parent);
	//
	// }
	//
	// }

}
