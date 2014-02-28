package com.hyn.app.page;

import com.hyn.app.util.Disposable;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public interface IPage extends Disposable{
	public void onCreate(Activity activity, Bundle savedInstanceState);
	
	public void setup(Fragment contentFragment, Fragment optionPanel);
	
	public void agent();
	
	public void revert();
}
