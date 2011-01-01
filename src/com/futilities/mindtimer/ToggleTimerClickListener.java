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
    public static final int TIMER_NOT_STARTED = 0;
    public static final int TIMER_ACTIVE = 1;
    public static final int TIMER_PAUSED = 2;
    public static final int TIMER_DINGED = 3;
    Toast mToast;
    private long mId;
    private Timer mTimer;
    private int mSecDuration;
    private long mAlarmDingAt;
    private Context mCtx;
    private UpdateTimeTask mTimerTask;
    private int mTimerState;
    private float mSecElapsed; // seconds timer has been running for

    public ToggleTimerClickListener(Context context, long id, int secDuration,
            long startedAtRealtime, float secondsElapsed) {
        mCtx = context;
        mId = id;
        mSecDuration = secDuration;
        mTimerState = TIMER_NOT_STARTED;
        // mSecElapsed = secondsElapsed;

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

        if (mTimerState == TIMER_ACTIVE) { // then pause
            mTimerState = TIMER_PAUSED;

            transitionToPauseState();

        } else if (mTimerState == TIMER_NOT_STARTED
                || mTimerState == TIMER_PAUSED) { // then start
            transitionToActiveState(mSecElapsed);

        } else if (mTimerState == TIMER_DINGED) {
            transitionToInactiveState();
        }

    }

    public void transitionToInactiveState() {
        // reset all timer info
        mSecElapsed = 0;
        mAlarmDingAt = 0;

        if (mTimerTask != null) {
            mTimerTask.cancel();
        }

        mTimerState = TIMER_NOT_STARTED;

        // reset progress meter and use play button
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);

        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_play_button);
        }

        setProgressMeter(0);

    }

    public void transitionToCompleteState() {
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);

        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_stop_button);
            setProgressMeter(360);
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
        }

        mTimerState = TIMER_DINGED;
    }

    public void transitionToPauseState() {
        AlarmManager am = (AlarmManager) mCtx
                .getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(mCtx, MindTimerAlarm.class);
        intent.putExtra("timerId", (int) mId);
        PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // change ui state of button to play, make the progress pie yellow
        // get view
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);
        d.setImageResource(R.drawable.slowpoke_play_button);

        mTimer.cancel();
        mTimerTask.cancel();

        am.cancel(sender);

        mTimerState = TIMER_PAUSED;

    }

    public void transitionToActiveState(float secondsElapsed) {
        AlarmManager am = (AlarmManager) mCtx
                .getSystemService(Activity.ALARM_SERVICE);
        Intent intent = new Intent(mCtx, MindTimerAlarm.class);
        intent.putExtra("timerId", (int) mId);
        PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (secondsElapsed != 0) {
            setSecondsElapsed(secondsElapsed);

        }
        
        mAlarmDingAt = (long) (SystemClock.elapsedRealtime() + (mSecDuration - getSecondsElapsed()) * 1000);

        // Use system alarm service so alarms can background
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mAlarmDingAt, sender);

        // foreground UI updates depend on this timer thread
        mTimer = new Timer();
        mTimerTask = new UpdateTimeTask();
        mTimer.schedule(mTimerTask, 500, 500);

        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);
        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_pause_button);
        }

        setTimerState(TIMER_ACTIVE);
    }

    private void setTimerState(int timerState) {
        mTimerState = timerState;

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mSecElapsed += .5;

            if (SystemClock.elapsedRealtime() > mAlarmDingAt) {
                transitionToCompleteState();
            }

            float percentComplete = (float) mSecElapsed / mSecDuration;

            float degrees = percentComplete * 360;

            setProgressMeter(degrees);

        }
    };

    public long getTimerId() {
        return mId;
    }

    public float getSecondsElapsed() {
        return mSecElapsed;
    }

    public void setSecondsElapsed(float secondsElapsed) {
        this.mSecElapsed = secondsElapsed;
    }

    public void setProgressMeter(float degrees) {
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);

        if (d != null) {

            // get pie progress view and update it
            ShapeDrawable drabble = new PieShapeDrawable(-90, degrees, 0, 0,
                    50, 50, 0x8074AC23);

            LayerDrawable bg = (LayerDrawable) d.getBackground();

            int layerId = bg.getId(1);

            bg.setDrawableByLayerId(layerId, drabble);

            d.setBackgroundDrawable(bg);
        }

    }

}
