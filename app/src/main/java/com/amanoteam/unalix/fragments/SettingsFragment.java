package com.amanoteam.unalix.fragments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.fragments.SettingsFragment;
import com.amanoteam.unalix.utilities.PackageUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

	private static final String PREF_IGNORE_REFERRAL_MARKETING = "ignoreReferralMarketing";
	private static final String PREF_IGNORE_RULES = "ignoreRules";
	private static final String PREF_IGNORE_EXCEPTIONS = "ignoreExceptions";
	private static final String PREF_IGNORE_RAW_RULES = "ignoreRawRules";
	private static final String PREF_IGNORE_REDIRECTIONS = "ignoreRedirections";
	private static final String PREF_SKIP_BLOCKED = "skipBlocked";

	private static final String PREF_TIMEOUT = "timeout";
	private static final String PREF_MAX_REDIRECTS = "maxRedirects";
	private static final String PREF_USER_AGENT = "userAgent";
	private static final String PREF_CUSTOM_USER_AGENT = "customUserAgent";

	private static final String PREF_DNS = "dns";
	private static final String PREF_CUSTOM_DNS = "customDns";

	private static final String PREF_SOCKS5_PROXY = "socks5Proxy";
	private static final String PREF_PROXY_ADDRESS = "proxyAddress";
	private static final String PREF_PROXY_PORT = "proxyPort";
	private static final String PREF_PROXY_AUTHENTICATION = "proxyAuthentication";
	private static final String PREF_PROXY_USERNAME = "proxyUsername";
	private static final String PREF_PROXY_PASSWORD = "proxyPassword";

	private static final String PREF_APP_THEME = "appTheme";
	private static final String PREF_DISABLE_CLEANURL_ACTIVITY = "disableCleanURLActivity";
	private static final String PREF_DISABLE_UNSHORTURL_ACTIVITY = "disableUnshortURLActivity";

	// "Export" preferences listener
	private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(new StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {

			final Context context = getActivity().getApplicationContext();
			final View view = getView();

			final Intent intent = result.getData();
			final Uri fileUri = intent.getData();

			final ContentResolver contentResolver = context.getContentResolver();

			try {
				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				final JSONObject obj = new JSONObject();

				obj.put(PREF_IGNORE_REFERRAL_MARKETING, preferences.getBoolean(PREF_IGNORE_REFERRAL_MARKETING, false));
				obj.put(PREF_IGNORE_RULES, preferences.getBoolean(PREF_IGNORE_RULES, false));
				obj.put(PREF_IGNORE_EXCEPTIONS, preferences.getBoolean(PREF_IGNORE_EXCEPTIONS, false));
				obj.put(PREF_IGNORE_RAW_RULES, preferences.getBoolean(PREF_IGNORE_RAW_RULES, false));
				obj.put(PREF_IGNORE_REDIRECTIONS, preferences.getBoolean(PREF_IGNORE_REDIRECTIONS, false));
				obj.put(PREF_SKIP_BLOCKED, preferences.getBoolean(PREF_SKIP_BLOCKED, false));

				obj.put(PREF_TIMEOUT, Integer.valueOf(preferences.getString(PREF_TIMEOUT, "")));
				obj.put(PREF_MAX_REDIRECTS, Integer.valueOf(preferences.getString(PREF_MAX_REDIRECTS, "")));
				obj.put(PREF_USER_AGENT, preferences.getString(PREF_USER_AGENT, ""));
				obj.put(PREF_CUSTOM_USER_AGENT, preferences.getString(PREF_CUSTOM_USER_AGENT, ""));

				obj.put(PREF_DNS, preferences.getString(PREF_DNS, ""));
				obj.put(PREF_CUSTOM_DNS, preferences.getString(PREF_CUSTOM_DNS, ""));

				obj.put(PREF_SOCKS5_PROXY, preferences.getBoolean(PREF_SOCKS5_PROXY, false));
				obj.put(PREF_PROXY_ADDRESS, preferences.getString(PREF_PROXY_ADDRESS, ""));
				obj.put(PREF_PROXY_PORT, preferences.getString(PREF_PROXY_PORT, ""));
				obj.put(PREF_PROXY_AUTHENTICATION, preferences.getBoolean(PREF_PROXY_AUTHENTICATION, false));
				obj.put(PREF_PROXY_USERNAME, preferences.getString(PREF_PROXY_USERNAME, ""));
				obj.put(PREF_PROXY_PASSWORD, preferences.getString(PREF_PROXY_PASSWORD, ""));

				obj.put(PREF_APP_THEME, preferences.getString(PREF_APP_THEME, ""));
				obj.put(PREF_DISABLE_CLEANURL_ACTIVITY, preferences.getBoolean(PREF_DISABLE_CLEANURL_ACTIVITY, false));
				obj.put(PREF_DISABLE_UNSHORTURL_ACTIVITY, preferences.getBoolean(PREF_DISABLE_UNSHORTURL_ACTIVITY, false));

				final OutputStream outputStream = contentResolver.openOutputStream(fileUri);

				outputStream.write(obj.toString().getBytes());
				outputStream.close();
			} catch (final IOException | JSONException e) {
				PackageUtils.showSnackbar(view, "Error exporting preferences file");
				return;
			}

			PackageUtils.showSnackbar(view, "Export successful");
		}
	});

	// "Import" preferences listener
	private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(new StartActivityForResult(), result -> {
		if (result.getResultCode() == Activity.RESULT_OK) {

			final Context context = getActivity().getApplicationContext();
			final View view = getView();

			final Intent intent = result.getData();
			final Uri fileUri = intent.getData();

			final ContentResolver contentResolver = context.getContentResolver();

			try {
				final InputStream inputStream = contentResolver.openInputStream(fileUri);
				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()));
				final StringBuilder stringBuilder = new StringBuilder();

				String inputLine;

				while ((inputLine = bufferedReader.readLine()) != null) {
					stringBuilder.append(inputLine);
				}

				inputStream.close();

				final JSONObject obj = new JSONObject(stringBuilder.toString());

				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				final Editor editor = preferences.edit();

				editor.putBoolean(PREF_IGNORE_REFERRAL_MARKETING, obj.getBoolean(PREF_IGNORE_REFERRAL_MARKETING));
				editor.putBoolean(PREF_IGNORE_RULES, obj.getBoolean(PREF_IGNORE_RULES));
				editor.putBoolean(PREF_IGNORE_EXCEPTIONS, obj.getBoolean(PREF_IGNORE_EXCEPTIONS));
				editor.putBoolean(PREF_IGNORE_RAW_RULES, obj.getBoolean(PREF_IGNORE_RAW_RULES));
				editor.putBoolean(PREF_IGNORE_REDIRECTIONS, obj.getBoolean(PREF_IGNORE_REDIRECTIONS));
				editor.putBoolean(PREF_SKIP_BLOCKED, obj.getBoolean(PREF_SKIP_BLOCKED));

				editor.putString(PREF_TIMEOUT, String.valueOf(obj.getInt(PREF_TIMEOUT)));
				editor.putString(PREF_MAX_REDIRECTS, String.valueOf(obj.getInt(PREF_MAX_REDIRECTS)));
				editor.putString(PREF_USER_AGENT, obj.getString(PREF_USER_AGENT));
				editor.putString(PREF_CUSTOM_USER_AGENT, obj.getString(PREF_CUSTOM_USER_AGENT));

				editor.putString(PREF_DNS, obj.getString(PREF_DNS));
				editor.putString(PREF_CUSTOM_DNS, obj.getString(PREF_CUSTOM_DNS));

				editor.putBoolean(PREF_SOCKS5_PROXY, obj.getBoolean(PREF_SOCKS5_PROXY));
				editor.putString(PREF_PROXY_ADDRESS, obj.getString(PREF_PROXY_ADDRESS));
				editor.putString(PREF_PROXY_PORT, String.valueOf(obj.getInt(PREF_PROXY_PORT)));
				editor.putBoolean(PREF_PROXY_AUTHENTICATION, obj.getBoolean(PREF_PROXY_AUTHENTICATION));
				editor.putString(PREF_PROXY_USERNAME, obj.getString(PREF_PROXY_USERNAME));
				editor.putString(PREF_PROXY_PASSWORD, obj.getString(PREF_PROXY_PASSWORD));

				editor.putString(PREF_APP_THEME, obj.getString(PREF_APP_THEME));
				editor.putBoolean(PREF_DISABLE_CLEANURL_ACTIVITY, obj.getBoolean(PREF_DISABLE_CLEANURL_ACTIVITY));
				editor.putBoolean(PREF_DISABLE_UNSHORTURL_ACTIVITY, obj.getBoolean(PREF_DISABLE_UNSHORTURL_ACTIVITY));

				editor.commit();
			} catch (final IOException | JSONException e) {
				PackageUtils.showSnackbar(view, "Error importing preferences file");
				return;
			}

			PackageUtils.showSnackbar(view, "Please restart for changes to take effect");
		}
	});

	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (preferences, key) -> {
		final Context context = getActivity().getApplicationContext();

		final PreferenceScreen preferenceScreen = getPreferenceScreen();

		switch (key) {
			case PREF_DISABLE_CLEANURL_ACTIVITY:
			case PREF_DISABLE_UNSHORTURL_ACTIVITY:
				final ComponentName component = (key.equals(PREF_DISABLE_CLEANURL_ACTIVITY)) ? PackageUtils.CLEAN_URL_COMPONENT : PackageUtils.UNSHORT_URL_COMPONENT;

				if (preferences.getBoolean(key, false)) {
					PackageUtils.disableComponent(context, component);
				} else {
					PackageUtils.enableComponent(context, component);
				}

				break;
			case PREF_DNS:
				final EditTextPreference customDns = preferenceScreen.findPreference("customDns");

				if (preferences.getString(key, "").equals("custom")) {
					customDns.setEnabled(true);
				} else {
					customDns.setEnabled(false);
				}

				break;
			case PREF_SOCKS5_PROXY:
			case PREF_PROXY_AUTHENTICATION:
				updateProxyPreferences(preferences, preferenceScreen);
				break;
			case PREF_USER_AGENT:
				final EditTextPreference customUserAgent = preferenceScreen.findPreference(PREF_CUSTOM_USER_AGENT);

				if (preferences.getString(key, "").equals("custom")) {
					customUserAgent.setEnabled(true);
				} else {
					customUserAgent.setEnabled(false);
				}
		}

	};

	@Override
	public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Context context = getActivity().getApplicationContext();

		final PreferenceScreen preferenceScreen = getPreferenceScreen();

		final Preference backupPreference = findPreference("backup");
		backupPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				final Calendar calendar = Calendar.getInstance();
				final Date date = calendar.getTime();
				final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy_HH-mm-ss");

				final Intent intent = new Intent();
				intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/json");
				intent.putExtra(Intent.EXTRA_TITLE, String.format("unalix_settings_%s.json", dateFormat.format(date)));

				try {
					exportPreferences.launch(intent);
				} catch (final ActivityNotFoundException e) {
					PackageUtils.showSnackbar(view, "There are no document providers available to handle this action");
				}

				return true;
			}
		});

		final Preference restorePreference = findPreference("restore");
		restorePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				final Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");

				try {
					importPreferences.launch(intent);
				} catch (final ActivityNotFoundException e) {
					PackageUtils.showSnackbar(view, "There are no document providers available to handle this action");
				}

				return true;
			}
		});

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		updateProxyPreferences(preferences, preferenceScreen);
	}

	@Override
	public void onDestroy() {
		final Context context = getActivity().getApplicationContext();

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		super.onDestroy();
	}

	private void updateProxyPreferences(final SharedPreferences preferences, final PreferenceScreen preferenceScreen) {
		final ListPreference dns = preferenceScreen.findPreference(PREF_DNS);
		final EditTextPreference customDns = preferenceScreen.findPreference(PREF_CUSTOM_DNS);

		final EditTextPreference proxyAddress = preferenceScreen.findPreference(PREF_PROXY_ADDRESS);
		final EditTextPreference proxyPort = preferenceScreen.findPreference(PREF_PROXY_PORT);

		final SwitchPreference proxyAuthentication = preferenceScreen.findPreference(PREF_PROXY_AUTHENTICATION);

		final EditTextPreference proxyUsername = preferenceScreen.findPreference(PREF_PROXY_USERNAME);
		final EditTextPreference proxyPassword = preferenceScreen.findPreference(PREF_PROXY_PASSWORD);

		final boolean socks5Proxy = preferences.getBoolean(PREF_SOCKS5_PROXY, false);

		if (socks5Proxy) {
			proxyAddress.setEnabled(true);
			proxyPort.setEnabled(true);
			proxyAuthentication.setEnabled(true);

			dns.setEnabled(false);
			customDns.setEnabled(false);

			final boolean proxyAuthenticationPref = preferences.getBoolean(PREF_PROXY_AUTHENTICATION, false);

			if (proxyAuthenticationPref) {
				proxyUsername.setEnabled(true);
				proxyPassword.setEnabled(true);
			} else {
				proxyUsername.setEnabled(false);
				proxyPassword.setEnabled(false);
			}
		} else {
			proxyAddress.setEnabled(false);
			proxyPort.setEnabled(false);
			proxyAuthentication.setEnabled(false);

			dns.setEnabled(true);

			proxyUsername.setEnabled(false);
			proxyPassword.setEnabled(false);
		}

		if (dns.isEnabled()) {
			if (preferences.getString(PREF_DNS, "").equals("custom")) {
				customDns.setEnabled(true);
			}
		}
	}

}
