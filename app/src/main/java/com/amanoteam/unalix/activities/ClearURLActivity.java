package com.amanoteam.unalix.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.amanoteam.unalix.services.UnalixService;

public class ClearURLActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		final Intent serviceIntent = new Intent(this, UnalixService.class);
		
		final String uglyUrl = (action.equals(Intent.ACTION_SEND) ? intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getData().toString());
		
		serviceIntent.putExtra("originalAction", action);
		serviceIntent.putExtra("uglyUrl", uglyUrl);
		serviceIntent.putExtra("whatToDo", "clearUrl");
		
		startService(serviceIntent);
		
		finish();
	}

}