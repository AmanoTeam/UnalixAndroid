package com.amanoteam.unalix.activities;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener;
import com.google.android.material.navigation.NavigationBarView;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.fragments.CleanURLFragment;
import com.amanoteam.unalix.fragments.SettingsFragment;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;

public class MainActivity extends AppCompatActivity {
	
	private static final String CLEAN_URL_FRAGMENT_TAG = "CleanURLFragment";
	private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragment";
	
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (settings, key) -> {
		if (key.equals("appTheme")) {
			final String appTheme = settings.getString("appTheme", "follow_system");
			PackageUtils.setAppTheme(appTheme);
		}
	};
	
	private final OnItemSelectedListener onNavigationItemSelected = (item) -> {
		final FragmentManager fragmentManager = getSupportFragmentManager();
		final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		final Fragment cleanURLFragment = fragmentManager.findFragmentByTag(CLEAN_URL_FRAGMENT_TAG);
		final Fragment settingsFragment = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
		
		final MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
		
		switch (item.getItemId()) {
			case R.id.bottom_navigation_home:
				fragmentTransaction.hide(settingsFragment);
				fragmentTransaction.show(cleanURLFragment);
				fragmentTransaction.commit();
				
				toolbar.setTitle("Unalix");
				
				return true;
			case R.id.bottom_navigation_settings:
				fragmentTransaction.hide(cleanURLFragment);
				fragmentTransaction.show(settingsFragment);
				fragmentTransaction.commit();
				
				toolbar.setTitle("Settings");
				
				return true;
			default:
				return false;
		}
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			final CleanURLFragment cleanURLFragment = new CleanURLFragment();
			final SettingsFragment settingsFragment = new SettingsFragment();
			
			final FragmentManager fragmentManager = getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			fragmentTransaction.add(R.id.fragment_container_view, settingsFragment, SETTINGS_FRAGMENT_TAG);
			fragmentTransaction.hide(settingsFragment);
			
			fragmentTransaction.add(R.id.fragment_container_view, cleanURLFragment, CLEAN_URL_FRAGMENT_TAG);
			fragmentTransaction.commit();
		}
		
		final MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		final NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
		navigationBarView.setOnItemSelectedListener(onNavigationItemSelected);
		
		final Window window = getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
	
	@Override
	protected void onDestroy() {
		// Unregister preferences callback
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		final NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
		navigationBarView.setOnItemSelectedListener(null);
		
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		final FragmentManager fragmentManager = getSupportFragmentManager();
		
		final Fragment settingsFragment = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
		final Fragment cleanURLFragment = fragmentManager.findFragmentByTag(CLEAN_URL_FRAGMENT_TAG);
		
		if (settingsFragment.isVisible()) {
			final NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
			navigationBarView.setSelectedItemId(R.id.bottom_navigation_home);
			
			final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.hide(settingsFragment);
			fragmentTransaction.show(cleanURLFragment);
			fragmentTransaction.commit();
			
			final MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
			toolbar.setTitle("Unalix");
		} else {
			finishAndRemoveTask();
		}
	}
	
}