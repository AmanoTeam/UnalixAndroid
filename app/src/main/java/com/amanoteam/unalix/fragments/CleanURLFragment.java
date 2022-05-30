package com.amanoteam.unalix.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;

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
		return inflater.inflate(R.layout.clean_url_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(final View root, final Bundle savedInstanceState) {
		final Context context = getActivity().getApplicationContext();
		
		final FloatingActionButton openUrlButton = root.findViewById(R.id.open_url_button);
		final FloatingActionButton cleanUrlButton = root.findViewById(R.id.clean_url_button);
		final FloatingActionButton shareUrlButton = root.findViewById(R.id.share_url_button);
		final FloatingActionButton clearInputButton = root.findViewById(R.id.clear_input_button);

		final TextInputEditText urlInput = root.findViewById(R.id.url_input);

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
			
			PackageUtils.showProgressSnackbar(context, view, "Resolving URL");

			new Thread(new Runnable() {
				@Override
				public void run() {
					final String cleanedUrl = unalix.unshortUrl(url);

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							urlInput.setText(cleanedUrl);
							urlInput.setSelection(cleanedUrl.length());
						}
					});
					
					PackageUtils.showSnackbar(view, "Done");
					
				}
			}).start();
			
			return true;
		});

		// "Open URL" button listener
		openUrlButton.setOnClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);
			
			if (url == null) {
				return;
			}
			
			final Intent chooser = PackageUtils.createChooser(context, url, Intent.ACTION_VIEW);
			startActivity(chooser);
		});

		// "Share URL" button listener
		shareUrlButton.setOnClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);
			
			if (url == null) {
				return;
			}
			
			final Intent chooser = PackageUtils.createChooser(context, url, Intent.ACTION_SEND);
			startActivity(chooser);
		});

		// "Copy to clipboard" button listener
		shareUrlButton.setOnLongClickListener((final View view) -> {
			final String url = PackageUtils.getURL(urlInput);
			
			if (url == null) {
				return true;
			}
			
			final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
			final ClipData clipData = ClipData.newPlainText("Clean URL", url);
			clipboardManager.setPrimaryClip(clipData);
			
			PackageUtils.showSnackbar(view, "Copied to clipboard");
			
			return true;
		});

		// "Clear URL input" button listener
		clearInputButton.setOnClickListener((final View view) -> {
			urlInput.setError(null);
			urlInput.getText().clear();
		});
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
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