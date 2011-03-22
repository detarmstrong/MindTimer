package com.futilities.mindtimer;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MindTimerAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Long timerId = intent.getLongExtra("timerId", -1L);

		Log.i("MindTimer", "alarm received of timer id " + timerId);

		TimersDbAdapter db = new TimersDbAdapter(context);
		db.open();

		Cursor timer = db.fetchOne(timerId);

		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		long milliseconds = 1000;
		v.vibrate(milliseconds);

		long[] pattern = { 500, 300, 20, 100, 200 };
		v.vibrate(pattern, -1);
		
		String label = timer.getString(timer
				.getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

		Toast.makeText(context, "Timer '" + label + "' dings!",
				Toast.LENGTH_SHORT).show();

		ContentValues cv = new ContentValues();
        cv.put(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT, 0);
        cv.put(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT, 0);
        
        db.update(timerId, cv);

		timer.close();
		db.close();
	}


}