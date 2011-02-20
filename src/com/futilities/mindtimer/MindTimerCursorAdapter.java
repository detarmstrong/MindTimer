package com.futilities.mindtimer;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class MindTimerCursorAdapter extends CursorAdapter {

	private Context mContext;

	public MindTimerCursorAdapter(Context context, Cursor c) {
		super(context, c);

		mContext = context;

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		MindTimerListItemView rowLayout = (MindTimerListItemView) view;

		long id = cursor.getLong(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

		String label = cursor.getString(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

		String minutePart = String.valueOf(cursor.getInt(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL)));

		String hourPart = String.valueOf(cursor.getInt(cursor
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL)));

		//TODO toColonFormat should be somewhere else, a "util" class? A Timer class?
		String formattedDuration = TimerEdit
				.toColonFormat(hourPart, minutePart);

		String thumbnailFilePath = String
				.valueOf(cursor.getString(cursor
						.getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));
		
		// Call methods in view used to populate fields
		rowLayout.setId(id);
		rowLayout.setLabel(label);
		rowLayout.setDurationLabel(formattedDuration);
		rowLayout.setThumbnail(thumbnailFilePath);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new MindTimerListItemView(context);
	}

}
