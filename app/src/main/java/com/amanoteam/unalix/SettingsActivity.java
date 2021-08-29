package com.amanoteam.unalix;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuInflater;
import android.view.Menu;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import androidx.preference.PreferenceManager;
import android.view.MenuItem;
import android.content.Intent;
import android.content.ContentResolver;
import android.os.Process;
import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import java.io.OutputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import java.io.File;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
	
	private final String BACKUP_FILE = "unalix_preferences.json";
	
	private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(ActivityResult result) {
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
					preferences.put("maxRedirects", Integer.valueOf(settings.getString("maxRedirects", "13")));
					preferences.put("connectTimeout", Integer.valueOf(settings.getString("connectTimeout", "3000")));
					preferences.put("readTimeout", Integer.valueOf(settings.getString("readTimeout", "3000")));
					preferences.put("readChunkSize", Integer.valueOf(settings.getString("readChunkSize", "1024")));
					preferences.put("dohUrl", settings.getString("dohUrl", "https://cloudflare-dns.com/dns-query"));
					preferences.put("dohAddress", settings.getString("dohAddress", "1.1.1.1"));
					preferences.put("dohPort", Integer.valueOf(settings.getString("dohPort", "443")));
					preferences.put("userAgent", settings.getString("userAgent", "UnalixAndroid/0.1 (+https://github.com/AmanoTeam/UnalixAndroid)"));
					preferences.put("appTheme", settings.getString("appTheme", "light"));
					
					final OutputStream fileOutputStream = contentResolver.openOutputStream(fileUri);
					
					fileOutputStream.write(preferences.toString().getBytes());
					fileOutputStream.close();
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error exporting preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Exported preferences file", Toast.LENGTH_SHORT).show();
			}
		}
	});
	private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(ActivityResult result) {
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
					editor.putString("maxRedirects", String.valueOf(preferences.getInt("maxRedirects")));
					editor.putString("connectTimeout", String.valueOf(preferences.getInt("connectTimeout")));
					editor.putString("readTimeout", String.valueOf(preferences.getInt("readTimeout")));
					editor.putString("readChunkSize", String.valueOf(preferences.getInt("readChunkSize")));
					editor.putString("dohUrl", preferences.getString("dohUrl"));
					editor.putString("dohAddress", preferences.getString("dohAddress"));
					editor.putString("dohPort", String.valueOf(preferences.getInt("dohPort")));
					editor.putString("userAgent", preferences.getString("userAgent"));
					editor.putString("appTheme", preferences.getString("appTheme"));
					
					editor.commit();
					
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error importing preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Please restart for changes to take effect", Toast.LENGTH_SHORT).show();
			}
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (settings.getString("appTheme", "light").equals("dark")) {
			setTheme(R.style.DarkTheme);
		} else {
			setTheme(R.style.LigthTheme);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		// Action bar
		final Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
		setSupportActionBar(myToolbar);
		
		// Preferences screen
		getSupportFragmentManager().beginTransaction().replace(R.id.fl_settings, new SettingsFragment()).commit();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_quit:
				System.exit(0);
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
			case R.id.settings_restart:
				recreate();
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
		
	}
	
}