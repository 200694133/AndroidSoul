package com.hyn.app.page.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class BasePieChartView extends View{
	private static final String TAG = BasePieChartView.class.getSimpleName();
	private static int[]sColorPools = new int[]{
		Color.RED, Color.BLUE, Color.GRAY, Color.GREEN
	};
	/** The  degrees of first sector. */
	private float mOrignalDegrees = 0;
	/** Pixel radius of the circle */
	private int mRadius = 0;
	private int mAnchorX = 0;
	private int mAnchorY = 0;
	public BasePieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public BasePieChartView(Context context) {
		this(context, null, 0);
	}
	public BasePieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	
	/** This is called when the view is attached to a window.  */
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
	}
	
	/** This is called when the view is detached to a window.  release resource in this function. */
	public void onDetachedFromWindow(){
		super.onDetachedFromWindow();
	}
	/**
	 * The radius of the circle. If the value is bigger than the width of height of this view, 
	 * then some part will out of  screen and can not drawer. 
	 * @param radius The pixel radius of the circle.
	 */
	public void setRadius(int radius){
		mRadius = radius;
	}
	
	/**
	 * set the center coordinate of the circle to original point which coordinate is (0,0) locate in top left corner.
	 * @param offsetX x coordinate to original point(top left)
	 * @param offsetY y coordinate to original point(top left)
	 */
	public void setAnchor(int offsetX, int offsetY){
		mAnchorX = offsetX;
		mAnchorY = offsetY;
	}
	
	public void setOrignalDegrees(float orignalDegrees){
		mOrignalDegrees = orignalDegrees;
		invalidate();
	}
	
	public void onDraw(Canvas canvas){
		super.draw(canvas);
		
		canvas.translate(mAnchorX, mAnchorY);
		
		canvas.translate(-mAnchorX, -mAnchorY);
	}
	
	
	public static interface IFragment{
		/** Get the ratio of the current fragment, the value must be  */
		public double getRatio();
		public String getDesc();
	}
}
