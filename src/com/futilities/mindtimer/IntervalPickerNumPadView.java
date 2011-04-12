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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class IntervalPickerNumPadView extends View {
    private static final String TAG = "INTERVALPICKERVIEW";

    private static final String[][] NUMPAD_SYMBOLS = { { "1", "2", "3" },
            { "4", "5", "6" }, { "7", "8", "9" }, { "00", "0", "<<" } };

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

        for (int i = 1; i < NUMPAD_SYMBOLS.length; i++) {
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
        // foreground.setTextScaleX(mTileWidth / mTileHeight);
        foreground.setTextAlign(Paint.Align.CENTER);

        int total = 1;
        String drawText;

        for (int i = 0; i < NUMPAD_SYMBOLS.length; i++) {
            for (int j = 0; j < 3; j++) {
                drawText = NUMPAD_SYMBOLS[i][j];

                canvas.drawText(
                        drawText,
                        j * mTileWidth + (mTileWidth / 2),
                        i
                                * mTileHeight
                                + (mTileHeight / 2 - (foreground.ascent() + foreground
                                        .descent()) / 2), foreground);

                total++;
            }

        }

        // Draw the selection...
        Log.d(TAG, "selRect=" + mSelectedRect);
        Paint selected = new Paint();
        selected.setColor(getResources().getColor(R.color.tile_selected));
        canvas.drawRect(mSelectedRect, selected);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
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

        // TODO fade out rect after .5 secs
        getRect(mSelectedX, mSelectedY, mSelectedRect);

        String selectedString = NUMPAD_SYMBOLS[mSelectedY][mSelectedX];

        IntervalPicker iPick = (IntervalPicker) getContext();
        iPick.handleNumPadSelection(selectedString);

        invalidate(mSelectedRect);
    }

    private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * mTileWidth), (int) (y * mTileHeight), (int) (x
                * mTileWidth + mTileWidth),
                (int) (y * mTileHeight + mTileHeight));
    }

}
