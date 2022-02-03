package com.amanoteam.unalix.core;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.amanoteam.unalix.utilities.PackageUtils;
import com.google.android.material.color.DynamicColors;

public class CoreApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Dark mode stuff
		final String appTheme = settings.getString("appTheme", "follow_system");

		PackageUtils.setAppTheme(appTheme);

		DynamicColors.applyToActivitiesIfAvailable(this);
	}
}
