package com.futilities.mindtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class MindTimerAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int timerId = intent.getIntExtra("timerId", -1);
        
        Log.i("MindTimer", "alarm received of timer id " + timerId);
        
        TimersDbAdapter db = new TimersDbAdapter(context);
        db.open();
        
        Cursor timer = db.fetchOne(timerId);
        
        String label = timer.getString(timer
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));
        
        Toast.makeText(context, "Timer '" + label + "' dings!", Toast.LENGTH_SHORT)
                .show();
        
        //TODO clear the alarm state for this alarm with id timerId
        
        db.update(timerId, -1);
        
        timer.deactivate();
        db.close();
    }
}