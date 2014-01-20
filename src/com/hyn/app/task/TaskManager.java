package com.hyn.app.task;


import com.hyn.xtask.IXFutureTask;
import com.hyn.xtask.XAsyncTask;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-4
 * Time: 下午5:49
 */
public class TaskManager {
    private static final String TAG = TaskManager.class.getSimpleName();

    private static TaskManager sInstance = null;

    XAsyncTask mXAsyncTask;
    public static synchronized TaskManager getsInstance(){
        if(null == sInstance){
            sInstance = new TaskManager();
        }
        return sInstance;
    }

    private TaskManager(){
        mXAsyncTask = new XAsyncTask(1);
    }

    public void postTask(IXFutureTask task){
        mXAsyncTask.post(task);
    }

    public void cancel(IXFutureTask task){
        mXAsyncTask.cancel(task);
    }
}
