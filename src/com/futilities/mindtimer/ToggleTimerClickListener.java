package com.futilities.mindtimer;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ToggleTimerClickListener implements OnClickListener {

    private Object mId;

    public ToggleTimerClickListener(int id) {
        mId = id;
    }

    @Override
    public void onClick(View v) {
        Log.i("MindTimer", "clicked id " + mId);
        ((TextView) v).setText("Stop");
        
        // start animation of progress bar
        // get progress bar
        View parent = (View) v.getParent();
        ProgressBar progressHorizontal = (ProgressBar) parent.findViewById(R.id.ProgressBar01);
        progressHorizontal.incrementProgressBy(1);
        
        

    }

}
