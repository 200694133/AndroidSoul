package com.hyn.app.util;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-11-22
 * Time: 上午10:25
 * A class to help print debug program. user could change the flag to decide if
 * open/close the level.
 */
public class SLog {
    /** A flag to indicate if print verbose level' log. */
    private final static boolean V_FLAG = true;
    /** A flag to indicate if print debug level' log. */
    private final static boolean D_FLAG = true;
    /** A flag to indicate if print warning level' log. */
    private final static boolean W_FLAG = true;
    /** A flag to indicate if print info level' log. */
    private final static boolean I_FLAG = true;
    /** A flag to indicate if print error level' log. */
    private final static boolean E_FLAG = true;


    public final static void d(final String tag, final String content){
        if(D_FLAG){
            Log.d(tag, content);
        }
    }

    public final static void i(final String tag, final String content){
        if(I_FLAG){
            Log.i(tag, content);
        }
    }

    public final static void w(final String tag, final String content){
        if(W_FLAG){
            Log.w(tag, content);
        }
    }

    public final static void e(final String tag, final String content){
        if(E_FLAG){
            Log.e(tag, content);
        }
    }

    public final static void v(final String tag, final String content){
        if(V_FLAG){
            Log.v(tag, content);
        }
    }
}
