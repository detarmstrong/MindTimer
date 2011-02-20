package com.futilities.mindtimer;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MindTimerListItemView extends LinearLayout {

	private Context mContext;
	private TextView mLabelView;
	private ImageButton mTimerControlButton;
	private RelativeLayout mThisView;
	private long mId;
	private TextView mDurationLabelView;
	private ImageButton mTimerIconView;

	public MindTimerListItemView(Context context) {
		super(context);

		mContext = context;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(R.layout.timers_row, this, true);
		
		mLabelView = (TextView) findViewById(R.id.TimerLabel);
		mDurationLabelView = (TextView) findViewById(R.id.Duration);
		mTimerIconView = (ImageButton) findViewById(R.id.TimerImageButton);
		mTimerControlButton = (ImageButton) findViewById(R.id.ToggleTimerOnOff);
		mThisView = (RelativeLayout) findViewById(R.id.TimerLayout);
	}
	
	public void setId(long id){
		mId = id;
	}
	
	public void setLabel(String label){
		mLabelView.setText(label);
	}
	
	public void setDurationLabel(String durationLabel){
		mDurationLabelView.setText(durationLabel);
	}
	
	public void setThumbnail(String thumbnailFilePath){
		File file = new File(thumbnailFilePath);

        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(thumbnailFilePath);
            mTimerIconView.setImageBitmap(bm);
        }
	}

}
