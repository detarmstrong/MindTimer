package com.futilities.mindtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MindTimerAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MindTimer", "alarm received");
        Toast.makeText(context, R.string.one_shot_received, Toast.LENGTH_SHORT)
                .show();

        // clear the alarm state for this alarm - How do I identify this alarm?
        // see the Intent!
    }

}
