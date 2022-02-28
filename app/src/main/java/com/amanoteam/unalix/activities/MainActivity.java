package com.amanoteam.unalix.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {

	private Unalix unalix;
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (settings, key) -> {
		// Update library preferences
		unalix.setFromPreferences(this);

		if (key.equals("appTheme")) {
			// Dark mode stuff
			final String appTheme = settings.getString("appTheme", "follow_system");

			PackageUtils.setAppTheme(appTheme);
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// Preferences stuff
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		// Dark mode stuff
		final String appTheme = settings.getString("appTheme", "follow_system");

		PackageUtils.setAppTheme(appTheme);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// ImageButton and EditText stuff
		final FloatingActionButton openUrlButton = findViewById(R.id.open_url_button);
		final FloatingActionButton cleanUrlButton = findViewById(R.id.clean_url_button);
		final FloatingActionButton shareUrlButton = findViewById(R.id.share_url_button);
		final FloatingActionButton clearInputButton = findViewById(R.id.clear_input_button);

		final AppCompatEditText urlInput = findViewById(R.id.url_input);

		// "Clean URL" button listener
		cleanUrlButton.setOnClickListener((final View view) -> {

			final String text = urlInput.getText().toString();

			if (TextUtils.isEmpty(text)) {
				PackageUtils.showSnackbar(view, "There is no URL to clean");
				return;
			}

			final String cleanedUrl = unalix.clearUrl(text);

			urlInput.setText(cleanedUrl);
		});

		// "Unshort URL" button listener
		cleanUrlButton.setOnLongClickListener((final View view) -> {

			final String text = urlInput.getText().toString();

			if (TextUtils.isEmpty(text)) {
				PackageUtils.showSnackbar(view, "There is no URL to unshort");
				return true;
			}

			PackageUtils.showSnackbar(view, "Resolving URL");

			new Thread(new Runnable() {
				@Override
				public void run() {
					final String cleanedUrl = unalix.unshortUrl(text);

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							urlInput.setText(cleanedUrl);
							PackageUtils.showSnackbar(view, "Done");
						}
					});
				}
			}).start();
			
			return true;
		});

		// "Open URL" button listener
		openUrlButton.setOnClickListener((final View view) -> {

			final String url = urlInput.getText().toString();

			if (TextUtils.isEmpty(url)) {
				PackageUtils.showSnackbar(view, "There is no URL to launch");
				return;
			}

			final Intent chooser = PackageUtils.createChooser(getApplicationContext(), url, Intent.ACTION_VIEW);
			startActivity(chooser);
		});

		// "Share URL" button listener
		shareUrlButton.setOnClickListener((final View view) -> {

			final String url = urlInput.getText().toString();

			if (TextUtils.isEmpty(url)) {
				PackageUtils.showSnackbar(view, "There is no URL to share");
				return;
			}

			final Intent chooser = PackageUtils.createChooser(getApplicationContext(), url, Intent.ACTION_SEND);
			startActivity(chooser);
		});

		// "Clean URL input" button listener
		clearInputButton.setOnClickListener((final View view) -> {
			final String url = urlInput.getText().toString();

			if (TextUtils.isEmpty(url)) {
				PackageUtils.showSnackbar(view, "URL input is already empty");
				return;
			}

			urlInput.getText().clear();
		});

		// Action bar stuff
		final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);

		// libunalix stuff
		unalix = new Unalix();
		unalix.setFromPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_quit:
				System.exit(0);
			case R.id.settings_activity:
				final Intent activity = new Intent(this, SettingsActivity.class);
				activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(activity);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	protected void onDestroy() {
		// Unregister preferences callback
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		super.onDestroy();
	}
}