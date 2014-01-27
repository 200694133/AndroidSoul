package com.hyn.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyn.app.data.LocationDAO;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.data.LocationPixel;
import com.hyn.app.task.TaskManager;
import com.hyn.app.util.FunctionUtil;
import com.hyn.xtask.IXFutureTask;
import com.hyn.xtask.IXTask;
import com.hyn.xtask.XException;
import com.hyn.xtask.XTask;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CancellationException;

/**
 * Created with IntelliJ IDEA. User: Administrator Date: 14-1-16 Time: 下午9:04
 */
public class MapController {
	private static final String LOG_TAG = MapController.class
			.getSimpleName();

	private MapFragment mMapFragment;
	private volatile long mStart, mEnd;
	private IXFutureTask<?> mPrevTask = null;
	private static final int MAX_HUE = 360;
	private GoogleMap mMap = null;
	private final List<MapPoint> mPoints = new ArrayList<MapPoint>();
	private int mStandardColor = 0xFFFF0000;//red color

	public MapController(MapFragment mapFragment) {
		mMapFragment = mapFragment;
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
		synchronized (MapController.this) {
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
		/* Polyline polyline = */mMapFragment.getMap().addPolyline(rectOptions);
	}
	
	
	
	protected void drawCircle() {
		for(MapPoint point : mPoints){
			point.onStyleChange();
		}
	}

	public void tryDrawTrace(final List<MapPoint> points){
		mStandardColor = (int) (System.currentTimeMillis() %  MAX_HUE);
		if(FunctionUtil.isRunOnUiThread()){
			mPoints.clear();
			mPoints.addAll(points);
			draw();
		}else{
			mMapFragment.getActivity().runOnUiThread(new Runnable() {
				public void run() {
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
				Log.e(LOG_TAG, "Request Location Failed, " + exception.getMessage());
			}

			@Override
			public void onResult(List<LocationEntry> result) {
				Log.d(LOG_TAG, "Get raw data size "+result.size());
				resetUpTimeIntervalIfNeed(result);
				final double interval = mEnd - mStart;
				Log.d(LOG_TAG, "start tag "+mStart+" End tag "+mEnd +" , Time interval "+interval);
				List<MapPoint> res = new ArrayList<MapPoint>();
				ListIterator<LocationEntry> iterator = result.listIterator();
				
				while (iterator.hasNext()) {
					LocationEntry data = iterator.next();
					MapPoint pixel = new MapPoint(data.getLat(), data.getLng(), 500);
					Log.d(LOG_TAG, "start "+data.getStartMillTime() + " , end "+data.getEndMillTime()+", time interval "+(data.getEndMillTime()-data.getStartMillTime()));
					pixel.setRatio((data.getEndMillTime() - data.getStartMillTime())/ interval);
					res.add(pixel);
				}
				tryDrawTrace(res);
			}
		};
		return task;
	}

	private void resetUpTimeIntervalIfNeed(List<LocationEntry> result){
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
	    	Log.d(LOG_TAG, "onStyleChange");
	    	setStandardColor(mStandardColor);
	    	draw();
	    }
	    
	    private void setStandardColor(int color){
	    	if(color < 0 || color > MAX_HUE) throw new IllegalArgumentException("standard color is illegal.");
	    	Log.d(LOG_TAG, "mRatio "+mRatio);
	    	if(mRatio > 0.00){
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
