package com.hyn.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import com.hyn.app.data.LocationEntry;
import com.hyn.app.service.SensorService;
import com.hyn.app.widget.PickAxisFragment;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MyActivity extends Activity {
    //private static final String TAG = MyActivity.class.getSimpleName();
    private static final String TAG = MyActivity.class.getSimpleName();
    final private Handler.Callback mLocationCallback = new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            displayLocationInfo(msg);
            return true;
        }
    };
    final Handler mHandler = new Handler(Looper.getMainLooper(), mLocationCallback);

    Messenger mClientMessenger;
    Messenger mSensorMessenger;
    TextView mDebugView = null;
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


        MapFragment mapFragment = (MapFragment)this.getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();
        map.setIndoorEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Marker melbourne = map.addMarker(new MarkerOptions()
                .position(new LatLng(-37.813, 144.962))
                .title("Hello world").draggable(true)
                .snippet("aaaaa")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
        melbourne.showInfoWindow();
        //Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(-37.813, 144.962))
                .add(new LatLng(-37.913, 144.962))  // North of the previous point, but at the same longitude
                .add(new LatLng(-37.913, 144.762))  // Same latitude, and 30km to the west
                .add(new LatLng(-37.813, 144.762))  // Same longitude, and 16km to the south
                .add(new LatLng(-37.813, 144.962)); // Closes the polyline.

        // Get back the mutable Polyline
        //Polyline polyline = map.addPolyline(rectOptions);

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(new LatLng(-37.813, 144.962),
                        new LatLng(-37.913, 144.962),
                        new LatLng(-37.913, 144.762),
                        new LatLng(-37.813, 144.762),
                        new LatLng(-37.813, 144.962));
        polygonOptions.fillColor(Color.GREEN).strokeColor(Color.BLUE);
        //Polygon polygon = map.addPolygon(polygonOptions);

        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(-37.813, 144.962))
                .radius(1000); // In meters
        circleOptions.fillColor(Color.BLUE&0x50FFFFFF);
        circleOptions.strokeColor(Color.GREEN);
        circleOptions.zIndex(1);
        //Circle circle = map.addCircle(circleOptions);

        LatLng NEWARK = new LatLng(-37.813, 144.962);
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
                .position(NEWARK, 8600f, 6500f);
        map.addGroundOverlay(newarkMap);






        //map.setInfoWindowAdapter();
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
                mDebugView.setText("Index "+msg.what+"\tException"+e.toString());
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


    private GoogleMap.InfoWindowAdapter mInfoWindowAdapter = new GoogleMap.InfoWindowAdapter(){
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    private void setupGoogleMap(GoogleMap map){
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);
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

    public void onDestroy(){
        super.onDestroy();

        this.unbindService(mConnection);
    }


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
