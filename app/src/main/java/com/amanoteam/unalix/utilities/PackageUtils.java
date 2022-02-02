package com.amanoteam.unalix.utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;

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
			chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, CHOOSER_EXCLUDE_COMPONENTS);

			return chooser;
		}

		// Set package manager
		final PackageManager packageManager = context.getPackageManager();

		final List<Intent> intents = new ArrayList<>();
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

		return chooser;

	}

}