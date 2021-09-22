package com.amanoteam.unalix;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import com.amanoteam.unalix.wrapper.Unalix;
import com.amanoteam.unalix.R;
import com.amanoteam.unalix.SettingsActivity;

public class MainActivity extends AppCompatActivity {
	
	private Unalix unalix;
	
	private AppCompatEditText urlInput;
	
	private PackageManager packageManager;
	
	private final ArrayList<ComponentName> excludeTargets = new ArrayList<>();
	
	private final ComponentName clearUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.ClearURLActivity");
	private final ComponentName unshortUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.UnshortURLActivity");
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// Set package manager
		packageManager = getPackageManager();
		
		// Set exclude targets
		excludeTargets.add(clearUrlActivity);
		excludeTargets.add(unshortUrlActivity);
		
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
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
		
		setContentView(R.layout.activity_main);
		
		// ImageButton and EditText stuff
		final AppCompatImageButton openUrlButton = (AppCompatImageButton) findViewById(R.id.open_url_button);
		final AppCompatImageButton cleanUrlButton = (AppCompatImageButton) findViewById(R.id.clean_url_button);
		final AppCompatImageButton shareUrlButton = (AppCompatImageButton) findViewById(R.id.share_url_button);
		final AppCompatImageButton clearInputButton = (AppCompatImageButton) findViewById(R.id.clear_input_button);
		
		urlInput = (AppCompatEditText) findViewById(R.id.url_input);
		
		// This needs to come after setContentView() (see https://stackoverflow.com/a/43635618)
		if (isDarkMode) {
			urlInput.setBackgroundResource(R.drawable.edit_text_border_dark);
			openUrlButton.setBackgroundResource(R.drawable.edit_text_border_dark);
			cleanUrlButton.setBackgroundResource(R.drawable.edit_text_border_dark);
			shareUrlButton.setBackgroundResource(R.drawable.edit_text_border_dark);
			clearInputButton.setBackgroundResource(R.drawable.edit_text_border_dark);
		}
		
		// "Clean URL" button listener
		cleanUrlButton.setOnClickListener(new OnClickListener() {
			 public void onClick(final View view) {
				
				final String text = urlInput.getText().toString();
				
				if (TextUtils.isEmpty(text)) {
					Toast.makeText(getApplicationContext(), "There is no URL to clean", Toast.LENGTH_SHORT).show();
					return;
				}
				
				final String cleanedUrl = unalix.clearUrl(text);
				
				urlInput.setText(cleanedUrl);
			 }
		 });
		
		// "Open URL" button listener
		openUrlButton.setOnClickListener(new OnClickListener() {
			 public void onClick(final View view) {
				
				final String text = urlInput.getText().toString();
				
				if (TextUtils.isEmpty(text)) {
					Toast.makeText(getApplicationContext(), "There is no URL to launch", Toast.LENGTH_SHORT).show();
					return;
				}
				
				final Intent sendIntent = new Intent();
				
				sendIntent.setAction(Intent.ACTION_VIEW);
				sendIntent.setData(Uri.parse(text));
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					final Intent chooserIntent = Intent.createChooser(sendIntent, "Open with");
					chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeTargets.toArray(new ComponentName[0]));
					startActivity(chooserIntent);
				} else {
					final List<Intent> intentsList = new ArrayList<>();
					final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
					
					for (ResolveInfo resolveInfoItem : resolveInfoList) {
						Intent targetIntent = (Intent) sendIntent.clone();
						
						String packageName = resolveInfoItem.activityInfo.packageName;
						String activityName = resolveInfoItem.activityInfo.name;
						
						if (activityName.equals("com.amanoteam.unalix.ClearURLActivity") || activityName.equals("com.amanoteam.unalix.UnshortURLActivity")) {
							continue;
						}
						
						targetIntent.setComponent(new ComponentName(packageName, activityName));
						intentsList.add(targetIntent);
					}
		
					final Intent chooserIntent = Intent.createChooser(intentsList.remove(0), "Open with");
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentsList.toArray(new Parcelable[intentsList.size()]));
					
					startActivity(chooserIntent);
				}
			}
		 });
		
		// "Share URL" button listener
		shareUrlButton.setOnClickListener(new OnClickListener() {
			 public void onClick(final View view) {
				
				final String text = urlInput.getText().toString();
				
				if (TextUtils.isEmpty(text)) {
					Toast.makeText(getApplicationContext(), "There is no URL to share", Toast.LENGTH_SHORT).show();
					return;
				}
				
				final Intent sendIntent = new Intent();
				
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, text);
				sendIntent.setType("text/plain");
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					final Intent chooserIntent = Intent.createChooser(sendIntent, "Share with");
					chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeTargets.toArray(new ComponentName[0]));
					startActivity(chooserIntent);
				} else {
					final List<Intent> intentsList = new ArrayList<>();
					final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
					
					for (ResolveInfo resolveInfoItem : resolveInfoList) {
						Intent targetIntent = (Intent) sendIntent.clone();
						
						String packageName = resolveInfoItem.activityInfo.packageName;
						String activityName = resolveInfoItem.activityInfo.name;
						
						if (activityName.equals("com.amanoteam.unalix.ClearURLActivity") || activityName.equals("com.amanoteam.unalix.UnshortURLActivity")) {
							continue;
						}
						
						targetIntent.setComponent(new ComponentName(packageName, activityName));
						intentsList.add(targetIntent);
					}
		
					final Intent chooserIntent = Intent.createChooser(intentsList.remove(0), "Share with");
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentsList.toArray(new Parcelable[intentsList.size()]));
					
					startActivity(chooserIntent);
				}
			}
		 });
		
		// "Clean URL input" button listener
		clearInputButton.setOnClickListener(new OnClickListener() {
			 public void onClick(final View view) {
				
				final String text = urlInput.getText().toString();
				
				if (TextUtils.isEmpty(text)) {
					Toast.makeText(getApplicationContext(), "URL input is already empty", Toast.LENGTH_SHORT).show();
					return;
				}
				
				urlInput.getText().clear();
			 }
		 });
		
		// Action bar stuff
		final Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		
		setSupportActionBar(mainToolbar);
		
		// libunalix stuff
		unalix = new Unalix();
		unalix.setFromPreferences(settings);
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
				final Intent settingsIntent = new Intent(this, SettingsActivity.class);
				
				settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				startActivity(settingsIntent);
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
}