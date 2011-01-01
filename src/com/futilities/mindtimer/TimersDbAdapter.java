package com.futilities.mindtimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TimersDbAdapter {
    public static final String KEY_LABEL = "label";
    public static final String KEY_SECONDS = "seconds";
    public static final String KEY_STARTED_AT_MILLIS_SINCE_BOOT = "started_at_millis_since_boot";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "TimersDbAdapter";
    private static final String DATABASE_TABLE = "timers";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "mind_timer_data";
        private static final int DATABASE_VERSION = 6;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String populateSchemaSql = "create table " + DATABASE_TABLE + " ("
                    + "_id integer primary key autoincrement, "
                    + "label text not null, " + "seconds integer not null, "
                    + "started_at_millis_since_boot integer" + ");"
                    + "create index if not exists label_text on timers(label);";

            db.execSQL(populateSchemaSql);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS timers");
            onCreate(db);

        }

    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx
     *            the Context within which to work
     */
    public TimersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public TimersDbAdapter() {
        this.mCtx = null;
    }

    public TimersDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long create(String label, int intervalSeconds) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LABEL, label);
        initialValues.put(KEY_SECONDS, intervalSeconds);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean delete(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public void truncate() {
        String sql = "delete from " + DATABASE_TABLE;

        mDb.execSQL(sql);
    }

    public Cursor fetchAll() {

        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LABEL,
                KEY_SECONDS, KEY_STARTED_AT_MILLIS_SINCE_BOOT }, null, null,
                null, null, null);
    }

    public Cursor fetchOne(long rowId) throws SQLException {

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_LABEL, KEY_SECONDS,
                KEY_STARTED_AT_MILLIS_SINCE_BOOT }, KEY_ROWID + "=" + rowId,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;

    }

    public Cursor fetchWhere(String whereClause) {
        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LABEL,
                KEY_SECONDS, KEY_STARTED_AT_MILLIS_SINCE_BOOT }, whereClause,
                null, null, null, KEY_ROWID);
    }

    public boolean update(long rowId, String label, int intervalSeconds) {
        ContentValues args = new ContentValues();
        args.put(KEY_LABEL, label);
        args.put(KEY_SECONDS, intervalSeconds);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean update(long rowId, long startedAtMillisSinceBoot) {
        ContentValues args = new ContentValues();
        args.put(KEY_STARTED_AT_MILLIS_SINCE_BOOT, startedAtMillisSinceBoot);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
