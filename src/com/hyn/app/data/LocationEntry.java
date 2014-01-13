package com.hyn.app.data;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-11-17
 * Time: 下午11:00
 * A entry store at database. A entry mean that the time user stayed where a
 * little area.
 * */
public class LocationEntry implements java.io.Serializable{
    static final long serialVersionUID = 51556253L;
    private long mId;
    private double mLat;
    private double mLng;
    private long mStartMillTime;
    private long mEndMillTime;

    public LocationEntry(){

    }
    public LocationEntry(long start, long end, double lat, double lng){
        mLat = lat;
        mLng = lng;
        mStartMillTime = start;
        mEndMillTime = end;
    }


    /** the primary key of a entry */
    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    /** the latitude, in degrees. */
    public double getLat() {
        return mLat;
    }

    public void setLat(double mLat) {
        this.mLat = mLat;
    }

    /** the longitude, in degrees. */
    public double getLng() {
        return mLng;
    }

    public void setLng(double mLng) {
        this.mLng = mLng;
    }

    /** the start time, unit is mill time, which offset is 1970.1.1 */
    public long getStartMillTime() {
        return mStartMillTime;
    }

    public void setStartMillTime(long mStartMillTime) {
        this.mStartMillTime = mStartMillTime;
    }

    /** the end time, unit is mill time, which offset is 1970.1.1 */
    public long getEndMillTime() {
        return mEndMillTime;
    }

    public void setEndMillTime(long mEndMillTime) {
        this.mEndMillTime = mEndMillTime;
    }
}
