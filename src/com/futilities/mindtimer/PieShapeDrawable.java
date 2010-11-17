package com.futilities.mindtimer;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;

public class PieShapeDrawable extends ShapeDrawable {
    private int x = 0;
    private int y = 0;
    private int width = 50;
    private int height = 50;

    public PieShapeDrawable(float startAngle, float sweepAngle, 
            int x, int y, int width, int height, int color) {
        super(new ArcShape(startAngle, sweepAngle));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.getPaint().setColor(color);
        this.setBounds(x, y, x + width, y + height);
    }


    
    
}
