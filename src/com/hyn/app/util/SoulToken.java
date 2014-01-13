package com.hyn.app.util;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-12-25
 * Time: 下午8:00
 * A token to indicate the state of the task. When create a task need a
 * SoulToken to indicate that if the task is validate.
 */
public class SoulToken {
    static WeakReference<SoulToken> mNewestToken = new WeakReference<SoulToken>(null);
    public static synchronized SoulToken getNewToken(){
        SoulToken next = new SoulToken();
        mNewestToken = new WeakReference<SoulToken>(next);
        return next;
    }

    public static synchronized SoulToken getCurrToken(){
        return mNewestToken.get();
    }

    private SoulToken(){

    }
}
