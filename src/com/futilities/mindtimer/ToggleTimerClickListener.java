package com.futilities.mindtimer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class ToggleTimerClickListener implements OnClickListener {
    Toast mToast;
    private int mId;
    private long mStartTime;
    private Timer timer;
    private int mSecDuration;
    private long mAlarmAt;
    private Context mCtx;

    public ToggleTimerClickListener(Context context, int id, int secDuration) {
        mCtx = context;
        mId = id;
        mSecDuration = secDuration;
    }

    class UpdateTimeTask extends TimerTask {
        public void run() {
            long millis = SystemClock.elapsedRealtime() - mStartTime;
            int seconds = (int) (millis / 1000);

            Log.i("MindTimer", String.format("Seconds elapsed %d", seconds));

            if (SystemClock.elapsedRealtime() > mAlarmAt) {
                this.cancel();
            }

            float percentComplete = (float) seconds / mSecDuration;
            
            float degrees = percentComplete * 360;
            
            mHandler.sendMessage(mHandler.obtainMessage(0, degrees));

        }
    }

    @Override
    public void onClick(View v) {
        Log.i("MindTimer", "clicked id " + mId);

        // set tag for this view
        v.setTag(1);

        // TODO toggle running state alarm manager; save progress made and
        // cancel alarm
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
        mStartTime = SystemClock.elapsedRealtime();
        mAlarmAt = mStartTime + (mSecDuration * 1000);
        AlarmManager am = (AlarmManager) v.getContext().getSystemService(
                Activity.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mAlarmAt, sender);

        timer = new Timer();
        timer.schedule(new UpdateTimeTask(), 500, 500);

        // Tell the user about what we did.
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(v.getContext(), R.string.one_shot_scheduled,
                Toast.LENGTH_LONG);
        mToast.show();

        // TODO change play drawable to pause drawable

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            // get pie progress view and update it
            ShapeDrawable drabble = new PieShapeDrawable(-90, (Float) msg.obj, 0,
                    0, 50, 50, 0xff74AC23);

            // get view
            ImageView d = (ImageView) ((MindTimer) mCtx).getListView().findViewWithTag(1);

            d.setImageResource(R.drawable.slowpoke_pause_button);
            d.setBackgroundDrawable(drabble);

        }
    };

}
