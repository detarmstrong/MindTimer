package com.futilities.mindtimer;

import java.io.File;

import com.futilities.mindtimer.HourGlass.TimerState;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MindTimerListItemView extends LinearLayout implements
		OnClickListener {

	private static final String TAG = "MINDTIMERLISTVIEWITEM";
	private Context mContext;
	private TextView mLabelView;
	private ImageButton mTimerControlButton;
	private RelativeLayout mThisView;
	private long mId;
	private TextView mDurationLabelView;
	private ImageButton mTimerIconView;
	private Object mThisTimer;
	private long mDeadline;
	private TimerState mTimerState;
	private TextView mTimeRemainingView;
	private long mSecondsElapsed;

	public MindTimerListItemView(Context context) {
		super(context);

		mContext = context;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.timers_row, this, true);

		mLabelView = (TextView) findViewById(R.id.TimerLabel);
		mDurationLabelView = (TextView) findViewById(R.id.Duration);
		mTimeRemainingView = (TextView) findViewById(R.id.TimeRemaining);
		mTimerIconView = (ImageButton) findViewById(R.id.TimerImageButton);
		mTimerControlButton = (ImageButton) findViewById(R.id.ToggleTimerOnOff);
		mTimerState = TimerState.NOT_STARTED;
		
		mThisView = (RelativeLayout) findViewById(R.id.TimerLayout);
		

	}

	public TimerState getTimerState() {
		return mTimerState;
	}

	public void setTimerState(TimerState timerState) {
		mTimerState = timerState;
	}

	public void setId(long id) {
		mId = id;
	}

	public void setLabel(String label) {
		mLabelView.setText(label);
	}

	public void setDurationLabel(String durationLabel) {
		mDurationLabelView.setText(durationLabel);
	}

	public void setThumbnail(String thumbnailFilePath) {
		File file = new File(thumbnailFilePath);

		if (file.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(thumbnailFilePath);
			mTimerIconView.setImageBitmap(bm);
		}
	}

	public void setDeadline(long deadline) {
		mDeadline = deadline;
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, v.toString());

		// TODO also go edit if the timer label is clicked
		if (mTimerIconView == v) {
			Log.i(TAG, "timer icon clicked");
			Intent i = new Intent(mContext, TimerEdit.class);
			i.putExtra(TimersDbAdapter.KEY_ROWID, mId);
			((MindTimerList) mContext).startActivityForResult(i,
					MindTimerList.ACTIVITY_EDIT);

		} else if (mTimerControlButton == v) {
			Log.i(TAG, "play button clicked");

			MindTimerList list = (MindTimerList) mContext;
			list.toggleTimerState(mId);
		}

	}

	public void setEditClickListener() {
		mTimerIconView.setOnClickListener(this);
	}

	// Listener is MindTimerListActivity
	public void setTimerControlClickListener() {
		mTimerControlButton.setOnClickListener(this);
	}

	public void updateProgress(TimerState state, long secondsElapsed) {
		long delta = mDeadline - SystemClock.elapsedRealtime();
		mTimerState = state;
		mSecondsElapsed = secondsElapsed;
		
		switch(state){
		case RUNNING:
			mTimeRemainingView.setText(HourGlass.getDurationString(delta));
			break;
		case NOT_STARTED:
			break;
		case PAUSED:
			mTimerControlButton.setImageResource(R.drawable.slowpoke_pause_button);
			break;
		}
		
	}

}
