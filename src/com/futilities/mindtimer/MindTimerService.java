package com.futilities.mindtimer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MindTimerService extends Service {
    
    private static final String TAG = MindTimerService.class.getSimpleName();
    /**
     * Use this tag to change logging level of the whole application
     * Is used is isLoggable(APPTAG, ... ) calls
     */
    public static final String APPTAG = "MindTimer";

    private static final String packageName = MindTimerService.class.getPackage().getName();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
