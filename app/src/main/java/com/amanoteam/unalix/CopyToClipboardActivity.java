package com.amanoteam.unalix;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;

public class CopyToClipboardActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		String urlToCopy = null;
		
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