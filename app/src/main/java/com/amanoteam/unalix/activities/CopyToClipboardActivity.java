package com.amanoteam.unalix.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CopyToClipboardActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		final String urlToCopy = (intent.getAction().equals(Intent.ACTION_SEND) ? intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getData().toString());

		final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText("Clean URL", urlToCopy));

		Toast.makeText(CopyToClipboardActivity.this, String.format("Copied %s to clipboard", urlToCopy), Toast.LENGTH_SHORT).show();

		finish();
	}

}