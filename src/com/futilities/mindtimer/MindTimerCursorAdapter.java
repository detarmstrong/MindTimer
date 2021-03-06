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

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.futilities.mindtimer.HourGlass.TimerState;

public class MindTimerCursorAdapter extends CursorAdapter {

    private static final String TAG = "MINDTIMERCURSORADAPTER";
    private Context mContext;
    private HashMap<Long, HourGlass> mHourGlassMap = new HashMap<Long, HourGlass>();

    public MindTimerCursorAdapter(Context context, Cursor c) {
        super(context, c);

        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MindTimerListItemView rowLayout = (MindTimerListItemView) view;

        long id = cursor.getLong(cursor
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_ROWID));

        String label = cursor.getString(cursor
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL));

        int secondsDuration = cursor.getInt(cursor
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_SECONDS));

        String minutePart = String.valueOf(cursor.getInt(cursor
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_MINUTE_LABEL)));

        String hourPart = String.valueOf(cursor.getInt(cursor
                .getColumnIndexOrThrow(TimersDbAdapter.KEY_HOUR_LABEL)));

        // TODO toColonFormat should be somewhere else, a "util" class? A Timer
        // class?
        String formattedDuration = TimerEdit
                .toColonFormat(hourPart, minutePart);

        String thumbnailFilePath = String
                .valueOf(cursor.getString(cursor
                        .getColumnIndexOrThrow(TimersDbAdapter.KEY_THUMBNAIL_ABSOLUTE_PATH)));

        long startedAtMillisSinceBoot = cursor
                .getLong(cursor
                        .getColumnIndexOrThrow(TimersDbAdapter.KEY_STARTED_AT_MILLIS_SINCE_BOOT));
        long deadline = cursor
                .getLong(cursor
                        .getColumnIndexOrThrow(TimersDbAdapter.KEY_DEADLINE_MILLIS_SINCE_BOOT));

        // Call methods in view used to populate fields
        rowLayout.setTimerId(id);
        rowLayout.setLabel(label);
        rowLayout.setDurationLabel(formattedDuration);
        rowLayout.setSecondsDuration(secondsDuration);

        if ((thumbnailFilePath == null)
                || (thumbnailFilePath != null && thumbnailFilePath == "null")) {
            thumbnailFilePath = "";
        }

        rowLayout.setThumbnail(thumbnailFilePath);

        rowLayout.setDeadline(deadline);

        // set onclick handlers
        rowLayout.setEditClickListener();
        rowLayout.setTimerControlClickListener();

        TimerState state = TimerState.NOT_STARTED;
        if (deadline > SystemClock.elapsedRealtime()) {
            state = TimerState.RUNNING;
        } else if (deadline > 0) {
            state = TimerState.FINISHED;
        }

        HourGlass glass = new HourGlass(mContext, id, 0, 0, secondsDuration);
        glass.setTimerState(state);
        mHourGlassMap.put(id, glass);

        rowLayout.setTimerState(state);

        rowLayout.updateProgress();

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new MindTimerListItemView(context);
    }

    public HashMap<Long, HourGlass> getHourGlassMap() {
        return mHourGlassMap;
    }

    // Commented because requery() causes bindView(), which updates views
    // with latest data
    // @Override
    // public View getView(int position, View convertView, ViewGroup parent) {
    // MindTimerListItemView timerView;
    //
    // if (convertView != null) {
    // Cursor timer = (Cursor) getItem(position);
    // timerView = (MindTimerListItemView) convertView;
    // timerView.setLabel(timer.getString(timer
    // .getColumnIndexOrThrow(TimersDbAdapter.KEY_LABEL)));
    //
    // return timerView;
    //
    // } else {
    // return super.getView(position, convertView, parent);
    //
    // }
    //
    // }

}
