package com.hyn.app.util;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-13
 * Time: 上午10:21
 */
public interface Disposable {
    /**
     * Called when class need recycled allocate memory.
     */
    public void dispose();

    /**
     * Check if task is disposed.
     * @return true means that this task is disposed.
     *          false means that this task is not disposed.
     */
    public boolean isDisposed();
}
