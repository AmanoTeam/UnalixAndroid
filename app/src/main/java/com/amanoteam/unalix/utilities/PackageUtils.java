package com.amanoteam.unalix.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.R;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

public class PackageUtils {

	private static final String PACKAGE_NAME = "com.amanoteam.unalix";

	private static final String CLEAR_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.ClearURLActivity");
	private static final String UNSHORT_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.UnshortURLActivity");
	private static final String COPY_TO_CLIPBOARD_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.CopyToClipboardActivity");

	public static final ComponentName CLEAR_URL_COMPONENT = new ComponentName(PACKAGE_NAME, CLEAR_URL_ACTIVITY);
	public static final ComponentName UNSHORT_URL_COMPONENT = new ComponentName(PACKAGE_NAME, UNSHORT_URL_ACTIVITY);
	public static final ComponentName COPY_TO_CLIPBOARD_COMPONENT = new ComponentName(PACKAGE_NAME, COPY_TO_CLIPBOARD_ACTIVITY);

	private static final ComponentName[] CHOOSER_EXCLUDE_COMPONENTS = {
		CLEAR_URL_COMPONENT,
		UNSHORT_URL_COMPONENT
	};

	public static Intent createChooser(final Context context, final String url, final String action) {

		final Intent intent = new Intent();

		switch (action) {
			case Intent.ACTION_VIEW:
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				break;
			case Intent.ACTION_SEND:
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, url);
				intent.setType("text/plain");
				break;
			default:
				return null;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			final Intent chooser = Intent.createChooser(intent, url);
			chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, CHOOSER_EXCLUDE_COMPONENTS);
			chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			return chooser;
		}

		// Set package manager
		final PackageManager packageManager = context.getPackageManager();

		final List<Intent> intents = new ArrayList<Intent>();
		final List<ResolveInfo> targets = packageManager.queryIntentActivities(intent, 0);

		for (final ResolveInfo target : targets) {
			final String packageName = target.activityInfo.packageName;
			final String activityName = target.activityInfo.name;

			if (activityName.equals(CLEAR_URL_ACTIVITY) || activityName.equals(UNSHORT_URL_ACTIVITY)) {
				continue;
			}

			final Intent targetIntent = (Intent) intent.clone();
			targetIntent.setComponent(new ComponentName(packageName, activityName));
			intents.add(targetIntent);
		}

		final Intent chooser = Intent.createChooser(intents.remove(0), url);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return chooser;
	}

	public static void disableComponent(final Context context, final ComponentName component) {
		final PackageManager packageManager = context.getPackageManager();
		packageManager.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	public static void enableComponent(final Context context, final ComponentName component) {
		final PackageManager packageManager = context.getPackageManager();
		packageManager.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	public static void setAppTheme(final String appTheme) {
		switch (appTheme) {
			case "follow_system":
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				break;
			case "dark":
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;
			case "light":
			default:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;
		}
	}

	public static void showSnackbar(final View view, final String text) {
		final Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
		snackbar.show();
	}

	public static void showProgressSnackbar(final Context context, final View view, final String text) {
		final Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
		final ProgressBar progressBar = new ProgressBar(context);
		final ViewGroup layout = (ViewGroup) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
		layout.addView(progressBar);
		snackbar.show();
	}

	public static void showToast(final Context context, final String text) {
		final Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}

}