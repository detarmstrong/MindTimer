package com.futilities.mindtimer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
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
    private static final int TIMER_NOT_STARTED = 0;
    private static final int TIMER_ACTIVE = 1;
    private static final int TIMER_PAUSED = 2;
    protected static final int TIMER_DINGED = 3;
    Toast mToast;
    private long mId;
    private Timer mTimer;
    private int mSecDuration;
    private long mAlarmDingAt;
    private Context mCtx;
    private UpdateTimeTask mTimerTask;
    private int mTimerState;
    private float mSecElapsed; // seconds timer has been running for

    public ToggleTimerClickListener(Context context, long id, int secDuration, long startedAtRealtime) {
        mCtx = context;
        mId = id;
        mSecDuration = secDuration;
        mTimerState = TIMER_NOT_STARTED;
    }

    /**
     * @return the mTimerTask
     */
    public UpdateTimeTask getTimerTask() {
        return mTimerTask;
    }

    private class UpdateTimeTask extends TimerTask {
        public void run() {
            if (mCtx == null) {
                Log.v("MindTimer", "canceling because mctx is null");
                this.cancel();
                return;
            }

            mHandler.sendMessage(mHandler.obtainMessage(0));

        }
    }

    @Override
    public void onClick(View v) {
        AlarmManager am = (AlarmManager) v.getContext().getSystemService(
                Activity.ALARM_SERVICE);
        Intent intent = new Intent(v.getContext(), MindTimerAlarm.class);

        intent.putExtra("timerId", (int) mId);
        Log.v("MindTimer", "putting id in extra " + mId);
        PendingIntent sender = PendingIntent.getBroadcast(v.getContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (mTimerState == TIMER_ACTIVE) { // then pause
            mTimerState = TIMER_PAUSED;

            // change ui state of button to play, make the progress pie yellow
            // get view
            ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                    .findViewWithTag(mId);
            d.setImageResource(R.drawable.slowpoke_play_button);

            mTimer.cancel();
            mTimerTask.cancel();

            am.cancel(sender);

        } else if (mTimerState == TIMER_NOT_STARTED
                || mTimerState == TIMER_PAUSED) { // then start

            if (mTimerState == TIMER_PAUSED) { // then resume
                // alarm will now ding at now plus the duration minus the
                // seconds elapsed
                mAlarmDingAt = (long) (SystemClock.elapsedRealtime() + (mSecDuration - mSecElapsed) * 1000);

                // persist the sec elapsed of this timer - if the user pauses
                // and exits out and returns the elapsed state should remain

            } else if (mTimerState == TIMER_NOT_STARTED) {
                mAlarmDingAt = SystemClock.elapsedRealtime()
                        + (mSecDuration * 1000);

            }

            // Use system alarm service so alarms can background
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mAlarmDingAt, sender);

            // foreground UI updates depend on this timer thread
            mTimer = new Timer();
            mTimerTask = new UpdateTimeTask();
            mTimer.schedule(mTimerTask, 500, 500);

            mTimerState = TIMER_ACTIVE;
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mSecElapsed += .5;

            if (SystemClock.elapsedRealtime() > mAlarmDingAt) {
                mTimerState = TIMER_DINGED;
                mTimerTask.cancel();
            }

            float percentComplete = (float) mSecElapsed / mSecDuration;

            float degrees = percentComplete * 360;

            ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                    .findViewWithTag(mId);

            if (d != null) {

                if (mTimerState == TIMER_ACTIVE) {
                    // TODO move this so it only happens on the button clicks,
                    // not
                    // on timer tick
                    d.setImageResource(R.drawable.slowpoke_pause_button);

                } else if (mTimerState == TIMER_DINGED) {
                    d.setImageResource(R.drawable.slowpoke_stop_button);

                }

                // get pie progress view and update it
                ShapeDrawable drabble = new PieShapeDrawable(-90, degrees, 0,
                        0, 50, 50, 0x8074AC23);

                LayerDrawable bg = (LayerDrawable) d.getBackground();

                int layerId = bg.getId(1);

                bg.setDrawableByLayerId(layerId, drabble);

                d.setBackgroundDrawable(bg);
            }

        }
    };

    public long getTimerId() {
        return mId;
    }

    public float getSecondsElapsed() {
        return mSecElapsed;
    }

}
