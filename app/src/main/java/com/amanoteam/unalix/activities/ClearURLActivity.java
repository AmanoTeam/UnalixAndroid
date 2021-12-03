package com.amanoteam.unalix.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.amanoteam.unalix.services.UnalixService;

public class ClearURLActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		String uglyUrl = null;
		
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