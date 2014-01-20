package com.hyn.xtask;

import java.util.concurrent.CancellationException;

public interface IXTask <Result>{
	public Result runInBackground() throws InterruptedException, CancellationException ;
}
