package com.futilities.mindtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MindTimerAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int timerId = intent.getIntExtra("timerId", -1);
        
        Log.i("MindTimer", "alarm received of timer id " + timerId);
        
        Toast.makeText(context, R.string.one_shot_received, Toast.LENGTH_SHORT)
                .show();

        // clear the alarm state for this alarm - How do I identify this alarm?
        // see the Intent!
    }

}
