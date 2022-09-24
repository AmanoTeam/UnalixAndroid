package com.amanoteam.unalix.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.fragment.app.FragmentActivity;


import com.amanoteam.unalix.R;
import com.amanoteam.unalix.databinding.CleanUrlFragmentBinding;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class CleanURLFragment extends Fragment {

	private Unalix unalix;

	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (preferences, key) -> {
		if (!key.equals("appTheme")) {
			final Context context = getActivity().getApplicationContext();
			unalix.setFromPreferences(context);
		}
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final CleanUrlFragmentBinding binding = CleanUrlFragmentBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {
		final FragmentActivity activity = getActivity();
		final Context context = activity.getApplicationContext();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		final FloatingActionButton openUrlButton = fragmentView.findViewById(R.id.open_url_button);
		final FloatingActionButton cleanUrlButton = fragmentView.findViewById(R.id.clean_url_button);
		final FloatingActionButton shareUrlButton = fragmentView.findViewById(R.id.share_url_button);
		final FloatingActionButton clearInputButton = fragmentView.findViewById(R.id.clear_input_button);

		final TextInputEditText urlInput = fragmentView.findViewById(R.id.url_input);

		// "Clean URL" button listener
		cleanUrlButton.setOnClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);

			if (url == null) {
				return;
			}

			final String cleanedUrl = unalix.cleanUrl(url);

			urlInput.setText(cleanedUrl);
			urlInput.setSelection(cleanedUrl.length());
		});

		// "Unshort URL" button listener
		cleanUrlButton.setOnLongClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);

			if (url == null) {
				return true;
			}

			PackageUtils.showProgressSnackbar(activity, view, "Resolving URL");

			new Thread(() -> {
				final String cleanedUrl = unalix.unshortUrl(url);

				view.post(() -> {
					urlInput.setText(cleanedUrl);
					urlInput.setSelection(cleanedUrl.length());
				});

				PackageUtils.showSnackbar(view, "Done");
			}).start();

			return true;
		});

		// "Open URL" button listener
		openUrlButton.setOnClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);

			if (url == null) {
				return;
			}
			
			final boolean preferNativeIntentChooser = preferences.getBoolean("preferNativeIntentChooser", false);
			
			if (preferNativeIntentChooser) {
				PackageUtils.createChooser(context, url, Intent.ACTION_VIEW);
			} else {
				PackageUtils.createChooserNew(activity, url, Intent.ACTION_VIEW);
			}
		});

		// "Share URL" button listener
		shareUrlButton.setOnClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);

			if (url == null) {
				return;
			}
			
			final boolean preferNativeIntentChooser = preferences.getBoolean("preferNativeIntentChooser", false);
			
			if (preferNativeIntentChooser) {
				PackageUtils.createChooser(context, url, Intent.ACTION_SEND);
			} else {
				PackageUtils.createChooserNew(activity, url, Intent.ACTION_SEND);
			}
		});

		// "Copy to clipboard" button listener
		shareUrlButton.setOnLongClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);

			if (url == null) {
				return true;
			}
			
			PackageUtils.copyToClipboard(context, url);
			PackageUtils.showSnackbar(view, "Copied to clipboard");

			return true;
		});

		// "Clear URL input" button listener
		clearInputButton.setOnClickListener((final View view) -> {
			urlInput.setError(null);
			urlInput.getText().clear();;
		});

		unalix = new Unalix(context);
	}

	@Override
	public void onDestroy() {
		final Context context = getActivity().getApplicationContext();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		super.onDestroy();
	}

}
