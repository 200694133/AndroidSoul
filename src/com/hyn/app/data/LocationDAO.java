package com.hyn.app.data;

import android.content.Context;
import android.database.Cursor;
import com.hyn.app.util.Disposable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-18
 * Time: 上午11:22
 * This class is a Data Access Object. It's provide interface and method to
 * add/delete/update/query location information.
 * @see com.hyn.app.data.LocationEntry
 */
public class LocationDAO implements Disposable {
    private static final String TAG = LocationDAO.class.getSimpleName();
    /** database helper.  */
    LocationDatabaseHelper mLocationDatabaseHelper= null;

    /** Create a DAO. */
    public LocationDAO(Context context){
        mLocationDatabaseHelper = new LocationDatabaseHelper(context);
    }

    /**
     * insert a entry to database.
     * @param entry the entry need to insert to database
     * @return the key id of the new entry
     */
    public LocationEntry insert(LocationEntry entry){
        if(null == entry) throw new NullPointerException("insert a empty location.");
        long id = mLocationDatabaseHelper.insert(entry.getStartMillTime(),
                entry.getEndMillTime(),entry.getLat(), entry.getLng());
        entry.setId(id);
        return entry;
    }

    /**
     * Update a entry in database.
     * @param entry the entry need to update
     */
    public void update(LocationEntry entry){
        if(null == entry) throw new NullPointerException("update a empty location.");
        mLocationDatabaseHelper.update(entry.getId(), entry.getEndMillTime());
    }

    /**
     * Delete a entry from database.
     * @param entry the entry need to delete
     */
    public void delete(LocationEntry entry){
        if(null == entry) throw new NullPointerException("Delete a empty location.");
        mLocationDatabaseHelper.delete(entry.getId());
    }

    /**
     * get all
     * @return
     */
    public List<LocationEntry> queryAll(){
        Cursor cursor = mLocationDatabaseHelper.queryAll();
        List<LocationEntry> locations = new LinkedList<LocationEntry>();
        while(cursor.moveToNext()){
            LocationEntry entry = new LocationEntry();
            entry.setId(mLocationDatabaseHelper.getId(cursor));
            entry.setLat(mLocationDatabaseHelper.getLat(cursor));
            entry.setLng(mLocationDatabaseHelper.getLng(cursor));
            entry.setStartMillTime(mLocationDatabaseHelper.getStartTime(cursor));
            entry.setEndMillTime(mLocationDatabaseHelper.getEndTime(cursor));
            locations.add(entry);

        }
        return locations;
    }

    public List<LocationEntry> query(final long start, final long end){
        Cursor cursor = mLocationDatabaseHelper.query(start, end);
        List<LocationEntry> locations = new LinkedList<LocationEntry>();
        while(cursor.moveToNext()){
            LocationEntry entry = new LocationEntry();
            entry.setId(LocationDatabaseHelper.getId(cursor));
            entry.setLat(LocationDatabaseHelper.getLat(cursor));
            entry.setLng(LocationDatabaseHelper.getLng(cursor));
            entry.setStartMillTime(LocationDatabaseHelper.getStartTime(cursor));
            entry.setEndMillTime(LocationDatabaseHelper.getEndTime(cursor));
            locations.add(entry);

        }
        return locations;
    }

    public List<LocationEntry> query(final long start, final long end, final int page, final int pageSize){
        int offset = page * pageSize;
        Cursor cursor = mLocationDatabaseHelper.query(start, end);
        int count = cursor.getCount();
        if(count < offset) return null;

        List<LocationEntry> locations = new LinkedList<LocationEntry>();
        while(cursor.moveToNext()){
            LocationEntry entry = new LocationEntry();
            entry.setId(LocationDatabaseHelper.getId(cursor));
            entry.setLat(LocationDatabaseHelper.getLat(cursor));
            entry.setLng(LocationDatabaseHelper.getLng(cursor));
            entry.setStartMillTime(LocationDatabaseHelper.getStartTime(cursor));
            entry.setEndMillTime(LocationDatabaseHelper.getEndTime(cursor));
            locations.add(entry);

        }
        return locations;
    }


    public LocationEntry getLastEntry(){
        Cursor cursor = mLocationDatabaseHelper.queryLastOne();

        if(cursor.moveToNext()){
            LocationEntry entry = new LocationEntry();
            entry.setId(mLocationDatabaseHelper.getId(cursor));
            entry.setLat(mLocationDatabaseHelper.getLat(cursor));
            entry.setLng(mLocationDatabaseHelper.getLng(cursor));
            entry.setStartMillTime(mLocationDatabaseHelper.getStartTime(cursor));
            entry.setEndMillTime(mLocationDatabaseHelper.getEndTime(cursor));
            return entry;

        }

        return null;
    }

    @Override
    public synchronized void dispose() {
        mLocationDatabaseHelper.close();
        mLocationDatabaseHelper = null;
    }

    @Override
    public boolean isDisposed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
