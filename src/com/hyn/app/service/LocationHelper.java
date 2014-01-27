package com.hyn.app.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.hyn.app.util.FunctionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-20
 * Time: 上午9:21
 * This is a helper class to get the location at time interval or distance interval.
 */
class LocationHelper implements LocationListener{
    private static final String TAG = LocationHelper.class.getSimpleName();
    /**
     * min distance of division, if user moves distance is less than this value,
     * app consider it's in a same position.
     * */
    private static final int LOCATION_MIN_DISTANCE_DIVISION = 500;
    /**
     * min division time. Set this time to avoid create too many data to slow the system.
     */
    private static final int LOCATION_MIN_TIME_DIVISION = 1000 * 60 * 10;//10 mins

    /** gps provider tag, if gps is available, use this tag to listen the location state. */
    private static final String GPS_PROVIDE = LocationManager.GPS_PROVIDER;
    /** network provider tag, if gps is unavailable, use this base station to get location state. */
    private static final String BASE_STATION_PROVIDE = LocationManager.NETWORK_PROVIDER;

    /** the time interval to update once. */
    int mTimeInterval = LOCATION_MIN_TIME_DIVISION;
    /** the distance interval to update once. */
    int mDistanceInterval = LOCATION_MIN_DISTANCE_DIVISION;

    /** WrapContext for getting system service. */
    Context mContext = null;
    /** GPS Service manager */
    LocationManager mLocationManager = null;

    final Handler mHandler = new Handler(Looper.myLooper());

    ListenerInfo mListenerInfo = null;

    private long mPrevUpdateTime = -1;

    private double mPrevLat = 0;
    private double mPrevLng = 0;

    static class ListenerInfo {
        /** when system trigger update new location, need invoke this callback. */
        List<LocationCallback> mLocationCallbacks = new ArrayList<LocationCallback>();
        synchronized void addListener(LocationCallback callback){
            mLocationCallbacks.add(callback);
        }
        synchronized void removeListener(LocationCallback callback){
            mLocationCallbacks.remove(callback);
        }

        synchronized void notifyLocationInfo(long start, long end, double lat, double lng){
            List<LocationCallback> callbacks = new ArrayList<LocationCallback>(mLocationCallbacks);
            for(LocationCallback callback : callbacks){
                callback.notifyLocation(start, end, lat, lng);
            }
        }

        synchronized void notifyLocationError(Object object){
            List<LocationCallback> callbacks = new ArrayList<LocationCallback>(mLocationCallbacks);
            for(LocationCallback callback : callbacks){
                callback.onError(object);
            }
        }

        synchronized void clear(){
            mLocationCallbacks.clear();
        }
    }

    ListenerInfo getListenerInfo(){
        if(null == mListenerInfo){
            mListenerInfo = new ListenerInfo();
        }
        return mListenerInfo;
    }

    LocationHelper(LocationCallback callback, Context context){
        this(LOCATION_MIN_TIME_DIVISION, LOCATION_MIN_DISTANCE_DIVISION, callback, context);
    }

    LocationHelper(int timeInterval, int distanceInterval, LocationCallback callback, Context context){
        mTimeInterval = timeInterval;
        mDistanceInterval = distanceInterval;
        getListenerInfo().addListener(callback);
        mContext = context;
    }

    /**
     * This class will be called if there is no any update during the past ten minutes.
     * It's wake up the system to get the last location and notify callbacks.
     */
    Runnable mWakeUpRunnable = new Runnable() {
        @Override
        public void run() {
            Location location = null;
            LocationManager locationManager = mLocationManager;
            if(null == locationManager) {
            	Log.e(TAG, "In Wake Up Runnable, LocationManager has finished!");
            	return ;
            }
            if(locationManager.isProviderEnabled(GPS_PROVIDE)){
                Log.i(TAG, "WakeUp Runnable Support GPS");
                location = locationManager.getLastKnownLocation(GPS_PROVIDE);
            }else if(locationManager.isProviderEnabled(BASE_STATION_PROVIDE)){
                Log.i(TAG, "WakeUp Runnable Support NETWORK");
                location = locationManager.getLastKnownLocation(BASE_STATION_PROVIDE);
            }
            if(null == location) {
                Log.e(TAG, "Cannot get location in wake up runnable. Delay 10 minutes to run again. ");
                mHandler.postDelayed(this, LOCATION_MIN_TIME_DIVISION);
            }else{
                onLocationChanged(location);
            }
        }
    };


    /**
     * If cannot get the location currently, delay some times to try again.
     */
    Runnable mTryAgainRunnable = new Runnable(){
        @Override
        public void run() {
            start();
        }
    };
    /**
     * Start listen the state of position.
     */
    final synchronized void start(){
        mHandler.removeCallbacks(null);
        if(null == mLocationManager){
            mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        LocationManager locationManager = mLocationManager;
        if(null == locationManager){
        	Log.e(TAG, "Can not get LocationManager Service. Delay "+mTimeInterval+" to run again.");
        	mHandler.postDelayed(mTryAgainRunnable, mTimeInterval);
        	return ;
        }
        if(locationManager.isProviderEnabled(GPS_PROVIDE)){
            Log.i(TAG, "Support GPS");
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDE);
            onLocationChanged(location);
            locationManager.requestLocationUpdates(GPS_PROVIDE, 30000, 500, this);
            mHandler.postDelayed(mWakeUpRunnable, LOCATION_MIN_TIME_DIVISION);
        }else if(locationManager.isProviderEnabled(BASE_STATION_PROVIDE)){
            Log.i(TAG, "Support NETWORK");
            Location location = locationManager.getLastKnownLocation(BASE_STATION_PROVIDE);
            onLocationChanged(location);
            locationManager.requestLocationUpdates(BASE_STATION_PROVIDE, 30000, 500, this);
            mHandler.postDelayed(mWakeUpRunnable, LOCATION_MIN_TIME_DIVISION);
        }else{
            Log.i(TAG, "Support NONE");
            getListenerInfo().notifyLocationError(new RuntimeException("Not Support GPS and Network Location."));
            mHandler.postDelayed(mTryAgainRunnable, mTimeInterval);
        }
    }
    /**
     * Stop listen the state of position.
     */
    final synchronized void stop(){
    	if(null != mLocationManager){
            mLocationManager.removeUpdates(this);
        }
        mHandler.removeCallbacks(mTryAgainRunnable);
        mHandler.removeCallbacks(mWakeUpRunnable);
    }

    /**
     * Dispose this class, it's useless and should recycle to system.
     * */
    final synchronized void dispose(){
        //update the last location to callbacks.
        if(mPrevUpdateTime>0){
            getListenerInfo().notifyLocationInfo(mPrevUpdateTime, System.currentTimeMillis(), mPrevLat, mPrevLng);
        }
        mHandler.removeCallbacks(mTryAgainRunnable);
        mHandler.removeCallbacks(mWakeUpRunnable);
        getListenerInfo().clear();
        mContext = null;
        mLocationManager = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location change to" + location);
        if(null == location){
            //getListenerInfo().notifyLocationError(new RuntimeException("Get empty Location."));
            return ;
        }
        long curr = System.currentTimeMillis();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        if(mPrevUpdateTime <= 0){//Running on the first time
            mPrevUpdateTime = curr;
            mPrevLat = lat;
            mPrevLng = lng;
            return ;
        }

        if(FunctionUtil.locationEquals(lat, mPrevLat) && FunctionUtil.locationEquals(lng, mPrevLng)){
            return ;
        }
        getListenerInfo().notifyLocationInfo(mPrevUpdateTime, curr, mPrevLat, mPrevLng);//previous location with time
        mPrevUpdateTime = curr;
        mPrevLat = lat;
        mPrevLng = lng;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, provider + " has enabled!");
        start();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider + " has disabled!");
        start();
    }


    static interface LocationCallback{
        public void notifyLocation(long startTime, long endTime, double lat, double lng);
        public void onError(Object e);
    }
}
