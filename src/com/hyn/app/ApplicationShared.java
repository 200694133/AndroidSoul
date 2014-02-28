package com.hyn.app;

import android.app.Application;
import android.util.Log;
import com.hyn.app.util.Disposable;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-13
 * Time: 上午9:44
 * This class is used to store the static var which will shared in global application.
 * It's provide a convenience way to store and access the static vars, which may be cause
 * memory leaks. Make sure that it must be disposed when application has existed, in other
 * words, when exist the application, the function #{@link #dispose()} must be called to
 * recycle the resource, especially clear static variable.
 */
public class ApplicationShared extends Application implements Disposable {
    /** class name, used for logcat */
    private static final String TAG = ApplicationShared.class.getSimpleName();


    /** @{hide} */
    private AttatchInfo mAttatchInfo;

    static{

    }

    /**
     * Called when the application is created.
     */
    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "Application is onCreate");
        if(BuildConfig.DEBUG){

        }
    }

    /**
     * Used for debug low memory occurred.
     */
    @Override
    public void onLowMemory() {
        if(BuildConfig.DEBUG){
            Log.d(TAG, "onLowMemory");
        }
        super.onLowMemory();
    }

    /**
     * occurred when memory is too lowly.
     * @param level the level of memory, if want to know detail more, please read reference
     *              http://developer.android.com/training/articles/memory.html
     */
    @Override
    public void onTrimMemory(int level){
        if(BuildConfig.DEBUG){
            Log.d(TAG, "onTrimMemory, level "+level);
        }
        super.onTrimMemory(level);
    }


    /**
     * Release the resource hold by application, especially release the static vars.
     * If this function is not called, the application may be in a unknow state.
     */
    @Override
    public void dispose() {

    }

    @Override
    public boolean isDisposed() {
        return false; 
    }

    /**
     * A set of information given to this application when it is running.
     */
    static class AttatchInfo{

    }
}
