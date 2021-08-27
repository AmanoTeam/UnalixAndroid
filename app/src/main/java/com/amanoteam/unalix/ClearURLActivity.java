package com.amanoteam.unalix;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import unalix.Unalix;

public class ClearURLActivity extends Activity {
	
	private static final Unalix unalix = new Unalix();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		String uglyUrl = "";
		
		if (action.equals(Intent.ACTION_SEND)) {
			uglyUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
		} else if (action.equals(Intent.ACTION_VIEW)) {
			uglyUrl = intent.getData().toString();
		}
		
		final Intent serviceIntent = new Intent(this, UnalixService.class);
		serviceIntent.putExtra("originalAction", action);
		serviceIntent.putExtra("uglyUrl", uglyUrl);
		serviceIntent.putExtra("whatToDo", "clearUrl");
		
		startService(serviceIntent);
		
		finish();
	}

}