package com.futilities.mindtimer;

import android.os.SystemClock;
import android.util.Log;

public class HourGlass {

	private long mSecondsElapsed;
	private long mRealtimeDeadline;
	private long mSecondsDuration;
	private TimerState mTimerState;

	public enum TimerState {
		RUNNING, PAUSED, NOT_STARTED
	};

	public HourGlass(long elapsed, long deadline, long duration) {
		mSecondsElapsed = elapsed;
		mRealtimeDeadline = deadline;
		mSecondsDuration = duration;
		setTimerState(TimerState.NOT_STARTED);
	}

	public void updateElapsed() {
		mSecondsElapsed += 1000;
		Log.i("HORUGLASS", "updateElapsedCalled, mElapsed = " + mSecondsElapsed);

	}

	public void setTimerState(TimerState timerState) {
		mTimerState = timerState;
	}

	public TimerState getTimerState() {
		return mTimerState;
	}

	/**
	 * @purpose Go to next valid timer state based on the existing state
	 */
	public TimerState transitionTimerState() {
		TimerState startingTimerState = getTimerState();
		TimerState resultingTimerState;

		switch (startingTimerState) {
		case NOT_STARTED:
			resultingTimerState = TimerState.RUNNING;
			mRealtimeDeadline = (long) (SystemClock.elapsedRealtime() + (mSecondsDuration - mSecondsElapsed) * 1000);
			
			// Update db and requery cursor in cursor adapter
			break;

		default:
			resultingTimerState = TimerState.NOT_STARTED;
		}

		return resultingTimerState;

	}

}
