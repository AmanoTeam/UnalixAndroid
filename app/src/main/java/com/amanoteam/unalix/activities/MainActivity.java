package com.amanoteam.unalix.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.databinding.ActivityMainBinding;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (settings, key) -> {
		if (key.equals("appTheme")) {
			final String appTheme = settings.getString("appTheme", "follow_system");
			PackageUtils.setAppTheme(appTheme);
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		final MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
				R.id.navigation_home, R.id.navigation_rulesets, R.id.navigation_settings)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.fragment_container_view);
		NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
		NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

		navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> PackageUtils.hideKeyboard(MainActivity.this));

		final Window window = getWindow();
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

}
