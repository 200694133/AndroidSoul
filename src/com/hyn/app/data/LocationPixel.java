package com.hyn.app.data;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.hyn.app.util.FunctionUtil;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-12-10
 * Time: 下午9:16
 * To change this template use File | Settings | File Templates.
 */
public class LocationPixel {
	protected LatLng mCenter = null;
	protected double mRatio = 0.0;
    protected Circle mCircle = null;
    protected double mRadius;
    protected int mFillColor;
    
    public LocationPixel(LatLng center, LatLng radiusLatLng){
    	mCenter = center;
    	mRadius = FunctionUtil.toRadiusMeters(center, radiusLatLng);
    	
    	setupIfNeed();
    }
    
    public LocationPixel(LatLng center, double radius){
    	mCenter = center;
    	mRadius = radius;
    	
    	setupIfNeed();
    }
    
    public LocationPixel(double lat, double lng, double radius){
    	mCenter = new LatLng(lat, lng);
    	mRadius = radius;
    	
    	setupIfNeed();
    }
    
    public LatLng getCenter(){
    	return mCenter;
    }
    public double getRatio() {
        return mRatio;
    }

    public void setRatio(double mRatio) {
        this.mRatio = mRatio;
    }
    
    private void setupIfNeed(){
    	if(mRatio <= 0.00001) return ;
    }    
}
