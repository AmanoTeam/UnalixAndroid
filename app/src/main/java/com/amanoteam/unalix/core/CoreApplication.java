package com.amanoteam.unalix.core;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import com.google.android.material.color.DynamicColors;

import com.amanoteam.unalix.utilities.PackageUtils;

public class CoreApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		final String appTheme = settings.getString("appTheme", "follow_system");
		PackageUtils.setAppTheme(appTheme);

		DynamicColors.applyToActivitiesIfAvailable(this);
	}
	
}
