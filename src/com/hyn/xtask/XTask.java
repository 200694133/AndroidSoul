package com.hyn.xtask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;



public abstract class XTask<Result> implements IXFutureTask<Result>, 
							IXTask <Result>{
	private static final String LOG_TAG = XTask.class.getSimpleName();
	/**
	 * 
	 */
	private static final int HIGH_PRIORITY = 0;
	/**
	 * 
	 */
	private static final int MIDLLE_PRIORITY = 10;
	/**
	 * 
	 */
	private static final int LOW_PRIORITY = 20;	
	
	private int mPriority = MIDLLE_PRIORITY;
	private Status mStatus = Status.PENDING;
	private final AtomicBoolean mCancelled = new AtomicBoolean();
	
	
	public XTask(){
		
	}
	
	public void run(){
		setStatus(Status.RUNNING);
		try{
			Result res = runInBackground();
			setStatus(Status.FINISHED);
			if(isCanceled()){
				onCancelComplete() ;
			}else{
				onResult(res);
			}
		}catch(CancellationException e){
			onCancelComplete() ;
		}catch(Throwable t){
			onException(new XException(0, t.getMessage()));
		}finally{
			setStatus(Status.FINISHED);
		}
	}	
	
	@Override
	public IXFutureTask.Status getStatus() {
		return mStatus;
	}

	@Override
	public void setStatus(IXFutureTask.Status status) {
		mStatus = status;
	}


	@Override
	public boolean isCanceled() {
		return mCancelled.get();
	}


	@Override
	public int getPriority() {
		return mPriority;
	}


	@Override
	public void setPriority(int p) {
		mPriority = p;
	}


	@Override
	public void cancel() {
		mCancelled.set(true);
	}


	@Override
	public void checkIfCanceled() throws InterruptedException,
			CancellationException {
		if(mCancelled.get()){
			throw new CancellationException("Task has been canceled.");
		}
	}


	@Override
	public boolean isFinished() {
		return mStatus == Status.FINISHED;
	}


	@Override
	public void onProgressUpdate(Object... data) {
		
	}

	@Override
	public void onException(XException  exception) {
		
	}
	
	@Override
	public void onCancelComplete() {
		XLog.d(LOG_TAG, " onCancelComplete");
	}

	@Override
	public void onResult(Result result) {
		XLog.d(LOG_TAG, " onResult"+result);
	}

	/**
	 * Compare two task by priority, in order to enqueue the list by seqence.
	 */
	@Override
	public int compareTo(IXFutureTask<Result> o) {
		if(null == o) throw new NullPointerException("");
		int p1 = this.getPriority();
		int p2 = o.getPriority();
		if(p1 <= p2){
			return -1;
		}
		return 1;
	}
}
