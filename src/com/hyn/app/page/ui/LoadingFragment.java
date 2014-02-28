package com.hyn.app.page.ui;

import com.hyn.app.R;
import com.hyn.app.util.SLog;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoadingFragment extends Fragment{
	private static final String TAG = LoadingFragment.class.getSimpleName();
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SLog.d(TAG, "onCreate");
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.loading_layout, container);
	}
	
	public void onStop(){
		super.onStop();
		SLog.d(TAG, "onStop");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		SLog.d(TAG, "onDestroy");
	}
}
