package com.hyn.app.task;

import android.content.Context;
import com.hyn.app.data.LocationDAO;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.util.Disposable;
import com.hyn.app.util.SoulToken;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-5
 * Time: 下午3:29
 *
 */
public class RequestTask implements Runnable, Disposable{
    Context mContext;
    long mStart, mEnd;
    private SoulToken mToken = null;
    private CommonResultCallback<LocationEntry> mCallback = null;
    public RequestTask(Context context, long start, long end, SoulToken token){
        mContext = context;
        mStart = start;
        mEnd = end;
        mToken = token;
    }

    SoulToken getToken(){
        return mToken;
    }

    void setCallback(CommonResultCallback<LocationEntry> callback){
        mCallback = callback;
    }

    @Override
    public void run() {
        Context context = mContext;
        mContext = null;
        LocationDAO dao = null;
        try{
            dao = new LocationDAO(context);
            List<LocationEntry> entryList = dao.query(mStart, mEnd);
            notifyResult(entryList);
        }catch(Exception e){
            notifyException(""+e);
        }finally{
            if(null != dao) dao.dispose();
        }
    }

    private void notifyResult(List<LocationEntry> entryList){
        if(null == entryList) return ;
        if(null != mCallback){
            mCallback.onResult(entryList);
        }
    }

    private void notifyException(String info){
        if(null != mCallback){
            mCallback.onException(info);
        }
    }

    @Override
    public void dispose() {

    }
}
