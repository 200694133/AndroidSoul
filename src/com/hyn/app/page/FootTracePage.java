package com.hyn.app.page;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyn.app.R;
import com.hyn.app.data.LocationDAO;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.data.LocationPixel;
import com.hyn.app.page.ui.PickAxisFragment;
import com.hyn.app.page.ui.PickAxisFragment.DescriptorAdapter;
import com.hyn.app.page.ui.PickAxisFragment.PickFragmentCallback;
import com.hyn.app.task.TaskManager;
import com.hyn.app.util.FunctionUtil.DateInfo;
import com.hyn.app.util.FunctionUtil;
import com.hyn.app.util.SLog;
import com.hyn.xtask.IXFutureTask;
import com.hyn.xtask.IXTask;
import com.hyn.xtask.XException;
import com.hyn.xtask.XTask;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class FootTracePage implements IPage , PickFragmentCallback{
	private static final String TAG = FootTracePage.class.getSimpleName();
	private static final long TIME_INTERVAL = 30L * 24 * 60 * 60 * 1000;
	AtomicBoolean isDisposed = new AtomicBoolean(false);
	private Activity mActivity = null;
	private Fragment mContentFragment = null;
	private Fragment mOptionPanelFragment = null;
	private MapFragment mMapFragment = null;
	private PickAxisFragment mIntervalPanelFragment = null;
	private FootTraceController mFootTraceController = null;
	DateInfo mStartDateInfo = null;
	DateInfo mEndDataInfo = null;
	
	@Override
	public void dispose() {
		SLog.d(TAG, "dispose");
	}

	@Override
	public boolean isDisposed() {
			return isDisposed.get();
	}

	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		mActivity = activity;
	}
	
	@Override
	public void setup(Fragment contentFragment, Fragment optionPanel) {
		mContentFragment = contentFragment;
		mOptionPanelFragment = optionPanel;
		mEndDataInfo = new DateInfo(System.currentTimeMillis());
		mStartDateInfo = new DateInfo(System.currentTimeMillis() - TIME_INTERVAL);
		if(null == mMapFragment) mMapFragment = new MapFragment();
		if(null == mFootTraceController) mFootTraceController = new FootTraceController(mMapFragment);
		if(null == mIntervalPanelFragment) mIntervalPanelFragment = new PickAxisFragment();
		mIntervalPanelFragment.disable();
		mIntervalPanelFragment.setPickedListener(this);
		mIntervalPanelFragment.setDescriptorAdapter(mDescriptorAdapter);
	}

	@Override
	public void agent() {
		Fragment contentFragment = mContentFragment;
		Fragment optionPanel = mOptionPanelFragment;
		if(null != contentFragment) {
			FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
	        //TODO transaction.setCustomAnimations(R.animator.from_right, R.animator.to_left, R.animator.pop_enter, R.animator.pop_exit);
			transaction.replace(R.id.map, mMapFragment);
	        transaction.commit();
		}
		
		if(null != optionPanel) {
			FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
	        //TODO transaction.setCustomAnimations(R.animator.from_right, R.animator.to_left, R.animator.pop_enter, R.animator.pop_exit);
			transaction.replace(R.id.info_fragment, mIntervalPanelFragment);
	        transaction.commit();
		}
		
		mActivity.findViewById(R.id.info_fragment).postDelayed(mAgentRunnable, 300);
	}

	private Runnable mAgentRunnable = new Runnable(){
		public void run(){
			mFootTraceController.init();
			mIntervalPanelFragment.enable();
		}
	};
	
	@Override
	public void revert() {
		
	}

	@Override
	public void handle(float left, float right) {
		long start = (long)((mEndDataInfo.mMillTimes - mStartDateInfo.mMillTimes) * left) + mStartDateInfo.mMillTimes;
		long end = (long)((mEndDataInfo.mMillTimes - mStartDateInfo.mMillTimes) * right) + mStartDateInfo.mMillTimes;
		
		mFootTraceController.post(start, end);
	}
	
	private DescriptorAdapter mDescriptorAdapter = new DescriptorAdapter(){
		@Override
		public Object description(float position) {
//			long mill = (long)((mEndDataInfo.mMillTimes - mStartDateInfo.mMillTimes) * position) + mStartDateInfo.mMillTimes;
//			DateInfo info = new DateInfo(mill);
//			return info;
			return ""+position;
		}
	};
};


class FootTraceController {
	private static final String LOG_TAG = FootTraceController.class.getSimpleName();

	private MapFragment mMapFragment;
	private volatile long mStart, mEnd;
	private IXFutureTask<?> mPrevTask = null;
	private static final int MAX_HUE = 360;
	private GoogleMap mMap = null;
	private final List<MapPoint> mPoints = new ArrayList<MapPoint>();
	private int mStandardColor = 0xFFFF0000;//red color

	public FootTraceController(MapFragment mapFragment) {
		mMapFragment = mapFragment;
		//0init();
	}

	public void init() {
		GoogleMap map = mMapFragment.getMap();
		map.setIndoorEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap = map;
	}

	public void post(long start, long end) {
		mStart = start;
		mEnd = end;
		RequestDataTask task = parseRequestTask();
		synchronized (FootTraceController.this) {
			mPrevTask = task;
			TaskManager.getsInstance().cancel(mPrevTask);
			TaskManager.getsInstance().postTask(task);
		}
	}
	
	
	private void draw() {
		List<MapPoint> pixelList = mPoints;
		if (null == pixelList || pixelList.size() <= 0) {
			onError("Cannot get location pixel.");
			return;
		}
		MapPoint p = pixelList.get(0);
		if (null != p) {
			mMapFragment.getMap().animateCamera(
					CameraUpdateFactory.newLatLng(p.getCenter()));
			Marker melbourne = mMapFragment.getMap().addMarker(
					new MarkerOptions()
							.position(p.getCenter())
							.title("Hello world")
							.draggable(true)
							.snippet("aaaaa")
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.ic_launcher)));
			melbourne.showInfoWindow();
		}
		drawCircle();
		drawLine(pixelList);
	}

	protected void drawLine(List<MapPoint> pixelList) {
		PolylineOptions rectOptions = new PolylineOptions();
		for (LocationPixel p : pixelList) {
			rectOptions.add(p.getCenter());
		}
		rectOptions.color(Color.HSVToColor(255, new float[] {mStandardColor, 1, 1}));
		mMapFragment.getMap().addPolyline(rectOptions);
	}
	
	
	protected void drawCircle() {
		for(MapPoint point : mPoints){
			point.onStyleChange();
		}
	}

	public void tryDrawTrace(final List<MapPoint> points){
		mStandardColor = (int) (System.currentTimeMillis() %  MAX_HUE);
		if(FunctionUtil.isRunOnUiThread()){
			for(MapPoint p : mPoints){
				p.clear();
			}
			mPoints.clear();
			mPoints.addAll(points);
			draw();
		}else{
			mMapFragment.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					for(MapPoint p : mPoints){
						p.clear();
					}
					mPoints.clear();
					mPoints.addAll(points);
					draw();
				}
			});
		}
	}
	
	protected void onError(String info){
		
	}
	
	private RequestDataTask parseRequestTask() {
		RequestDataTask task = new RequestDataTask(mMapFragment.getActivity()) {
			@Override
			public void onException(XException exception) {
				SLog.e(LOG_TAG, "Request Location Failed, " + exception.getMessage());
			}
			@Override
			public void onResult(List<LocationEntry> result) {
				SLog.d(LOG_TAG, "Get raw data size "+result.size());
				checkTimeIntervalIfNeed(result);
				double interval = mEnd - mStart;
				SLog.d(LOG_TAG, "start tag "+mStart+" End tag "+mEnd +" , Time interval "+interval);
				List<MapPoint> res = new ArrayList<MapPoint>();
				ListIterator<LocationEntry> iterator = result.listIterator();
				
				while (iterator.hasNext()) {
					LocationEntry data = iterator.next();
					MapPoint pixel = new MapPoint(data.getLat(), data.getLng(), 500);
					SLog.d(LOG_TAG, "start "+data.getStartMillTime() + " , end "+data.getEndMillTime()+", time interval "+(data.getEndMillTime()-data.getStartMillTime()));
					pixel.setRatio((data.getEndMillTime() - data.getStartMillTime())/ interval);
					res.add(pixel);
				}
				tryDrawTrace(res);
			}
		};
		return task;
	}
	
	private void checkTimeIntervalIfNeed(List<LocationEntry> result){
		if(null == result) throw new NullPointerException("reSetUpTimeIntervalIfNeed input is null.");
		if(result.size() > 0){
			int size = result.size();
			mStart = result.get(0).getStartMillTime();
			mEnd = result.get(size - 1).getEndMillTime();
			Log.d(LOG_TAG, "resetUpTimeIntervalIfNeed start tag "+mStart+" End tag "+mEnd);
		}
	}
	
	private class MapPoint extends LocationPixel {
		public MapPoint(LatLng center, LatLng radiusLatLng){
	    	super(center, radiusLatLng);
	    }
	    
	    public MapPoint(LatLng center, double radius){
	    	super(center, radius);
	    }
	    
	    public MapPoint(double lat, double lng, double radius){
	    	super(lat, lng, radius);
	    }
	    
	    public void onStyleChange(){
	    	SLog.d(LOG_TAG, "onStyleChange");
	    	setStandardColor(mStandardColor);
	    	draw();
	    }
	    
	    private void setStandardColor(int color){
	    	if(color < 0 || color > MAX_HUE) throw new IllegalArgumentException("standard color is illegal.");
	    	SLog.d(LOG_TAG, "mRatio "+mRatio);
	    	if(mRatio > 0.00){
	    		if(mRatio <0.1)  mRatio = 0.1;
	    		mFillColor = Color.HSVToColor((int)(mRatio*255), new float[] {color, 1, 1});
	    	}else{
	    		mFillColor = 0x00000000;
	    	}
	    }
	    
	    public void draw(){
	    	GoogleMap map = mMap;
	    	if(null == map) throw new NullPointerException("MapCircle draw google map is null.");
	    	if(null == mCircle) {
	    		mCircle = map.addCircle(new CircleOptions()
                    .center(mCenter)
                    .radius(500)
                    .strokeWidth(1));
	    	}
	    	mCircle.setFillColor(mFillColor);
	    }
	    
	    public void clear(){
	    	GoogleMap map = mMap;
	    	if(null == map) throw new NullPointerException("MapCircle clear google map is null.");
	    	mCircle.remove();
	    }
	}
	
	private abstract class RequestDataTask extends
			XTask<List<LocationEntry>> implements IXTask<List<LocationEntry>> {
		Activity mActivity;

		private RequestDataTask(Activity activity) {
			mActivity = activity;
		}

		@Override
		public List<LocationEntry> runInBackground()
				throws InterruptedException, CancellationException {
			Context context = mActivity;
			LocationDAO dao = null;
			try {
				dao = new LocationDAO(context);
				return dao.query(mStart, mEnd);
			} finally {
				if (null != dao)
					dao.dispose();
			}
		}

		@Override
		public abstract void onException(XException exception);

		@Override
		public abstract void onResult(List<LocationEntry> result);
	}
}

