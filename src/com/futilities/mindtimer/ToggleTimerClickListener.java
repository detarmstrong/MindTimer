package com.futilities.mindtimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class ToggleTimerClickListener implements OnClickListener {
    Toast mToast;
    private int mId;

    public ToggleTimerClickListener(int id) {
        mId = id;
    }

    @Override
    public void onClick(View v) {
        Log.i("MindTimer", "clicked id " + mId);

        //TODO toggle running state alarm manager; save progress made and cancel alarm
        // Upon restarting the alarm get the saved progress
        
        // When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver. Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        Intent intent = new Intent(v.getContext(), MindTimerAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(v.getContext(), 0,
                intent, 0);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) v.getContext().getSystemService(
                Activity.ALARM_SERVICE);

        // TODO use SystemClock.getElapsedRealTime() instead of
        // System.currentTimeMillis(). System.currentTimeMillis() may change if
        // the user
        // changes the time on the clock. It is wall clock time
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (1000 * 10), sender);

        // Tell the user about what we did.
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(v.getContext(), R.string.one_shot_scheduled,
                Toast.LENGTH_LONG);
        mToast.show();

        // start animation of progress bar
        // get progress bar
        View parent = (View) v.getParent();

        // ProgressBar progressHorizontal = (ProgressBar)
        // parent.findViewById(R.id.ProgressBar01);
        // progressHorizontal.incrementProgressBy(1);

        // TODO change play drawable to pause drawable

        ShapeDrawable drabble = new PieShapeDrawable(-90, 100, 0, 0, 50, 50,
                0xff74AC23);

        v.setBackgroundDrawable(drabble);

    }

}
