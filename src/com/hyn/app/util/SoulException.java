package com.hyn.app.util;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-11-22
 * Time: 上午10:36
 * Represents an error condition specific to the application.
 */
public class SoulException extends RuntimeException{
    static final long serialVersionUID = 1;

    /**
     * Constructs a new Exception.
     */
    public SoulException() {
        super();
    }

    /**
     * Constructs a new Exception.
     *
     * @param message
     *            the detail message of this exception
     */
    public SoulException(String message) {
        super(message);
    }

    /**
     * Constructs a new Exception.
     *
     * @param message
     *            the detail message of this exception
     * @param throwable
     *            the cause of this exception
     */
    public SoulException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new Exception.
     *
     * @param throwable
     *            the cause of this exception
     */
    public SoulException(Throwable throwable) {
        super(throwable);
    }
}
