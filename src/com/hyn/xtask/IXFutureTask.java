package com.hyn.xtask;

import java.util.concurrent.CancellationException;

/**
 * 
 * @author Yananh
 *
 */
public interface IXFutureTask<Result> extends Comparable<IXFutureTask<Result>>, Runnable{
    /**
     * Indicates the current status of the task. Each status will be set only once
     * during the lifetime of a task.
     */
    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that task has finished.
         */
        FINISHED,
    }
    
    public enum TerminalStage{
    	/**
    	 * Indicated that the last state of the task is failed.
    	 */
    	FAILED,
    	/**
    	 * 
    	 */
    	NORMAL,
    	/**
    	 * 
    	 */
    	CANCELED,
    }
    
	
	public static class TerminalResult<Result>{
		IXFutureTask.TerminalStage mTerminalStage;
		Result mResult;
		XException mException;
		TerminalResult(){}
	}
    
    public Status getStatus();
    	
	public void setStatus(Status status);
	
	public boolean isCanceled();
	
	public int getPriority();
	
	public void setPriority(int p);
	
	/**
	 * Check if the current task has canceled.
	 */
	public void cancel();
	
	public void checkIfCanceled() throws InterruptedException, CancellationException;
	
	/**
	 * Check if the current task has finished.
	 * @return 
	 */
	public boolean isFinished();
	
	public void onProgressUpdate(Object ... data);
	
	/**
	 * When some exception occurred, call this function to notify the observer.
	 * <b>It's must work in the background thread.</b>
	 * @see #{@link #onCancelComplete()}
	 * @see #{@link #onResult(Result)}
	 * @param e
	 */
	public void onException(XException  exception);
	
	/**
	 * Invoke this function when user canceled this task.
	 * <b>It's must work in the background thread.</b>
	 * @see #{@link #onException(XException)}
	 * @see #{@link #onResult(Result)}
	 * @param info
	 */
	public void onCancelComplete();
	
	/**
	 * Invoke this function when this task running complete.
	 * <b>It's must work in the background thread.</b>
	 * @see #{@link #onException(XException)}
	 * @see #{@link #onCancelComplete()}
	 * @param result the result after calculate.
	 */
	public void onResult(Result result);

}
