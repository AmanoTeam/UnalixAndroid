package com.amanoteam.unalix;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportFragmentManager().beginTransaction().replace(R.id.fl_settings, new SettingsFragment()).commit();
	}

}