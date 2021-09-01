package com.amanoteam.unalix;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;

import com.amanoteam.unalix.UnalixService;

public class UnshortURLActivity extends Activity {
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String uglyUrl = null;
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if (action.equals(Intent.ACTION_SEND)) {
			uglyUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
		} else if (action.equals(Intent.ACTION_VIEW)) {
			uglyUrl = intent.getData().toString();
		}
		
		final Intent serviceIntent = new Intent(this, UnalixService.class);
		
		serviceIntent.putExtra("originalAction", action);
		serviceIntent.putExtra("uglyUrl", uglyUrl);
		serviceIntent.putExtra("whatToDo", "unshortUrl");
		
		startService(serviceIntent);
		
		finish();
	}

}