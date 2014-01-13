package com.hyn.app.task;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-1-5
 * Time: 下午8:04
 */
public interface CommonResultCallback<T> {
    public void onException(String info);
    public void onResult(List<T> results);
}
