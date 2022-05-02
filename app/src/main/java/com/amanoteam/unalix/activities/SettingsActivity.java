package com.amanoteam.unalix.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.EditTextPreference;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.fragments.SettingsFragment;
import com.amanoteam.unalix.utilities.PackageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

	private SettingsFragment settingsFragment;
	private PreferenceScreen preferenceScreen;

	// "Export" preferences listener
	private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(new StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {

					final Context context = getApplicationContext();
					final View view = findViewById(android.R.id.content);

					final Intent intent = result.getData();
					final Uri fileUri = intent.getData();

					final ContentResolver contentResolver = getContentResolver();

					try {
						final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
						final JSONObject obj = new JSONObject();

						obj.put("ignoreReferralMarketing", preferences.getBoolean("ignoreReferralMarketing", false));
						obj.put("ignoreRules", preferences.getBoolean("ignoreRules", false));
						obj.put("ignoreExceptions", preferences.getBoolean("ignoreExceptions", false));
						obj.put("ignoreRawRules", preferences.getBoolean("ignoreRawRules", false));
						obj.put("ignoreRedirections", preferences.getBoolean("ignoreRedirections", false));
						obj.put("skipBlocked", preferences.getBoolean("skipBlocked", false));
						
						obj.put("timeout", Integer.valueOf(preferences.getString("timeout", "")));
						obj.put("maxRedirects", Integer.valueOf(preferences.getString("maxRedirects", "")));
						obj.put("userAgent", preferences.getString("userAgent", ""));
						obj.put("customUserAgent", preferences.getString("customUserAgent", ""));
						
						obj.put("dns", preferences.getString("dns", ""));
						obj.put("customDns", preferences.getString("customDns", ""));

						obj.put("socks5Proxy", preferences.getBoolean("socks5Proxy", false));
						obj.put("proxyAddress", preferences.getString("proxyAddress", ""));
						obj.put("proxyPort", preferences.getString("proxyPort", ""));
						obj.put("proxyAuthentication", preferences.getBoolean("proxyAuthentication", false));
						obj.put("proxyUsername", preferences.getString("proxyUsername", ""));
						obj.put("proxyPassword", preferences.getString("proxyPassword", ""));

						obj.put("appTheme", preferences.getString("appTheme", "follow_system"));
						obj.put("disableClearURLActivity", preferences.getBoolean("disableClearURLActivity", false));
						obj.put("disableUnshortURLActivity", preferences.getBoolean("disableUnshortURLActivity", false));

						final OutputStream outputStream = contentResolver.openOutputStream(fileUri);

						outputStream.write(obj.toString().getBytes());
						outputStream.close();
					} catch (final IOException | JSONException e) {
						PackageUtils.showSnackbar(view, "Error exporting preferences file");
						return;
					}

					PackageUtils.showSnackbar(view, "Export successful");
				}
			});

	// "Import" preferences listener
	private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(new StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {

					final Context context = getApplicationContext();
					final View view = findViewById(android.R.id.content);

					final Intent intent = result.getData();
					final Uri fileUri = intent.getData();

					final ContentResolver contentResolver = getContentResolver();

					try {
						final InputStream inputStream = contentResolver.openInputStream(fileUri);
						final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()));
						final StringBuilder stringBuilder = new StringBuilder();

						String inputLine;

						while ((inputLine = bufferedReader.readLine()) != null) {
							stringBuilder.append(inputLine);
						}

						inputStream.close();

						final JSONObject obj = new JSONObject(stringBuilder.toString());

						final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
						final Editor editor = preferences.edit();

						editor.putBoolean("ignoreReferralMarketing", obj.getBoolean("ignoreReferralMarketing"));
						editor.putBoolean("ignoreRules", obj.getBoolean("ignoreRules"));
						editor.putBoolean("ignoreExceptions", obj.getBoolean("ignoreExceptions"));
						editor.putBoolean("ignoreRawRules", obj.getBoolean("ignoreRawRules"));
						editor.putBoolean("ignoreRedirections", obj.getBoolean("ignoreRedirections"));
						editor.putBoolean("skipBlocked", obj.getBoolean("skipBlocked"));
						
						editor.putString("timeout", String.valueOf(obj.getInt("timeout")));
						editor.putString("maxRedirects", String.valueOf(obj.getInt("maxRedirects")));
						editor.putString("userAgent", obj.getString("userAgent"));
						editor.putString("customUserAgent", obj.getString("customUserAgent"));
						
						editor.putString("dns", obj.getString("dns"));
						editor.putString("customDns", obj.getString("customDns"));

						editor.putBoolean("socks5Proxy", obj.getBoolean("socks5Proxy"));
						editor.putString("proxyAddress", obj.getString("proxyAddress"));
						editor.putString("proxyPort", String.valueOf(obj.getInt("proxyPort")));
						editor.putBoolean("proxyAuthentication", obj.getBoolean("proxyAuthentication"));
						editor.putString("proxyUsername", obj.getString("proxyUsername"));
						editor.putString("proxyPassword", obj.getString("proxyPassword"));

						editor.putString("appTheme", obj.getString("appTheme"));
						editor.putBoolean("disableClearURLActivity", obj.getBoolean("disableClearURLActivity"));
						editor.putBoolean("disableUnshortURLActivity", obj.getBoolean("disableUnshortURLActivity"));

						editor.commit();
					} catch (final IOException | JSONException e) {
						PackageUtils.showSnackbar(view, "Error importing preferences file");
						return;
					}

					PackageUtils.showSnackbar(view, "Please restart for changes to take effect");
				}
			});

	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			
			switch (key) {
				case "disableClearURLActivity":
					if (preferences.getBoolean(key, false)) {
						PackageUtils.disableComponent(getApplicationContext(), PackageUtils.CLEAR_URL_COMPONENT);
					} else {
						PackageUtils.enableComponent(getApplicationContext(), PackageUtils.CLEAR_URL_COMPONENT);
					}
					break;
				case "disableUnshortURLActivity":
					if (preferences.getBoolean(key, false)) {
						PackageUtils.disableComponent(getApplicationContext(), PackageUtils.UNSHORT_URL_COMPONENT);
					} else {
						PackageUtils.enableComponent(getApplicationContext(), PackageUtils.UNSHORT_URL_COMPONENT);
					}
					break;
				case "dns":
					final EditTextPreference customDns = preferenceScreen.findPreference("customDns");
					
					if (preferences.getString(key, "follow_system").equals("custom")) {
						customDns.setEnabled(true);
					} else {
						customDns.setEnabled(false);
					}
					
					break;
				case "socks5Proxy":
				case "proxyAuthentication":
					updateProxyPreferences(preferences, preferenceScreen);
					break;
				case "userAgent":
					final EditTextPreference customUserAgent = preferenceScreen.findPreference("customUserAgent");
					
					if (preferences.getString(key, "").equals("custom")) {
						customUserAgent.setEnabled(true);
					} else {
						customUserAgent.setEnabled(false);
					}
			}

		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		// Action bar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
		setSupportActionBar(toolbar);

		settingsFragment = new SettingsFragment();

		// Preferences screen
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_layout_settings, settingsFragment)
			.commit();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Preferences stuff
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		preferenceScreen = settingsFragment.getPreferenceScreen();
		updateProxyPreferences(preferences, preferenceScreen);
		
		final String userAgent = preferences.getString("userAgent", "");
		
		if (userAgent.equals("custom")) {
			final EditTextPreference customUserAgent = preferenceScreen.findPreference("customUserAgent");
			customUserAgent.setEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_export:
				final Calendar calendar = Calendar.getInstance();
				final Date currentLocalTime = calendar.getTime();
				final DateFormat date = new SimpleDateFormat("dd-MM-yyy_HH-mm-ss");

				final Intent exportIntent = new Intent();
				exportIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
				exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
				exportIntent.setType("application/json");
				exportIntent.putExtra(Intent.EXTRA_TITLE, String.format("unalix_settings_%s.json", date.format(currentLocalTime)));

				exportPreferences.launch(exportIntent);

				return true;
			case R.id.settings_import:
				final Intent importIntent = new Intent();
				importIntent.setAction(Intent.ACTION_GET_CONTENT);
				importIntent.addCategory(Intent.CATEGORY_OPENABLE);
				importIntent.setType("*/*");

				importPreferences.launch(importIntent);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	public void updateProxyPreferences(final SharedPreferences preferences, final PreferenceScreen pScreen) {
		final ListPreference dns = pScreen.findPreference("dns");
		final EditTextPreference customDns = pScreen.findPreference("customDns");
		
		final EditTextPreference proxyAddress = pScreen.findPreference("proxyAddress");
		final EditTextPreference proxyPort = pScreen.findPreference("proxyPort");
		
		final CheckBoxPreference proxyAuthentication = pScreen.findPreference("proxyAuthentication");
		
		final EditTextPreference proxyUsername = pScreen.findPreference("proxyUsername");
		final EditTextPreference proxyPassword = pScreen.findPreference("proxyPassword");
		
		final boolean socks5Proxy = preferences.getBoolean("socks5Proxy", false);
		
		if (socks5Proxy) {
			proxyAddress.setEnabled(true);
			proxyPort.setEnabled(true);
			proxyAuthentication.setEnabled(true);
			
			dns.setEnabled(false);
			customDns.setEnabled(false);
			
			final boolean proxyAuthenticationPref = preferences.getBoolean("proxyAuthentication", false);
			
			if (proxyAuthenticationPref) {
				proxyUsername.setEnabled(true);
				proxyPassword.setEnabled(true);
			} else {
				proxyUsername.setEnabled(false);
				proxyPassword.setEnabled(false);
			}
		} else {
			proxyAddress.setEnabled(false);
			proxyPort.setEnabled(false);
			proxyAuthentication.setEnabled(false);
			
			dns.setEnabled(true);
			
			proxyUsername.setEnabled(false);
			proxyPassword.setEnabled(false);
		}
		
		if (dns.isEnabled()) {
			if (preferences.getString("dns", "follow_system").equals("custom")) {
				customDns.setEnabled(true);
			}
		}
	}

}