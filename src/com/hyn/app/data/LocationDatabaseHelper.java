package com.hyn.app.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-11-17
 * Time: 下午9:07
 * This class is a helper class for operation database which store location information.
 */
class LocationDatabaseHelper extends SQLiteOpenHelper {
    private final static String TAG = LocationDatabaseHelper.class.getSimpleName();
    private final static String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/androidHelper_debug/";

    /** Database name. */
    private final static String DATABASE_NAME = "location_db";
    /** Table name. */
    private final static String TABLE_NAME="location";
    /** Field's name of primary key. */
    private final static String FIELD_ID="_id";
    /** Field's name of start time. */
    private final static String FIELD_START_TIME = "start_time";
    /** Field's name of end time. */
    private final static String FIELD_END_TIME = "end_time";
    /** Field's name of lat. */
    private final static String FIELD_LAT = "lat";
    /** Field's name of lng. */
    private final static String FIELD_LNG = "lng";
    /** Database version. */
    private final static int DATABASE_VERSION = 1;

    private SQLiteStatement mInsertStatement = null;
    private SQLiteStatement mDeleteStatement = null;
    private SQLiteStatement mUpdateStatement = null;

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.
     * use the default factory.
     * @param context to use to open or create the database
     */
    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_PATH+DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("ddd", "Current database path = " + context.getDatabasePath(DATABASE_PATH + DATABASE_NAME).getPath());
    }

    /**
     * Called when the database is created for the first time. This is where the creation
     * of create and init table.
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "OnCreate Database.");
        StringBuilder cmd = new StringBuilder();
        cmd.append("create table ").append(TABLE_NAME)
                .append(" ( ")
                .append(FIELD_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(FIELD_START_TIME).append(" Long NOT NULL, ")
                .append(FIELD_END_TIME).append(" Long NOT NULL, ")
                .append(FIELD_LAT).append(" DOUBLE NOT NULL, ")
                .append(FIELD_LNG).append(" DOUBLE NOT NULL")
                .append(" )");
        String sCmd = cmd.toString();
        Log.d(TAG, "execute command "+sCmd);
        db.execSQL(sCmd);
    }

    /**
     * Inserts a record in the table.
     * @param startTime the start time of the location
     * @param endTime the end time of the location
     * @param lat
     * @param lng
     * @return
     */
    public long insert(long startTime, long endTime, double lat, double lng) {
        if (mInsertStatement == null) {
            mInsertStatement = getWritableDatabase().compileStatement(
                    "INSERT INTO  " + TABLE_NAME + "("
                            + FIELD_START_TIME + ","
                            + FIELD_END_TIME + ","
                            + FIELD_LAT + ","
                            + FIELD_LNG
                            + ") VALUES (?,?,?,?)");
        }
        mInsertStatement.bindLong(1, startTime);
        mInsertStatement.bindLong(2, endTime);
        mInsertStatement.bindDouble(3, lat);
        mInsertStatement.bindDouble(4, lng);
        return mInsertStatement.executeInsert();
    }

    /**
     * Execute this SQL statement, if the the number of rows affected by execution of this SQL
     * statement.
     *
     * @return the number of rows affected by this SQL statement execution.
     * @throws android.database.SQLException If the SQL string is invalid for
     *         some reason
     */
    public int delete(long id) {
        if (mDeleteStatement == null) {
            mDeleteStatement = getWritableDatabase().compileStatement(
                    "DELETE FROM" + TABLE_NAME + " WHERE"
                            + FIELD_ID + " = ?");
        }
        mDeleteStatement.bindLong(1, id);
        return mInsertStatement.executeUpdateDelete();
    }

    /**
     * Update a entry.
     *
     * @return the number of rows affected by this SQL statement execution.
     * @throws android.database.SQLException If the SQL string is invalid for
     *         some reason
     */
    public int update(long id, long endTime){
        if (mUpdateStatement == null) {
            mUpdateStatement = getWritableDatabase().compileStatement(
                    "UPDATE " + TABLE_NAME + " SET "
                        + FIELD_END_TIME + " = ? WHERE "
                        + FIELD_ID + " = ?");
        }
        mUpdateStatement.bindLong(1, endTime);
        mUpdateStatement.bindLong(2, id);
        return mUpdateStatement.executeUpdateDelete();
    }

    /**
     * Query all entry from database.
     * @return first cursor
     */
    public Cursor queryAll(){
        return getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
    }

    /**
     * Query the last entry from database.
     * @return last cursor in database
     */
    public Cursor queryLastOne(){
        return getWritableDatabase().rawQuery("select * from " + TABLE_NAME + " order by " + FIELD_ID + " desc limit 1", null);
    }

    /**
     *
     */
    public Cursor query(long startTime, long endTime){
    	String sql = "select * from " + TABLE_NAME + " where "+FIELD_START_TIME + " >= "+startTime
    			+" and "+FIELD_END_TIME + " <= "+endTime+" order by " + FIELD_ID;
        return getWritableDatabase().rawQuery(sql, null);
    }

    /**
     * Called when the database needs to be upgraded.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade Database, older version is " + oldVersion + " , new version is " + newVersion);
        //delete the older database and create a newer.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Close any open database object and close any open statement.
     */
    public void close(){
        if(null != mInsertStatement){
            mInsertStatement.close();
        }
        mInsertStatement = null;
        if(null != mDeleteStatement){
            mDeleteStatement.close();
        }
        mDeleteStatement = null;
        if(null != mUpdateStatement){
            mUpdateStatement.close();
        }
        mUpdateStatement = null;
        super.close();
    }
    public static long getId(Cursor cursor){
        if(null == cursor) throw new NullPointerException("Cursor is empty.");
        return cursor.getLong(cursor.getColumnIndex(FIELD_ID));
    }

    public static double getLat(Cursor cursor){
        if(null == cursor) throw new NullPointerException("Cursor is empty.");
        return cursor.getDouble(cursor.getColumnIndex(FIELD_LAT));
    }

    public static double getLng(Cursor cursor){
        if(null == cursor) throw new NullPointerException("Cursor is empty.");
        return cursor.getDouble(cursor.getColumnIndex(FIELD_LNG));
    }

    public static long getStartTime(Cursor cursor){
        if(null == cursor) throw new NullPointerException("Cursor is empty.");
        return cursor.getLong(cursor.getColumnIndex(FIELD_START_TIME));
    }

    public static long getEndTime(Cursor cursor){
        if(null == cursor) throw new NullPointerException("Cursor is empty.");
        return cursor.getLong(cursor.getColumnIndex(FIELD_END_TIME));
    }
}
