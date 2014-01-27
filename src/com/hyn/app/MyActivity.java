package com.hyn.app;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.service.SensorService;
import com.hyn.app.widget.PickAxisFragment;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MyActivity extends Activity {
    //private static final String TAG = MyActivity.class.getSimpleName();
    private static final String TAG = MyActivity.class.getSimpleName();
    
    final Handler mHandler = new Handler(Looper.getMainLooper()){
    	 public void handleMessage(Message msg) {
    		 displayLocationInfo(msg);
    	 }
    };

    Messenger mClientMessenger;
    Messenger mSensorMessenger;
    TextView mDebugView = null;
    private MapController mMapFragmentController = null;
    MapFragment mMapFragment;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDebugView = (TextView)findViewById(R.id.debug_info);

        mClientMessenger = new Messenger(mHandler);
        bindService(new Intent(this, SensorService.class), mConnection, Context.BIND_AUTO_CREATE);

        PickAxisFragment fragment = (PickAxisFragment)getFragmentManager().findFragmentById(R.id.pick_fragment_id);
        fragment.setDescriptorAdapter(new PickAxisFragment.DescriptorAdapter() {
            public Object description(float position) {
                return String.format("%-10.3f", position);
            }
        });
        fragment.setPickedListener(new PickAxisFragment.PickFragmentCallback(){
            @Override
            public void handle(float left, float right) {

            }
        });

        mMapFragment = (MapFragment)this.getFragmentManager().findFragmentById(R.id.map);
        mMapFragmentController = new MapController(mMapFragment);
    }


    public void onResume(){
        super.onResume();

        mDebugView.postDelayed(new Runnable() {
            @Override
            public void run() {
            	mMapFragmentController.init();
                mMapFragmentController.post(0, 5555456456456L);
            }
        }, 5000);
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
