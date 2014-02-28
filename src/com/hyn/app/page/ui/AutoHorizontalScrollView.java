package com.hyn.app.page.ui;

import java.util.ArrayList;
import java.util.List;

import com.hyn.app.util.SLog;
import com.hyn.app.util.SoulException;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class AutoHorizontalScrollView extends HorizontalScrollView{
	private static final String TAG = AutoHorizontalScrollView.class.getSimpleName();
	
	private static final int ANIMATOR_DURATION = 300;
	/**  all view displayed in title bar. */
	private List<ITitleBarItem> mChildrenList = new  ArrayList<ITitleBarItem>();
	/**  current selected view. */
	private View mCurrSelectedView = null;
	/** layout container. */
	private LinearLayout mContainer = null;
	/** Click listener for caller. */
	private OnClickListener mOuterClickListener = null;
	
	ObjectAnimator mPrevObjectAnimator = null;
	/** Highlight color for selected item.  */
	private int mHighlightColor = Color.RED;
	
	/** the duration of animation. */
	private int mDuration = ANIMATOR_DURATION;
	
	
	public AutoHorizontalScrollView(Context context) {
		this(context, null, 0);
	}
	public AutoHorizontalScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public AutoHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup();
	}
	
	public void setup(){
		if(null == mContainer){
			mContainer = new LightLinearLayout(getContext());
			LinearLayout.LayoutParams fl = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, 
					LinearLayout.LayoutParams.MATCH_PARENT);
			addView(mContainer, fl);
		}
	}

	public void reset(){
		
	}
	
	public void setOnClickListener(OnClickListener l){
		mOuterClickListener = l;
	}
	
	public void setMoveDuration(int duration){
		mDuration = duration;
	}
	
	@Override
	public void addView(View view){
		throw new UnsupportedOperationException(TAG + " not support add view directly, please add(ITitleBarItem).");
	}
	
	
	public void addView(ITitleBarItem view){
		if(null == view || view.getView() == null) throw new NullPointerException("add empty null!");
		view.getView().setOnClickListener(mChildClickListener);
		if(null != mContainer){
			mContainer.addView(view.getView(), generateItemLayoutParam());
			mChildrenList.add(view);
		}
	}
	
	public void remove(View view){
		throw new UnsupportedOperationException(TAG + " not support remove view directly, please remove(ITitleBarItem).");
	}
	
	public void remove(ITitleBarItem view){
		if(null == view || view.getView() == null)  throw new NullPointerException("add empty null!");
		if(null != mContainer){
			mContainer.removeView(view.getView());
			mChildrenList.remove(view);
		}
	}
	
	public void select(ITitleBarItem view){
		if(null != view && null !=  view.getView()){
			mCurrSelectedView = view.getView();
		}
		sync(false);
		mContainer.invalidate();
	}
	
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		SLog.d(TAG, "onConfigurationChanged");
	}

	private OnClickListener mChildClickListener = new OnClickListener() {
		public void onClick(View v) {
			onSelectedChanged(v);
		}
	};
	
	
	private void onSelectedChanged(View newView){
		if(!(newView instanceof ITitleBarItem)){
			SLog.e(TAG, "select a unsupport view, ignore this event.");
			return ;
		}
		if(null != mCurrSelectedView){
			ITitleBarItem oldView = (ITitleBarItem)mCurrSelectedView;
			oldView.onReset();
		}
		mCurrSelectedView = newView;
		ITitleBarItem nextView = (ITitleBarItem)mCurrSelectedView;
		nextView.onSelected();
		sync(true);
		if(null != mOuterClickListener) mOuterClickListener.onClick(newView);
		mContainer.invalidate();
	}
	
	/**
	 * This function is the core of this class. It change the position of the 
	 * @param smooth
	 */
	public void sync(boolean smooth){
		if(null == mCurrSelectedView) {
			SLog.i(TAG, "sync scroll view failed, no selected view.");
		}
		if(null != mPrevObjectAnimator) mPrevObjectAnimator.cancel();
		mPrevObjectAnimator = null;
		int currScrollX = getScrollX();
		int nextScrollX = calculatNewScrollX(mCurrSelectedView);
		if(smooth){
			ObjectAnimator oa = ObjectAnimator.ofInt(this, "scrollX", currScrollX, nextScrollX);
			oa.setDuration(mDuration);
			oa.start();
			mPrevObjectAnimator = oa;
		}else{
			setScrollX(nextScrollX);
		}
	}
	
	/**
	 * Calculate the x coordination for new selected view.
	 * @param newSelectedView
	 * @return
	 */
	private int calculatNewScrollX(View newSelectedView){
		View newView = newSelectedView;
		int windowWidth = getWidth();
		int containerWidth = mContainer.getWidth();
		int maxXScrollable = containerWidth - windowWidth;
		if(maxXScrollable<=0) return 0;//cannot scrollable, so keep current state, do nothing.
		List<View> allView = new ArrayList<View>();
		for(ITitleBarItem v : mChildrenList){
			allView.add(v.getView());
		}
		int [] res = new int[allView.size()];
		int index = 0 ;
		for(View pv : allView){
			int left = pv.getLeft();
			if(left > maxXScrollable){
				left = maxXScrollable;
			}
			res[index] = Math.abs(newView.getLeft() + newView.getWidth()/2 - left - windowWidth/2);

			++index;
		}
		
		//find best value
		int best = Integer.MAX_VALUE;
		int bestIndex = -1;
		for(int i=0;i<res.length;++i){
			if(best >  res[i]){
				best = res[i];
				bestIndex = i;
			}
		}
		
		if(bestIndex < 0){
			throw new SoulException("");
		}
		SLog.d(TAG, "Best index "+bestIndex);
		SLog.d(TAG, "new scroll value "+allView.get(bestIndex).getLeft());
		int v = allView.get(bestIndex).getLeft();
		if(v > maxXScrollable) v = maxXScrollable;
		return v;
	}
	
	private class LightLinearLayout extends LinearLayout{
		public LightLinearLayout(Context context) {
			this(context, null, 0);
		}
		public LightLinearLayout(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		Paint mPaint = new Paint();
		public void dispatchDraw(Canvas canvas){
			super.dispatchDraw(canvas);
			View view = mCurrSelectedView;
			if(null != view){
				mPaint.setColor(mHighlightColor);
				int left = view.getLeft();
				int height = this.getHeight();
				canvas.drawRect(new Rect(left,height-1-5,left+view.getWidth(), height-1), mPaint);
			}
		}
	}
	
	
	private FrameLayout.LayoutParams generateItemLayoutParam(){
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		return fl;
	}
}
