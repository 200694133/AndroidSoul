package com.hyn.xtask;

public class XException extends RuntimeException{
	/**
	 * serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	int mErrorCode;
	String mMsg;
	
	public XException(int code, String msg){
		super(msg);
		mErrorCode = code;
		mMsg = msg;
	}

}
