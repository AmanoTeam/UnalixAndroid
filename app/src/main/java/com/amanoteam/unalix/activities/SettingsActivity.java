package com.amanoteam.unalix.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import org.json.JSONException;
import org.json.JSONObject;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
	
	// "Export" preferences listener
	private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(final ActivityResult result) {
			if (result.getResultCode() == Activity.RESULT_OK) {
				
				final Context context = getApplicationContext();
				
				final Intent intent = result.getData();
				final Uri fileUri = intent.getData();
				
				final ContentResolver contentResolver =  getContentResolver();
				
				try {
					final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
					final JSONObject preferences = new JSONObject();
					
					preferences.put("ignoreReferralMarketing", settings.getBoolean("ignoreReferralMarketing", false));
					preferences.put("ignoreRules", settings.getBoolean("ignoreRules", false));
					preferences.put("ignoreExceptions", settings.getBoolean("ignoreExceptions", false));
					preferences.put("ignoreRawRules", settings.getBoolean("ignoreRawRules", false));
					preferences.put("ignoreRedirections", settings.getBoolean("ignoreRedirections", false));
					preferences.put("skipBlocked", settings.getBoolean("skipBlocked", false));
					preferences.put("stripDuplicates", settings.getBoolean("stripDuplicates", false));
					preferences.put("stripEmpty", settings.getBoolean("stripEmpty", false));
					
					preferences.put("appTheme", settings.getString("appTheme", "follow_system"));
					preferences.put("disableClearURLActivity", settings.getBoolean("disableClearURLActivity", false));
					preferences.put("disableUnshortURLActivity", settings.getBoolean("disableUnshortURLActivity", false));
					preferences.put("disableCopyToClipboardActivity", settings.getBoolean("disableCopyToClipboardActivity", false));
					
					final OutputStream fileOutputStream = contentResolver.openOutputStream(fileUri);
					
					fileOutputStream.write(preferences.toString().getBytes());
					fileOutputStream.close();
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error exporting preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show();
			}
		}
	});
	
	// "Import" preferences listener
	private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(final ActivityResult result) {
			if (result.getResultCode() == Activity.RESULT_OK) {
				
				final Context context = getApplicationContext();
				
				final Intent intent = result.getData();
				final Uri fileUri = intent.getData();
				
				final ContentResolver contentResolver =  getContentResolver();
				
				try {
					final InputStream fileInputStream = contentResolver.openInputStream(fileUri);
					final BufferedReader streamReader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8.name()));
					final StringBuilder text = new StringBuilder();
					
					String inputLine;
					
					while ((inputLine = streamReader.readLine()) != null)
						text.append(inputLine);
					
					fileInputStream.close();
					
					final JSONObject preferences = new JSONObject(text.toString());
					
					final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
					final Editor editor = settings.edit();
					
					editor.putBoolean("ignoreReferralMarketing", preferences.getBoolean("ignoreReferralMarketing"));
					editor.putBoolean("ignoreRules", preferences.getBoolean("ignoreRules"));
					editor.putBoolean("ignoreExceptions", preferences.getBoolean("ignoreExceptions"));
					editor.putBoolean("ignoreRawRules", preferences.getBoolean("ignoreRawRules"));
					editor.putBoolean("ignoreRedirections", preferences.getBoolean("ignoreRedirections"));
					editor.putBoolean("skipBlocked", preferences.getBoolean("skipBlocked"));
					editor.putBoolean("stripDuplicates", preferences.getBoolean("stripDuplicates"));
					editor.putBoolean("stripEmpty", preferences.getBoolean("stripEmpty"));
					
					editor.putString("appTheme", preferences.getString("appTheme"));
					editor.putBoolean("disableClearURLActivity", preferences.getBoolean("disableClearURLActivity"));
					editor.putBoolean("disableUnshortURLActivity", preferences.getBoolean("disableUnshortURLActivity"));
					editor.putBoolean("disableCopyToClipboardActivity", preferences.getBoolean("disableCopyToClipboardActivity"));
					
					editor.commit();
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error importing preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show();
			}
		}
	});
	
	private PackageManager packageManager;
	
	private final ComponentName clearUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.ClearURLActivity");
	private final ComponentName unshortUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.UnshortURLActivity");
	private final ComponentName copyToClipboardActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.CopyToClipboardActivity");
	
	private SettingsFragment settingsFragment;
	private PreferenceScreen preferenceScreen;
	
	private SharedPreferences settings;
	
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences settings, final String key) {
			
			if (key.equals("disableClearURLActivity")) {
				if (settings.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(clearUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(clearUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("disableUnshortURLActivity")) {
				if (settings.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(unshortUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(unshortUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("disableCopyToClipboardActivity")) {
				if (settings.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(copyToClipboardActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(copyToClipboardActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("appTheme")) {
				recreate();
			}
			
		}
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		packageManager = getPackageManager();
		
		// Preferences stuff
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		// Dark mode stuff
		final String appTheme = settings.getString("appTheme", "follow_system");
		
		boolean isDarkMode = false;
		
		if (appTheme.equals("follow_system")) {
			// Snippet from https://github.com/Andrew67/dark-mode-toggle/blob/11c1e16071b301071be0c4715a15fcb031d0bb64/app/src/main/java/com/andrew67/darkmode/UiModeManagerUtil.java#L17
			final UiModeManager uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
				isDarkMode = true;
			}
		} else if (appTheme.equals("dark")) {
			isDarkMode = true;
		}
		
		if (isDarkMode) {
			setTheme(R.style.DarkTheme);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		// Action bar
		final Toolbar settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
		setSupportActionBar(settingsToolbar);
		
		settingsFragment = new SettingsFragment();
		
		// Preferences screen
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_layout_settings, settingsFragment)
			.commit();
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
				exportIntent.putExtra(Intent.EXTRA_TITLE, "unalix_" + date.format(currentLocalTime) + ".json");
				
				exportPreferences.launch(exportIntent);
				
				return true;
			case R.id.settings_import:
				final Intent importIntent = new Intent();
				
				importIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				importIntent.addCategory(Intent.CATEGORY_OPENABLE);
				importIntent.setType("application/json");
				
				importPreferences.launch(importIntent);
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
}