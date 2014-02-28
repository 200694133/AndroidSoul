package com.hyn.app.page.ui;

import com.hyn.app.util.SLog;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class TitleBarItem extends TextView implements ITitleBarItem {
	private static final String TAG = TitleBarItem.class.getSimpleName();
	public TitleBarItem(Context context) {
		this(context, null, 0);
	}
	
	public TitleBarItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TitleBarItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onSelected() {
		setTextColor(Color.RED);
		SLog.d(TAG, "onSelected");
	}

	@Override
	public void onReset() {
		setTextColor(Color.WHITE);
		SLog.d(TAG, "onReset");
	}

	@Override
	public View getView() {
		return this;
	}

}
