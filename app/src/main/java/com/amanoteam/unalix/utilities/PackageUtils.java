package com.amanoteam.unalix.utilities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PackageUtils {
	
	private static final String PACKAGE_NAME = "com.amanoteam.unalix";
	
	private static final String CLEAR_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "ClearURLActivity");
	private static final String UNSHORT_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "UnshortURLActivity");
	
	private static final ComponentName CLEAR_URL_COMPONENT = new ComponentName(PACKAGE_NAME, CLEAR_URL_ACTIVITY);
	private static final ComponentName UNSHORT_URL_COMPONENT = new ComponentName(PACKAGE_NAME, UNSHORT_URL_ACTIVITY);
	
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
		
		// Set package manager
		final PackageManager packageManager = context.getPackageManager();

		// Set exclude targets
		final ArrayList<ComponentName> excludeTargets = new ArrayList<>();
		
		excludeTargets.add(CLEAR_URL_COMPONENT);
		excludeTargets.add(UNSHORT_URL_COMPONENT);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			final Intent chooser = Intent.createChooser(intent, url);
			chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeTargets.toArray(new ComponentName[0]));
			
			return chooser;
		}
		
		final List<Intent> intents = new ArrayList<>();
		final List<ResolveInfo> intentTargets = packageManager.queryIntentActivities(intent, 0);

		for (ResolveInfo item : intentTargets) {
			Intent targetIntent = (Intent) intent.clone();

			String packageName = item.activityInfo.packageName;
			String activityName = item.activityInfo.name;

			if (activityName.equals(CLEAR_URL_ACTIVITY) || activityName.equals(UNSHORT_URL_ACTIVITY)) {
				continue;
			}

			targetIntent.setComponent(new ComponentName(packageName, activityName));
			intents.add(targetIntent);
		}

		final Intent chooser = Intent.createChooser(intents.remove(0), url);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
		
		return chooser;
		
	}
	
}