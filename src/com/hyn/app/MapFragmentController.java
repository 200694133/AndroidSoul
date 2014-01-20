package com.hyn.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyn.app.data.LocationDAO;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.data.LocationPixel;
import com.hyn.app.task.TaskManager;
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
public class MapFragmentController {
	private static final String LOG_TAG = MapFragmentController.class
			.getSimpleName();

	private MapFragment mMapFragment;
	private long mStart, mEnd;
	private IXFutureTask mPrevTask = null;

	public MapFragmentController(MapFragment mapFragment) {
		mMapFragment = mapFragment;
	}

	public void init() {
		GoogleMap map = mMapFragment.getMap();
		map.setIndoorEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		

		// map.get.mapType(GoogleMap.MAP_TYPE_SATELLITE)
		// .compassEnabled(false)
		// .rotateGesturesEnabled(false)
		// .tiltGesturesEnabled(false);
	}

	public void post(long start, long end) {
		mStart = start;
		mEnd = end;
		RequestDataTask task = parseRequestTask();
		synchronized (MapFragmentController.this) {
			mPrevTask = task;
			TaskManager.getsInstance().cancel(mPrevTask);
			TaskManager.getsInstance().postTask(task);
		}
	}

	private void updateTrace(final List<LocationPixel> pixelList) {
		Log.d(LOG_TAG, "get content size " + pixelList.size());
		mMapFragment.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				LocationPixel p = pixelList.get(0);
				if (null != p){
					Marker melbourne = mMapFragment.getMap().addMarker(new MarkerOptions()
	                .position(new LatLng(p.getLat(),
							p.getLng()))
	                .title("Hello world").draggable(true)
	                .snippet("aaaaa")
	                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
					melbourne.showInfoWindow();
				}
				drawLine(pixelList);
				drawArea(pixelList);
			}
		});
	}

	protected void drawLine(List<LocationPixel> pixelList) {
		PolylineOptions rectOptions = new PolylineOptions();
		for (LocationPixel p : pixelList) {
			rectOptions.add(new LatLng(p.getLat(), p.getLng()));
		}
		/* Polyline polyline = */mMapFragment.getMap().addPolyline(rectOptions);
	}

	protected void drawArea(List<LocationPixel> pixelList) {
		// TODO
	}

	private RequestDataTask parseRequestTask() {
		RequestDataTask task = new RequestDataTask(mMapFragment.getActivity(),
				mStart, mEnd) {
			@Override
			public void onException(XException exception) {
				Log.e(LOG_TAG,
						"Request Location Failed, " + exception.getMessage());
			}

			@Override
			public void onResult(List<LocationEntry> result) {
				AnalyzerDataTask analyzerDataTask = parseAnalyzerTask(result);
				synchronized (MapFragmentController.this) {
					mPrevTask = analyzerDataTask;
					TaskManager.getsInstance().postTask(analyzerDataTask);
				}
			}
		};
		return task;
	}

	private AnalyzerDataTask parseAnalyzerTask(List<LocationEntry> list) {
		AnalyzerDataTask task = new AnalyzerDataTask(
				mMapFragment.getActivity(), list, mStart, mEnd) {
			@Override
			public void onException(XException exception) {
				Log.e(LOG_TAG,
						"Analyze Location pixel Failed, "
								+ exception.getMessage());
			}

			@Override
			public void onResult(List<LocationPixel> result) {
				updateTrace(result);
			}
		};
		return task;
	}

	private abstract static class AnalyzerDataTask extends
			XTask<List<LocationPixel>> implements IXTask<List<LocationPixel>> {
		Activity mActivity;
		long mEnd, mStart;
		List<LocationEntry> mEntryList;

		private AnalyzerDataTask(Activity activity, List<LocationEntry> list,
				long start, long end) {
			mActivity = activity;
			mEntryList = list;
			mStart = start;
			mEnd = end;
		}

		@Override
		public List<LocationPixel> runInBackground()
				throws InterruptedException, CancellationException {
			final double interval = mEnd - mStart;
			List<LocationEntry> entryList = mEntryList;
			List<LocationPixel> locationPixelLis = new ArrayList<LocationPixel>();
			ListIterator<LocationEntry> iterator = entryList.listIterator();
			while (iterator.hasNext()) {
				LocationEntry data = iterator.next();
				LocationPixel pixel = new LocationPixel();
				pixel.setLat(data.getLat());
				pixel.setLng(data.getLng());
				pixel.setRatio((data.getEndMillTime() - data.getStartMillTime())
						/ interval);
				locationPixelLis.add(pixel);
			}
			mEntryList = null;
			return locationPixelLis;
		}

		@Override
		public abstract void onException(XException exception);

		@Override
		public abstract void onResult(List<LocationPixel> result);
	}

	private static abstract class RequestDataTask extends
			XTask<List<LocationEntry>> implements IXTask<List<LocationEntry>> {
		Activity mActivity;
		long mEnd, mStart;

		private RequestDataTask(Activity activity, long start, long end) {
			mActivity = activity;
			mStart = start;
			mEnd = end;
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
