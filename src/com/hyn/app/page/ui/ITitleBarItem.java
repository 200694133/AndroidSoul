package com.hyn.app.page.ui;

import android.view.View;

public interface ITitleBarItem {
	public void onSelected();
	
	public void onReset();
	
	public View getView();
}
