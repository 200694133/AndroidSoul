package com.hyn.app.service;


import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import com.hyn.app.data.LocationDAO;
import com.hyn.app.data.LocationEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-13
 * Time: 下午1:50
 * This class provide a service to get the sensor information. This Service is run on
 * other process.
 */
public class SensorService extends Service implements Handler.Callback, LocationHelper.LocationCallback {
    //private static final String TAG = SensorService.class.getSimpleName();
    private static final String TAG = Constants.TAG;

    /** Message id: register a listener to listen the location state. */
    public static final int MSG_REGISTER_LOCATION = 0x01;
    /** Message id: un-register a listener to remove listen the location state. */
    public static final int MSG_UNREGISTER_LOCATION = 0x02;

    /**
     * Handler of incoming messages from clients.
     */
    final Handler mInternalHandler = new Handler(Looper.myLooper(), this);

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(mInternalHandler);

    /**
     * location manager helper.
     */
    private LocationHelper mLocationHelper = null;

    /** The times of running of get the message from client. */
    private int mCount = 0;

    /** Messenger callback list. When location has changed, notify all callbacks to update new location. */
    final List<Messenger> mLocationCallbacks = new LinkedList<Messenger>();

    /** LocationDAO */
    private LocationDAO mLocationDAO;
    public SensorService(){
        super();
    }

    /** Init some vars when create. */
    @Override
    public void onCreate(){
        mLocationHelper = new LocationHelper(this, getApplication());
        mLocationDAO = new LocationDAO(this);
    }

    /**
     * Clean the service, recycle all allocated memory.
     */
    @Override
    public void onDestroy(){
        Log.i(TAG, "BackService onDestroy");
        mLocationHelper.stop();
        mLocationHelper.dispose();
        mLocationDAO.dispose();
        mLocationDAO = null;
        synchronized (mLocationCallbacks){
            mLocationCallbacks.clear();
        }

        mLocationHelper = null;
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * It's the main function to process the message which come from clients.
     * @param msg the request message come from client. It contain the command to
     *            indicate that if it's register callback or un-register callback.
     * @return true to means that handler no need to process the message again.
     */
    public boolean handleMessage(Message msg) {
        switch(msg.what){
            case MSG_REGISTER_LOCATION:
                internalRegisterLocationCallback(msg.replyTo);
                break;
            case MSG_UNREGISTER_LOCATION:
                internalUnregisterLocationCallback(msg.replyTo);
                break;
            default:
                throw new IllegalArgumentException("Illegal command from client!");
        }

        return true;
    }

    /**
     * register a callback for listening location state.
     * @param messenger client messenger which listen location state
     */
    private void internalRegisterLocationCallback(Messenger messenger){
        synchronized (mLocationCallbacks){
            mLocationCallbacks.add(messenger);
            if(mLocationCallbacks.size() > 0){
                mLocationHelper.start();
            }
        }
    }

    /**
     * un-register a callback for cancelling listen location state.
     * @param messenger  client messenger which remove listen location state
     */
    private void internalUnregisterLocationCallback(Messenger messenger){
        synchronized (mLocationCallbacks){
            mLocationCallbacks.remove(messenger);
            if(mLocationCallbacks.size() < 0){
                mLocationHelper.start();
            }
        }
    }

    /**
     * send all message to client.
     */
    @Override
    public void notifyLocation(long startTime, long endTime, double lat, double lng) {
        List<Messenger> callbacks = new ArrayList<Messenger>();
        synchronized (mLocationCallbacks){
            callbacks.addAll(mLocationCallbacks);
        }
        Log.e(TAG, "notifyUpdateLocation callbacks size "+callbacks.size());

        LocationEntry entry = new LocationEntry(startTime,endTime,lat ,lng);
        sendData(callbacks, entry);

        //insert to database
        mLocationDAO.insert(entry);
    }

    @Override
    public void onError(Object obj) {
        List<Messenger> callbacks = new ArrayList<Messenger>();
        synchronized (mLocationCallbacks){
            callbacks.addAll(mLocationCallbacks);
        }
        sendData(callbacks, (Serializable)obj);
    }

    /**
     * Send data to messengers.Notify all listeners which in a separate processor that location
     * has changed!<p>
     * because this service is running on a separate processor, you cannot use
     * the obj data member of Message. <b>Instead, you will need to package your data in a
     * Bundle and attach it to the Message via setData()</b>.
     * @param callbacks all listeners
     * @param data that send to listeners which in a separate processor.
     */
    final private void sendData(List<Messenger> callbacks, Serializable data){
        if(null == callbacks) throw new NullPointerException("send data to messenger is null pointer. ");
        for(Messenger messenger : callbacks){
            Message responseMsg = Message.obtain(null, mCount++);

            Bundle sendData = new Bundle();
            sendData.putSerializable("data", data);
            responseMsg.setData(sendData);
            responseMsg.replyTo = mMessenger;
            try {
                messenger.send(responseMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
