package com.hyn.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.view.Gravity;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.page.PageFlow;
import com.hyn.app.page.PageFlow.Page;
import com.hyn.app.page.ui.AutoHorizontalScrollView;
import com.hyn.app.page.ui.ITitleBarItem;
import com.hyn.app.page.ui.TitleBarItem;
import com.hyn.app.service.SensorService;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MyActivity extends Activity {
    @SuppressWarnings("unused")
	private static final String TAG = MyActivity.class.getSimpleName();
    
    final Handler mHandler = new Handler(Looper.getMainLooper()){
    	 public void handleMessage(Message msg) {
    		 displayLocationInfo(msg);
    	 }
    };

    Messenger mClientMessenger;
    Messenger mSensorMessenger;
    TextView mDebugView = null;
    MapFragment mMapFragment;
    Fragment mContentFragment = null;
    Fragment mInfoFragment = null;
    PageFlow mPageFlow = null;
    AutoHorizontalScrollView mAutoHorizontalScrollView;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDebugView = (TextView)findViewById(R.id.debug_info);
        mAutoHorizontalScrollView = (AutoHorizontalScrollView) findViewById(R.id.titlebar);

        mClientMessenger = new Messenger(mHandler);
        bindService(new Intent(this, SensorService.class), mConnection, Context.BIND_AUTO_CREATE);
         
        mContentFragment = getFragmentManager().findFragmentById(R.id.map);
        mInfoFragment = getFragmentManager().findFragmentById(R.id.info_fragment);
        
//        mMapFragment = new MapFragment();
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.replace(R.id.map, mMapFragment);
//        transaction.commit();
//        mMapFragmentController = new FootTraceController(mMapFragment);
        mPageFlow = PageFlow.getInstance(this);
        mPageFlow.setup(mContentFragment, mInfoFragment);
        setupTitleBar();
    }


    public void onResume(){
        super.onResume();

        mDebugView.postDelayed(new Runnable() {
            @Override
            public void run() {
//            	mMapFragmentController.init();
//                mMapFragmentController.post(0, 5555456456456L);
            	
            	
            	
            	
            	//mPageFlow.displayPage();
       
            	
            	
            	
            	
            }
        }, 5000);
    }
    
    
    private void setupTitleBar(){
    	Page pages[] = PageFlow.getInstance(this).listPage();
    	for(Page page : pages){
    		String info = ""+page;
    		TitleBarItem tv = new TitleBarItem(this);
    		tv.setText(info);
    		tv.setMinWidth(200);
    		tv.setGravity(Gravity.CENTER);
    		mAutoHorizontalScrollView.addView(((ITitleBarItem)tv));
    	}        
    }
    
    public void onDestroy(){
        super.onDestroy();

        this.unbindService(mConnection);
    }

    
    void displayLocationInfo(Message msg){
        Object data = msg.getData().get("data");
        if(null != mDebugView){
            Object res = data;
            if(null == res) {
                mDebugView.setText("Index " + msg.what + "\tResult is empty");
                return;
            }

            if(res instanceof RuntimeException){
                RuntimeException e = (RuntimeException)res;
                mDebugView.setText("Index "+msg.what+"\t Exception"+e.toString());
                return;
            }

            if(res instanceof LocationEntry){
                LocationEntry location = (LocationEntry)data;

                double lat=location.getLat();
                double lng=location.getLng();
                Date date1=new Date(location.getStartMillTime());
                Date date2=new Date(location.getEndMillTime());
                SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String s = df.format(date1);
                mDebugView.setText("Index "+msg.what+"\t"+lat+", "+lng+"\n"+df.format(date1)+" - "+df.format(date2));
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSensorMessenger = new Messenger(service);
            runnable.run();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSensorMessenger = null;
        }
    };


    public Runnable runnable = new Runnable() {
        Random random = new Random();
        @Override
        public void run() {
            if(null == mSensorMessenger) return ;
            int what = SensorService.MSG_REGISTER_LOCATION;
            int a1 = random.nextInt();
            int a2 = random.nextInt();
            Message msg = Message.obtain(null, what, a1, a2);
            msg.replyTo = mClientMessenger;
            try {
                mSensorMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
}
