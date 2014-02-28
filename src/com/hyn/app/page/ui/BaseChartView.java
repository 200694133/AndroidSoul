package com.hyn.app.page.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class BaseChartView extends View{
	private static final String TAG = BaseChartView.class.getSimpleName();
	
	public BaseChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public BaseChartView(Context context) {
		this(context, null, 0);
	}
	public BaseChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void onDraw(Canvas canvas){
		
	}
}
