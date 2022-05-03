package com.amanoteam.unalix.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.amanoteam.unalix.services.UnalixService;
import com.amanoteam.unalix.utilities.PackageUtils;

public class UnshortURLActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		final String uglyUrl = (action.equals(Intent.ACTION_SEND) ? intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getData().toString());

		if (!(uglyUrl.startsWith("http://") || uglyUrl.startsWith("https://"))) {
			PackageUtils.showToast(this, "Unsupported URL");
			
			finishAndRemoveTask();
			return;
		}

		final Intent service = new Intent(this, UnalixService.class);

		service.putExtra("originalAction", action);
		service.putExtra("uglyUrl", uglyUrl);
		service.putExtra("whatToDo", "unshortUrl");

		startService(service);

		finishAndRemoveTask();
	}

}