package com.hyn.app.task;

import com.hyn.app.data.LocationEntry;
import com.hyn.app.data.LocationPixel;
import com.hyn.app.util.Disposable;
import com.hyn.app.util.SLog;
import com.hyn.app.util.SoulToken;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-12-25
 * Time: 下午6:55
 * This class is a task which to analyze from original data #{@link com.hyn.app.data.LocationEntry} to
 * display format data #{@link LocationPixel}.
 */
public class AnalyzerTask implements Runnable, Disposable {
    private static final String TAG = AnalyzerTask.class.getSimpleName();
    private long mStartMillTime = -1;
    private long mEndMillTime = -1;
    List<LocationEntry> mEntryList = null;
    private SoulToken mToken;
    private boolean isDisposed = false;
    private CommonResultCallback<LocationPixel> mCallback = null;
    public AnalyzerTask(List<LocationEntry> entries, long start, long end, SoulToken token){
        SLog.d(TAG, "Create new Task, Size = " + entries.size());
        mStartMillTime = start;
        mEndMillTime = end;
        mEntryList = entries;
        mToken = token;
    }

    SoulToken getToken(){
        return mToken;
    }

    void setCallback(CommonResultCallback<LocationPixel> callback){
        mCallback = callback;
    }

    /**
     * May be this will cost many times to analyze the data list, so please may sure this
     * function called in background thread.<b>please call this function in background thread.</b>
     */
    public void run(){
        if(!isValid()){
            notifyException("Input is invalid!");
            return ;
        }
        final double interval = mEndMillTime - mStartMillTime;
        List<LocationEntry> entryList = mEntryList;
        List<LocationPixel> locationPixelLis = new ArrayList<LocationPixel>();
        ListIterator<LocationEntry> iterator = entryList.listIterator();
        while(iterator.hasNext()){
            synchronized (this){
                if(isDisposed) return ;
            }
            LocationEntry data = iterator.next();
            LocationPixel pixel = new LocationPixel();
            pixel.setLat(data.getLat());
            pixel.setLng(data.getLng());
            pixel.setRatio((data.getEndMillTime() - data.getStartMillTime()) / interval);
            locationPixelLis.add(pixel);
        }
        mEntryList = null;
        notifyResults(locationPixelLis);
    }

    private boolean isValid(){
        synchronized (this){
            if(isDisposed) return false;
        }

        if(null == mEntryList || mEntryList.isEmpty()) return false;
        if(mEndMillTime <= 0 || mStartMillTime <= 0) return false;
        if(mEndMillTime<=mStartMillTime) return false;
        return true;
    }

    private void notifyException(String info){
        synchronized (this){
            if(isDisposed) return ;
        }

        if(null != mCallback){
            mCallback.onException(info);
        }
    }

    private void notifyResults(List<LocationPixel> list){
        synchronized (this){
            if(isDisposed) return ;
        }

        if(null != mCallback){
            mCallback.onResult(list);
        }
    }

    @Override
    public void dispose() {
        synchronized (this){
            isDisposed = true;
        }
    }
}
