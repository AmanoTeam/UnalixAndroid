package com.amanoteam.unalix.utilities;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.material.snackbar.Snackbar;

import com.amanoteam.unalix.R;

public class PackageUtils {

	private static final String PACKAGE_NAME = "com.amanoteam.unalix";

	private static final String CLEAN_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.CleanURLActivity");
	private static final String UNSHORT_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.UnshortURLActivity");

	public static final ComponentName CLEAN_URL_COMPONENT = new ComponentName(PACKAGE_NAME, CLEAN_URL_ACTIVITY);
	public static final ComponentName UNSHORT_URL_COMPONENT = new ComponentName(PACKAGE_NAME, UNSHORT_URL_ACTIVITY);

	private static final ComponentName[] CHOOSER_EXCLUDE_COMPONENTS = {
		CLEAN_URL_COMPONENT,
		UNSHORT_URL_COMPONENT
	};
	
	private static final int DEFAULT_NOTIFICATION_ID = 1;
	private static final String DEFAULT_NOTIFICATION_CHANNEL = "unalix_notification_channel";
	private static final String DEFAULT_NOTIFICATION_CHANNEL_DESCRIPTION = "Default notification channel for Unalix";
	
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

			if (activityName.equals(CLEAN_URL_ACTIVITY) || activityName.equals(UNSHORT_URL_ACTIVITY)) {
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
	
	public static Snackbar createSnackbar(final View view, final String text, final int duration) {
		final Snackbar snackbar = Snackbar.make(view, text, duration);
		final View snackbarView = snackbar.getView();
		
		final LayoutParams params = (LayoutParams) snackbarView.getLayoutParams();
		params.setAnchorId(R.id.bottom_navigation);
		params.gravity = Gravity.TOP;
		params.anchorGravity = Gravity.TOP;
		
		snackbarView.setLayoutParams(params);
		
		return snackbar;
	}
	
	public static void showSnackbar(final View view, final String text) {
		final Snackbar snackbar = createSnackbar(view, text, Snackbar.LENGTH_SHORT);
		snackbar.show();
	}
	
	public static void showProgressSnackbar(final Context context, final View view, final String text) {
		final Snackbar snackbar = createSnackbar(view, text, Snackbar.LENGTH_INDEFINITE);
		
		final ProgressBar progressBar = new ProgressBar(context);
		final ViewGroup layout = (ViewGroup) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
		layout.addView(progressBar);
		
		snackbar.show();
	}
	
	public static void showToast(final Context context, final String text) {
		final Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public static int showNotification(final Context context, final String title, final String description) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			final NotificationChannel channel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, "Unalix", NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription(DEFAULT_NOTIFICATION_CHANNEL_DESCRIPTION);
			
			final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
			.setSmallIcon(R.drawable.cleaning_icon)
			.setContentTitle(title)
			.setContentText(description)
			.setPriority(NotificationCompat.PRIORITY_LOW);
		
		final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		
		notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
		
		return DEFAULT_NOTIFICATION_ID;
	}
	
	public static void cancelNotification(final Context context, final int id) {
		final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.cancel(id);
	}
	
}