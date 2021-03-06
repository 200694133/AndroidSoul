package com.hyn.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

public class FunctionUtil {
	public static final double RADIUS_OF_EARTH_METERS = 6371009;
	
	/** Generate LatLng of radius marker */
	public static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

	public static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }
	
	public static boolean isRunOnUiThread(){
		return Looper.myLooper() == Looper.getMainLooper();
	}
	public static boolean locationEquals(double d1, double d2){
        if(Math.abs(d1-d2) <= 0.00000000001){
            return true;
        }
        return false;
    }
	
	public DateInfo parseDateInfo(long t){
		return new DateInfo(t);
	}
	
	public static class DateInfo{
		public long mMillTimes;
		public String mDes;
		public DateInfo(long milltime){
			mMillTimes = milltime;
			sync();
		}
		void sync(){
			SimpleDateFormat  sDateFormat = new SimpleDateFormat("MM-dd");  
			Date curDate = new Date(mMillTimes);
			mDes = sDateFormat.format(curDate);
		}
		
		public String toString(){
			return mDes;
		}
	}
}
