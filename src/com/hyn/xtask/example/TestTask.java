package com.hyn.xtask.example;

import java.util.concurrent.CancellationException;

import com.hyn.xtask.XLog;
import com.hyn.xtask.XTask;

public class TestTask extends XTask<String>{
	private static final String LOG_TAG = TestTask.class.getSimpleName();
	private static int sIndex = 0;
	int p;
	public TestTask(int p){
		this.p = p;
	}
	
	@Override
	public String runInBackground() throws InterruptedException,
			CancellationException {
		XLog.d("Test", ""+p+" Start");
		Thread.sleep(100);
		checkIfCanceled();
		Thread.sleep(100);
		checkIfCanceled();
		Thread.sleep(100);
		checkIfCanceled();
		Thread.sleep(100);
		checkIfCanceled();
		Thread.sleep(100);
		XLog.d("Test", ""+p+" End");
		return ""+p;
	}
	
	@Override
	public void onCancelComplete() {
		XLog.d(LOG_TAG, p+" onCancelComplete");
	}

	@Override
	public void onResult(String result) {
		XLog.d(LOG_TAG, p+" onResult"+result);
	}
}
