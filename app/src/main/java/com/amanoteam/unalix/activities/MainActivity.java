package com.amanoteam.unalix.activities;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Gravity;
import androidx.core.widget.NestedScrollView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.widget.LinearLayout;
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
import com.amanoteam.unalix.fragments.RulesetsFragment;
import com.amanoteam.unalix.fragments.SettingsFragment;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;

public class MainActivity extends AppCompatActivity {
	
	private static final String CLEAN_URL_FRAGMENT_TAG = "CleanURLFragment";
	private static final String RULESETS_FRAGMENT_TAG = "RulesetsFragment";
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
		
		final Fragment currentFragment = getCurrentDisplayedFragment();
		
		final Fragment cleanURLFragment = fragmentManager.findFragmentByTag(CLEAN_URL_FRAGMENT_TAG);
		final Fragment rulesetsFragment = fragmentManager.findFragmentByTag(RULESETS_FRAGMENT_TAG);
		final Fragment settingsFragment = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
		
		final MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
		
		final NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.main_scroll_view);
		final CoordinatorLayout.LayoutParams scrollViewParams = (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
		
		final FloatingActionButton addRulesetButton = (FloatingActionButton) findViewById(R.id.add_ruleset_button);
		
		switch (item.getItemId()) {
			case R.id.bottom_navigation_home:
				fragmentTransaction.hide(currentFragment);
				fragmentTransaction.show(cleanURLFragment);
				fragmentTransaction.commit();
				
				toolbar.setTitle("Unalix");
				
				addRulesetButton.setVisibility(View.GONE);
				
				scrollViewParams.gravity = Gravity.CENTER_HORIZONTAL;
				scrollView.setLayoutParams(scrollViewParams);
				
				return true;
			case R.id.bottom_navigation_rulesets:
				PackageUtils.hideKeyboard(MainActivity.this);
				
				fragmentTransaction.hide(currentFragment);
				fragmentTransaction.show(rulesetsFragment);
				fragmentTransaction.commit();
				
				toolbar.setTitle("Rulesets");
				
				addRulesetButton.setVisibility(View.VISIBLE);
				
				scrollViewParams.gravity = Gravity.TOP;
				scrollView.setLayoutParams(scrollViewParams);
				
				return true;
			case R.id.bottom_navigation_settings:
				PackageUtils.hideKeyboard(MainActivity.this);
				
				fragmentTransaction.hide(currentFragment);
				fragmentTransaction.show(settingsFragment);
				fragmentTransaction.commit();
				
				toolbar.setTitle("Settings");
				
				addRulesetButton.setVisibility(View.GONE);
				
				scrollViewParams.gravity = Gravity.TOP;
				scrollView.setLayoutParams(scrollViewParams);
				
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
			final RulesetsFragment rulesetsFragment = new RulesetsFragment();
			final SettingsFragment settingsFragment = new SettingsFragment();
			
			final FragmentManager fragmentManager = getSupportFragmentManager();
			final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			fragmentTransaction.add(R.id.fragment_container_view, settingsFragment, SETTINGS_FRAGMENT_TAG);
			fragmentTransaction.hide(settingsFragment);
			
			fragmentTransaction.add(R.id.fragment_container_view, rulesetsFragment, RULESETS_FRAGMENT_TAG);
			fragmentTransaction.hide(rulesetsFragment);
			
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
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
		
		final Fragment cleanURLFragment = fragmentManager.findFragmentByTag(CLEAN_URL_FRAGMENT_TAG);
		
		if (!cleanURLFragment.isVisible()) {
			final NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);
			navigationBarView.setSelectedItemId(R.id.bottom_navigation_home);
		} else {
			finishAndRemoveTask();
		}
	}
	
	private Fragment getCurrentDisplayedFragment() {
		
		final FragmentManager fragmentManager = getSupportFragmentManager();
		
		final Fragment cleanURLFragment = fragmentManager.findFragmentByTag(CLEAN_URL_FRAGMENT_TAG);
		
		if (cleanURLFragment.isVisible()) {
			return cleanURLFragment;
		}
		
		final Fragment rulesetsFragment = fragmentManager.findFragmentByTag(RULESETS_FRAGMENT_TAG);
		
		if (rulesetsFragment.isVisible()) {
			return rulesetsFragment;
		}
		
		final Fragment settingsFragment = fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG);
		
		if (settingsFragment.isVisible()) {
			return settingsFragment;
		}
		
		return null;
	}
	
}