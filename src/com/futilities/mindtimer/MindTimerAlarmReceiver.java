/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futilities.mindtimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MindTimerAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "MINDTIMERALARMRECEIVER";
    private TimersDbAdapter mDb;
    private long mTimerId;
    private Context mContext;
    private Uri mUri;

    @Override
    public void onReceive(Context context, Intent intent) {
        mTimerId = intent.getLongExtra("timerId", -1L);
        mUri = intent.getData();
        mContext = context;

        Log.i("MindTimer", "alarm received of timer id " + mTimerId);

        mDb = new TimersDbAdapter(context);
        mDb.open();

        startNotification();

        updateDb();

        mDb.close();
    }

    private void startNotification() {
        Cursor timer = mDb.fetchOne(mTimerId);
        if (timer == null) {
            return;
        }

        String label = timer.getString(timer
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

        timer.close();

        if (TextUtils.isEmpty(label)) {
            label = mContext.getString(R.string.app_name);
        }

        Intent notificationIntent = new Intent(mContext,
                NotificationReceiver.class);
        notificationIntent.setData(mUri);

        // This intent will be forwarded on to the NotificationReceiver, which
        // will generate an intent based on this intents URI
        Intent stowawayIntent = new Intent();
        stowawayIntent.setAction(Intent.ACTION_EDIT);
        stowawayIntent.setData(mUri);

        notificationIntent.putExtra(NotificationReceiver.EXTRA_LAUNCH_INTENT,
                stowawayIntent.toUri(0));

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent launchOnClickIntent = PendingIntent.getBroadcast(
                mContext, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // construct the Notification object.
        Notification notif = new Notification(R.drawable.button_down, label,
                System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(mContext, label, "Timer done",
                launchOnClickIntent); // broadcast PendingIntent to
                                      // NotificationReceiver

        notif.vibrate = new long[] { 400, 100, 200, 100, 100, 100 };
        notif.defaults |= Notification.DEFAULT_LIGHTS;
        notif.defaults |= Notification.DEFAULT_SOUND;

        notif.flags |= Notification.FLAG_INSISTENT;

        // look up the notification manager service
        NotificationManager nm = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Show countdown notification
        nm.notify((int) mTimerId, notif);

        // NotificationState.start(mContext, mUri);

    }

    public void updateDb() {
        ContentValues cv = new ContentValues();
        cv.put(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT, 0);
        cv.put(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT, 0);

        mDb.update(mTimerId, cv);
    }

}