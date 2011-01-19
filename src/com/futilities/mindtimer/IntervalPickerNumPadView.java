package com.futilities.mindtimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class IntervalPickerNumPadView extends View {
    private static final String TAG = "INTERVALPICKERVIEW";
    private float mTileHeight;
    private float mTileWidth;
    private Rect mSelectedRect = new Rect();
    private int mSelectedX;
    private int mSelectedY;

    public IntervalPickerNumPadView(Context context) {
        super(context);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTileHeight = h / 4f;
        mTileWidth = w / 3f;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint background = new Paint();
        background.setColor(getResources().getColor(R.color.picker_background));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // draw lines
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(getResources().getColor(R.color.grid_line));

        Paint gridLineHilitePaint = new Paint();
        gridLinePaint.setColor(getResources()
                .getColor(R.color.grid_line_hilite));

        for (int i = 1; i < 4; i++) {
            canvas.drawLine(0, i * mTileHeight, getWidth(), i * mTileHeight,
                    gridLinePaint);
            canvas.drawLine(0, i * mTileHeight + 1, getWidth(), i * mTileHeight
                    + 1, gridLineHilitePaint);
        }

        for (int i = 1; i < 3; i++) {
            canvas.drawLine(i * mTileWidth, 0, i * mTileWidth, getHeight(),
                    gridLinePaint);
            canvas.drawLine(i * mTileWidth + 1, 0, i * mTileWidth + 1,
                    getHeight(), gridLineHilitePaint);
        }

        // draw numbers
        Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        foreground.setColor(getResources().getColor(R.color.foreground));
        foreground.setTextSize(mTileHeight * .55f);
        //foreground.setTextScaleX(mTileWidth / mTileHeight);
        foreground.setTextAlign(Paint.Align.CENTER);

        int total = 1;
        String drawText;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                switch (total) {
                case 10:
                    drawText = "00";
                    break;
                case 11:
                    drawText = "0";
                    break;
                case 12:
                    drawText = "<<";
                    break;
                default:
                    drawText = String.valueOf(total);
                }

                canvas.drawText(
                        drawText,
                        j * mTileWidth + (mTileWidth / 2),
                        i * mTileHeight
                                + (mTileHeight / 2 - (foreground.ascent() + foreground
                                        .descent()) / 2), foreground);

                total++;
            }

        }
        
        // Draw the selection...
        Log.d(TAG, "selRect=" + mSelectedRect);
        Paint selected = new Paint();
        selected.setColor(getResources().getColor(
              R.color.tile_selected));
        canvas.drawRect(mSelectedRect, selected);

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() != MotionEvent.ACTION_DOWN){
            return super.onTouchEvent(event);
        }
        
        select((int) (event.getX() / mTileWidth),
                (int) (event.getY() / mTileHeight));
        
        Log.d(TAG, "on touch event: " + mSelectedX + ", " + mSelectedY);
        
        return true;
        
    }
    
    private void select(int x, int y) {
        invalidate(mSelectedRect);
        mSelectedX = Math.min(Math.max(x, 0), 8);
        mSelectedY = Math.min(Math.max(y, 0), 8);
        
        //TODO lookup selected symbol in array of arrays and perform action
        // if 0 -9 then append it to the selected textview
        // if 00 append 00 to text view, if << then delete last char in text view
        
        getRect(mSelectedX, mSelectedY, mSelectedRect);
        
        //TODO fade out rect after .5 secs
        
        invalidate(mSelectedRect);
     }

     private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * mTileWidth), (int) (y * mTileHeight), (int) (x
              * mTileWidth + mTileWidth), (int) (y * mTileHeight + mTileHeight));
     }

}
