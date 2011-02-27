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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
	private long mTimerId;
	private TextView mDurationLabelView;
	private ImageButton mTimerIconView;
	private Object mThisTimer;
	private long mDeadline;
	private TimerState mTimerState;
	private TextView mTimeRemainingView;
	private long mSecondsElapsed;
	private int mSecondsDuration;
	private View mProgressBar;

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
		mProgressBar = findViewById(R.id.progress_bar);
		mTimerState = TimerState.NOT_STARTED;

		mThisView = (RelativeLayout) findViewById(R.id.TimerLayout);

	}

	public TimerState getTimerState() {
		return mTimerState;
	}

	public void setTimerState(TimerState timerState) {
		mTimerState = timerState;
	}

	public void setTimerId(long id) {
		mTimerId = id;
	}

	public long getTimerId() {
		return mTimerId;
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

	public void setSecondsDuration(int secondsDuration) {
		mSecondsDuration = secondsDuration;

	}

	@Override
	public void onClick(View v) {

		// TODO also go edit if the timer label is clicked
		if (mTimerIconView == v) {
			Log.i(TAG, "timer icon clicked");
			Intent i = new Intent(mContext, TimerEdit.class);
			i.putExtra(TimersDbAdapter.KEY_ROWID, mTimerId);
			((MindTimerList) mContext).startActivityForResult(i,
					MindTimerList.ACTIVITY_EDIT);

		} else if (mTimerControlButton == v) {
			Log.i(TAG, "play button clicked " + mTimerId);

			MindTimerList list = (MindTimerList) mContext;
			list.toggleTimerState(mTimerId, mSecondsDuration);
		}

	}

	public void setEditClickListener() {
		mTimerIconView.setOnClickListener(this);
	}

	// Listener is MindTimerListActivity
	public void setTimerControlClickListener() {
		mTimerControlButton.setOnClickListener(this);
	}

	public void updateProgress() {
		long delta = mDeadline - SystemClock.elapsedRealtime();
		mSecondsElapsed = delta / 1000;

		switch (mTimerState) {
		case RUNNING:
			mTimerControlButton
					.setImageResource(R.drawable.slowpoke_pause_button);
			
			mTimeRemainingView.setText(HourGlass.getDurationString(delta));
			
			// get width of progress_bar
			// get percentage completion of timer
			// get that percentage of width of progress bar
			// add that amount in px! to the left margin of TimeRemaining
			int progressBarPxWidth = mProgressBar.getMeasuredWidth();
			Log.i(TAG, "Timer " + mTimerId + ": progressBarWidthPx = "
					+ progressBarPxWidth);

			float completeRatio = ((float) mSecondsElapsed / mSecondsDuration);
			Log.i(TAG, "Timer " + mTimerId + ": completeRatio " + completeRatio
					+ " : " + mSecondsElapsed + " / " + mSecondsDuration);

			float adjustmentPxs = completeRatio * progressBarPxWidth;
			Log.i(TAG, "Timer " + mTimerId + ": adjPixs " + adjustmentPxs);

			ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mTimeRemainingView
					.getLayoutParams();

			mlp.setMargins((int) (progressBarPxWidth - adjustmentPxs), 0, 0, 0);

			break;
		case NOT_STARTED:
			mTimeRemainingView.setText(HourGlass.getDurationString(0));
			mTimerControlButton
					.setImageResource(R.drawable.slowpoke_play_button);
			break;
		}

	}

}
