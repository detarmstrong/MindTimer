package com.futilities.mindtimer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_LAUNCH_INTENT = "launchIntent";

    @Override
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        int notification_id = Integer.parseInt(uri.getLastPathSegment());

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notification_id);

    }

}
