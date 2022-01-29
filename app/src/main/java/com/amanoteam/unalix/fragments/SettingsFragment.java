package com.amanoteam.unalix.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.amanoteam.unalix.R;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
	
}