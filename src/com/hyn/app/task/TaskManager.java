package com.hyn.app.task;


import android.content.Context;
import android.os.AsyncTask;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.data.LocationPixel;
import com.hyn.app.util.SLog;
import com.hyn.app.util.SoulToken;

import java.util.List;
import java.util.WeakHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-4
 * Time: 下午5:49
 * To change this template use File | Settings | File Templates.
 */
public class TaskManager {
    private static final String TAG = TaskManager.class.getSimpleName();

    private static TaskManager sInstance = null;

    WeakHashMap<AnalyzerTask, SoulToken> mWorkTaskMap = new WeakHashMap<AnalyzerTask, SoulToken>();

    public static synchronized TaskManager getsInstance(){
        if(null == sInstance){
            sInstance = new TaskManager();
        }
        return sInstance;
    }

    private TaskManager(){

    }


    public void postRequest(Context context, final RequestObject input,
                            final CommonResultCallback<LocationPixel> callback){
        final SoulToken token = SoulToken.getNewToken();
        RequestTask requestTask = new RequestTask(context, input.mStart,
                                input.mEnd, token);

        final CommonResultCallback<LocationEntry> requestCallback = new CommonResultCallback<LocationEntry>() {
            @Override
            public void onException(String info) {
                SLog.e(TAG, "requestCallback exception " + info);
                //TODO
            }
            @Override
            public void onResult(List<LocationEntry> results) {
                SLog.i(TAG, "onResult size "+results.size());
                AnalyzerTask analyzerTask = new AnalyzerTask(results, input.mStart,
                                input.mEnd, token);
                analyzerTask.setCallback(callback);
                AsyncTask.execute(analyzerTask);
            }
        };
        requestTask.setCallback(requestCallback);
        AsyncTask.execute(requestTask);
    }

    /**
     * Running a new task to work task queue.
     * @param task waiting for running task.
     */
    public void run(AnalyzerTask task){
        if(mWorkTaskMap.containsKey(task)){
            return ;
        }

        mWorkTaskMap.put(task, SoulToken.getNewToken());
    }

    /**
     * Cancel a running task.
     * @param task
     */
    public void cancel(AnalyzerTask task){
        mWorkTaskMap.remove(task);
    }



    public static class RequestObject{
        public long mStart;
        public long mEnd;
    }
}
