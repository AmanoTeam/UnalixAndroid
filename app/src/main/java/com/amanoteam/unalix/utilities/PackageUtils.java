package com.amanoteam.unalix.utilities;

import android.view.WindowManager;
import com.amanoteam.unalix.activities.MainActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.graphics.drawable.Drawable;
import android.content.ClipData;
import android.content.ClipboardManager;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.amanoteam.unalix.utilities.PackageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.amanoteam.unalix.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PackageUtils {

	private static final String PACKAGE_NAME = "com.amanoteam.unalix";

	private static final String CLEAN_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.CleanURLActivity");
	public static final ComponentName CLEAN_URL_COMPONENT = new ComponentName(PACKAGE_NAME, CLEAN_URL_ACTIVITY);
	private static final String UNSHORT_URL_ACTIVITY = String.format("%s.%s", PACKAGE_NAME, "activities.UnshortURLActivity");
	public static final ComponentName UNSHORT_URL_COMPONENT = new ComponentName(PACKAGE_NAME, UNSHORT_URL_ACTIVITY);

	private static final ComponentName[] CHOOSER_EXCLUDE_COMPONENTS = {
		CLEAN_URL_COMPONENT,
		UNSHORT_URL_COMPONENT
	};

	private static final int DEFAULT_NOTIFICATION_ID = 1;
	private static final String DEFAULT_NOTIFICATION_CHANNEL = "unalix_notification_channel";
	private static final String DEFAULT_NOTIFICATION_CHANNEL_DESCRIPTION = "Default notification channel for Unalix";

	public static void createChooserNew(final Context context, final String url, final String action) {

		final Intent intent = new Intent(action)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		switch (action) {
			case Intent.ACTION_VIEW:
				intent.setData(Uri.parse(url));
				break;
			case Intent.ACTION_SEND:
				intent
					.putExtra(Intent.EXTRA_TEXT, url)
					.setType("text/plain");
				break;
			default:
				return;
		}
		
		//final Context context = activity.getApplicationContext();
		
		final class Item {
			private Drawable icon = null;
			private String label = null;
			private Intent intent = null;
			
			Item(
				final Drawable icon,
				final String label,
				final Intent intent
			) {
				this.icon = icon;
				this.label = label;
				this.intent = intent;
			}
			
			public Drawable getIcon() {
				return this.icon;
			}
			
			public String getLabel() {
				return this.label;
			}
			
			public Intent getIntent() {
				return this.intent;
			}
		}
		
		final PackageManager packageManager = context.getPackageManager();
		final List<ResolveInfo> targets = packageManager.queryIntentActivities(intent, 0);
		
		final ArrayList<Item> items = new ArrayList<Item>();
		
		for (final ResolveInfo target : targets) {
			final String packageName = target.activityInfo.packageName;
			final String activityName = target.activityInfo.name;

			if (activityName.equals(CLEAN_URL_ACTIVITY) || activityName.equals(UNSHORT_URL_ACTIVITY)) {
				continue;
			}

			final Intent targetIntent = (Intent) intent.clone();
			targetIntent.setComponent(new ComponentName(packageName, activityName));
			
			items.add(
				new Item(
					target.loadIcon(packageManager),
					(String) target.loadLabel(packageManager),
					targetIntent
				)
			);
		}

		final LayoutInflater layoutInflater = LayoutInflater.from(context);
		final View intentChooserDialog = layoutInflater.inflate(R.layout.intent_chooser, null);

		final RecyclerView recyclerView = intentChooserDialog.findViewById(R.id.intents_list);
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(linearLayoutManager);

		final AlertDialog alertDialog = new MaterialAlertDialogBuilder(context)
			.setView(intentChooserDialog)
			.setTitle(action.equals(Intent.ACTION_VIEW) ? "Open with" : "Share with")
			.setNegativeButton("Cancel", null)
			.create();
		
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
		
		final class ItemViewHolder extends ViewHolder {
			public AppCompatImageView icon = null;
			public MaterialTextView name = null;
			
			public ItemViewHolder(final View view) {
				super(view);
		
				icon = view.findViewById(R.id.activity_icon);
				name = view.findViewById(R.id.activity_name);
			}
		}
		
		final class ItemAdapter extends Adapter<ItemViewHolder> {
			private List<Item> items = null;
			
			public ItemAdapter(final List<Item> items) {
				this.items = items;
			}
		
			@Override
			public ItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
				final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activities_list_item, parent, false);
				final ItemViewHolder holder = new ItemViewHolder(view);
				
				view.setOnClickListener((final View itemView) -> {
					final Item item = items.get(holder.getAdapterPosition());
					final Context context = itemView.getContext();
					
					context.startActivity(item.getIntent());
					
					alertDialog.dismiss();
				});
				
				return holder;
			}
		
			@Override
			public void onBindViewHolder(final ItemViewHolder viewHolder, final int position) {
				final Item item = items.get(position);
				
				viewHolder.icon.setImageDrawable(item.getIcon());
				viewHolder.name.setText(item.getLabel());
			}
		
			@Override
			public int getItemCount() {
				return ((items != null) ? items.size() : 0);
			}
		}
		
		final ItemAdapter itemAdapter = new ItemAdapter(items);
		recyclerView.setAdapter(itemAdapter);

		alertDialog.show();
		
	}
	
	public static void createChooser(final Context context, final String url, final String action) {

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
				return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			final Intent chooser = Intent.createChooser(intent, url);
			chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, CHOOSER_EXCLUDE_COMPONENTS);
			chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			context.startActivity(chooser);
			
			return;
		}
			
		// Set package manager
		final PackageManager packageManager = context.getPackageManager();

		final List<Intent> intents = new ArrayList<>();
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
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[0]));
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(chooser);
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

	public static void showProgressSnackbar(final FragmentActivity activity, final View view, final String text) {
		final Snackbar snackbar = createSnackbar(view, text, Snackbar.LENGTH_INDEFINITE);

		final CircularProgressIndicator progressBar = new CircularProgressIndicator(activity);
		progressBar.setIndeterminate(true);
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

	public static void copyToClipboard(final Context context, final String text) {
		final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(null, text));
	}

	public static void cancelNotification(final Context context, final int id) {
		final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.cancel(id);
	}

	public static void hideKeyboard(final AppCompatActivity activity) {
		final View view = activity.getCurrentFocus();

		if (view == null) {
			return;
		}

		final IBinder windowToken = view.getWindowToken();

		final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
	}

	public static String getName(final TextInputEditText input) {
		final String name = input.getText().toString();

		input.setError(null);

		if (TextUtils.isEmpty(name)) {
			input.requestFocus();
			input.setError("Please enter a name");
			return null;
		}

		return name;
	}

	public static String getURL(final TextInputEditText input) {
		final String url = input.getText().toString();

		input.setError(null);

		if (TextUtils.isEmpty(url)) {
			input.requestFocus();
			input.setError("Please enter a URL");
			return null;
		}

		if (!(url.startsWith("http://") || url.startsWith("https://"))) {
			input.requestFocus();
			input.setError("Unrecognized URI or unsupported protocol");
			return null;
		}

		return url;
	}

	public static String sha256Digest(final byte[] data) {

		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (final GeneralSecurityException ignored) {
		}

		final byte[] digest = messageDigest.digest(data);

		final StringBuilder hexString = new StringBuilder(2 * digest.length);

		for (byte b : digest) {
			final String hex = Integer.toHexString(0xff & b);

			if (hex.length() == 1) {
				hexString.append('0');
			}

			hexString.append(hex);
		}

		return hexString.toString();
	}

	public static String generateFilename(final Context context) {
		final byte[] buffer = new byte[16];
		final Random random = new Random();
		random.nextBytes(buffer);

		final File file = new File(context.getFilesDir(), sha256Digest(buffer));

		return file.getAbsolutePath();
	}
}
