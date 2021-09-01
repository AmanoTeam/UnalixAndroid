package com.amanoteam.unalix;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.amanoteam.unalix.R;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
	
}