package com.hyn.xtask;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class XAsyncTask {
	private static final String LOG_TAG = XAsyncTask.class.getSimpleName();
	private static final AtomicInteger sPoolSize = new AtomicInteger(0);
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    
    private static final int QUEUE_THRESHOLD = 5;
	
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
        	sPoolSize.incrementAndGet();
            return new Thread(r, "XAsyncTask #" + mCount.getAndIncrement());
        }
    };
	
    /**
     * work Queue.
     */
	private final BlockingQueue<IXFutureTask<?>> mPoolWaitingQueue = new PriorityBlockingQueue<IXFutureTask<?>>();
	private final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<Runnable>(QUEUE_THRESHOLD<<2);
	ReentrantLock mWorkQueueLock = new ReentrantLock();
	private final WeakHashMap<IXFutureTask<?>, FutureTask<?>> mTaskFutureMap =
			new WeakHashMap<IXFutureTask<?>, FutureTask<?>>();
	/**
	 * Thread pool.
	 */
	private Executor mWorkExecutor = null;
	
	public XAsyncTask(int capability){
		mWorkExecutor = new  ThreadPoolExecutor(CORE_POOL_SIZE,  MAXIMUM_POOL_SIZE, KEEP_ALIVE, 
				TimeUnit.SECONDS,  mWorkQueue, sThreadFactory);
		
		
	}
	
	private void sheduleNext(){
		mWorkQueueLock.lock();
		if(mWorkQueue.size() <= QUEUE_THRESHOLD){
			IXFutureTask<?> task = mPoolWaitingQueue.poll();
			if(null != task){
				FutureTask<?> futuretask = parseSubmitTask(task);
				mTaskFutureMap.put(task, futuretask);
				mWorkExecutor.execute(futuretask);
			}
		}
		mWorkQueueLock.unlock();
	}
	
	private FutureTask<Void> parseSubmitTask(final IXFutureTask<?> task){
		FutureTask<Void> futureTask = new FutureTask<Void>(task, null){
			 	public boolean cancel(boolean mayInterruptIfRunning) {
			 		task.cancel();
			        return super.cancel(mayInterruptIfRunning);
			    }
			@Override
			protected void done() {
				try {
					get();
				} catch (CancellationException e) {
					notifyCancelComplete(task);
				} catch(Throwable e){
					notifyExcption(task, new XException(0, e.getMessage()));
				}finally{
					mWorkQueueLock.lock();
					mTaskFutureMap.remove(task);
					//mWorkQueue.remove(task);
					mWorkQueueLock.unlock();
					sheduleNext();
				}
			}
		};
		return futureTask;
	}
	
	
	public void post(IXFutureTask<?> task){
		mPoolWaitingQueue.offer(task);
		sheduleNext();
	}
	
	public void cancel(IXFutureTask<?> task){
		mWorkQueueLock.lock();
		if(mPoolWaitingQueue.contains(task)){
			mPoolWaitingQueue.remove(task);
			XLog.i(LOG_TAG, "the task is not running, so just delete it from queue, and no need to do any thing else.");
			mWorkQueueLock.unlock();
			return;
		}
		FutureTask<?> futuretask = mTaskFutureMap.get(task);
		mWorkQueueLock.unlock();
		if (null == futuretask) return;
		IXFutureTask.Status status = task.getStatus();
		if (status == IXFutureTask.Status.FINISHED) {
			XLog.d(LOG_TAG, "the task  is finished.");
		} else if (status == IXFutureTask.Status.PENDING) {
			XLog.d(LOG_TAG, "the task  is not running.");
			futuretask.cancel(true);
		} else {
			XLog.d(LOG_TAG, "the task  is running, cancel it.");
			futuretask.cancel(true);
		}
	}
	
	public static void execute(){
		
	}
	
	
	public void clearAllTask(){
		
	}
	
	private void notifyExcption(IXFutureTask<?> task, XException info){
		task.onException(info);
	}
	
	public void notifyCancelComplete(IXFutureTask<?> task){
		task.onCancelComplete();
	}
	
}
