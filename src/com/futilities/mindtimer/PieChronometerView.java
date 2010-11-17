package com.futilities.mindtimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChronometerView extends View {

    private Paint mPiePaint;

    public PieChronometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mPiePaint = new Paint();
        mPiePaint.setAntiAlias(true);
        mPiePaint.setStyle(Paint.Style.FILL);
        mPiePaint.setColor(0x88FF0000);
    }

    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        
        Context c = this.getContext();
        
        canvas.drawArc(new RectF( 10, 270,  70, 330), 0, 100, true, mPiePaint);

    }
    
    

}
