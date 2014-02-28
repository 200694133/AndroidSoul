package com.hyn.app.page;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.gms.internal.ao;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class ContactsPage implements IPage{
	private static final String TAG = ContactsPage.class.getSimpleName();
	private AtomicBoolean isDisposed = new AtomicBoolean(false);


	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setup(Fragment contentFragment, Fragment optionPanel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void agent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revert() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		isDisposed.set(true);
	}
	
	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}
}
