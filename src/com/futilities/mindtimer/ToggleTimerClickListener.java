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
    public static final int TIMER_BACKGROUND_ACTIVE = 4;
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

        } 
        else if (mTimerState == TIMER_NOT_STARTED
                || mTimerState == TIMER_PAUSED) { // then start
            transitionToActiveState(mSecElapsed, false);

        } 
        else if (mTimerState == TIMER_BACKGROUND_ACTIVE) { // update UI from database, alarm manager running
            transitionToActiveState(mSecElapsed, true);
            
        }
        else if (mTimerState == TIMER_DINGED) {
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

        setTimerState(TIMER_NOT_STARTED);
        
        // reset progress meter and use play button
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);

        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_play_button);
        }

        setProgressMeter(0,1);

    }

    public void transitionToCompleteState() {
        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);

        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_stop_button);
            setProgressMeter(1,1);
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
        }

        setTimerState(TIMER_DINGED);
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

        setTimerState(TIMER_PAUSED);

    }

    public void transitionToActiveState(float secondsElapsed, boolean resumeBackgroundActive) {
        if (secondsElapsed != 0) { 
            // timer has seconds on the clock, is either paused or backgrounded
            setSecondsElapsed(secondsElapsed);

        }
        
        mAlarmDingAt = (long) (SystemClock.elapsedRealtime() 
                + (mSecDuration - getSecondsElapsed()) * 1000);
        
        //TODO need sanity check that alarmManager is actually running
        
        if(!resumeBackgroundActive){// then AlarmManager already running            
            AlarmManager am = (AlarmManager) mCtx
                    .getSystemService(Activity.ALARM_SERVICE);
            Intent intent = new Intent(mCtx, MindTimerAlarm.class);
            intent.putExtra("timerId", (int) mId);
            PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
    
            // Use system alarm service so alarms can background
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mAlarmDingAt, sender);
        }

        // foreground UI updates depend on this timer thread
        mTimer = new Timer();
        mTimerTask = new UpdateTimeTask();
        mTimer.schedule(mTimerTask, 500, 500);

        ImageView d = (ImageView) ((MindTimerList) mCtx).getListView()
                .findViewWithTag(mId);
        if (d != null) {
            d.setImageResource(R.drawable.slowpoke_pause_button);
        }
        else{
            Log.e("mindtimer", "no image view found for " + mId);
            
        }

        setTimerState(TIMER_ACTIVE);
    }
    
    /**
     * Kill ui timer task, let the TimerManager roll and save the timer progress
     * @param dbHelper
     */
    public void transitionToBackgroundActiveState(TimersDbAdapter dbHelper) {
        TimerTask t = getTimerTask();
        long timerId = getTimerId();
        long startedAtMillisSinceBoot = SystemClock.elapsedRealtime()
                - (long) (getSecondsElapsed() * 1000);

        if (t != null) { //if a running timer, save state of timer
            dbHelper.update(timerId, startedAtMillisSinceBoot);
            t.cancel();
        }
        
        setTimerState(TIMER_BACKGROUND_ACTIVE);
        
    }

    public void setTimerState(int timerState) {
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

            setProgressMeter(mSecElapsed, mSecDuration);

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
    
    public void setProgressMeter(float secondsElapsed, int durationInSeconds) {
        
        float degrees = ((float) secondsElapsed / durationInSeconds) * 360;

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
