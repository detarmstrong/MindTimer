package com.futilities.mindtimer;

import android.content.ContentValues;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class HourGlass {

	private static final String TAG = "HOURGLASS";
	private Context mContext;
	private long mSecondsElapsed;
	private long mRealtimeDeadline;
	private long mSecondsDuration;
	private TimerState mTimerState;
	private long mId;

	public enum TimerState {
		RUNNING, NOT_STARTED
	};

	public HourGlass(Context context, long id, long elapsed, long deadline, long duration) {
		mContext = context;
		mId = id;
		mSecondsElapsed = elapsed;
		mRealtimeDeadline = deadline;
		mSecondsDuration = duration;
		setTimerState(TimerState.NOT_STARTED);
	}

	public void setTimerState(TimerState timerState) {
		mTimerState = timerState;
	}

	public TimerState getTimerState() {
		return mTimerState;
	}

	/**
	 * @purpose Go to next valid timer state based on the existing state
	 * @returns Transitioned to timer state
	 */
	public TimerState transitionTimerState() {
		TimerState startingTimerState = getTimerState();
		TimerState resultingTimerState;
		ContentValues cv;
		TimersDbAdapter db = new TimersDbAdapter(mContext);
		db.open();

		switch (startingTimerState) {
		case NOT_STARTED:
			
			Log.i(TAG, "go to RUNNING state");
			resultingTimerState = TimerState.RUNNING;
			
			long elapsedRealtime = SystemClock.elapsedRealtime();
			mRealtimeDeadline = (long) elapsedRealtime + (mSecondsDuration * 1000);
			
	        
	        cv = new ContentValues();
	        cv.put(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT, mRealtimeDeadline);
	        cv.put(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT, elapsedRealtime);
	        
	        db.update(mId, cv);
	        
	        
	        // requery() called in MindTimerList updates views
	        
			break;

		case RUNNING:
			resultingTimerState = TimerState.NOT_STARTED;
			
			cv = new ContentValues();
	        cv.put(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT, 0);
	        cv.put(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT, 0);
	        
	        db.update(mId, cv);
			
			break;
			
		default:
			resultingTimerState = TimerState.NOT_STARTED;
		}

		db.close();
		
		setTimerState(resultingTimerState);
		
		return resultingTimerState;

	}
	
	public long getSecondsElapsed() {
		return mSecondsElapsed;
	}

	public void setSecondsElapsed(long secondsElapsed) {
		mSecondsElapsed = secondsElapsed;
	}

	public long getRealtimeDeadline() {
		return mRealtimeDeadline;
	}

	public void setRealtimeDeadline(long realtimeDeadline) {
		mRealtimeDeadline = realtimeDeadline;
	}

	public long getSecondsDuration() {
		return mSecondsDuration;
	}

	public void setSecondsDuration(long secondsDuration) {
		mSecondsDuration = secondsDuration;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getTimeRemaining() {
    	long deadline = getRealtimeDeadline();
    	long delta = deadline - SystemClock.elapsedRealtime();
    	
		return getDurationString(delta);
	}
	/**
	 * Given a duration in milliseconds, returns a string in
	 * days, hours, minutes, and seconds.
	 * 
	 * @param duration in milliseconds
	 * @return
	 */
	public static String getDurationString(long duration) {
		StringBuilder sb = new StringBuilder();
		long seconds = (long) (duration / 1000);
		long minutes = seconds / 60;
		seconds = seconds % 60;
		long hours = minutes / 60;
		minutes = minutes % 60;
		long days = hours / 24;
		hours = hours % 24;
		
		sb.setLength(0);
		
		sb.append(hours);
		sb.append(":");
		if (minutes < 10) sb.append("0");
		sb.append(minutes);
		sb.append(":");
		if (seconds < 10) sb.append("0");
		sb.append(seconds);
		
		Log.i(TAG, sb.toString());
		
		return sb.toString();
	}
}
