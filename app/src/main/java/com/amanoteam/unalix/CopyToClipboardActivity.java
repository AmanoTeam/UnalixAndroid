package com.amanoteam.unalix;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import android.content.ClipData;
import android.content.ClipboardManager;

public class CopyToClipboardActivity extends Activity {
	
	private String urlToCopy = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if (action.equals(Intent.ACTION_SEND)) {
			urlToCopy = intent.getStringExtra(Intent.EXTRA_TEXT);
		} else if (action.equals(Intent.ACTION_VIEW)) {
			urlToCopy = intent.getData().toString();
		}
		
		final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Clean URL", urlToCopy));
		
		finish();
	}

}