package com.amanoteam.unalix.services;

import android.util.Log;
import androidx.work.Worker;
import com.amanoteam.libunalix.exceptions.UnalixException;
import androidx.work.WorkerParameters;
import androidx.work.ListenableWorker.Result;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.content.Intent;
import java.io.File;
import androidx.appcompat.widget.AppCompatImageButton;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import android.view.Menu;
import androidx.core.view.MenuProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.lifecycle.Lifecycle;
import android.view.ViewParent;
import android.widget.CheckBox;
import androidx.appcompat.widget.AppCompatButton;
import android.content.DialogInterface;
import android.widget.ScrollView;
import com.amanoteam.unalix.fragments.IntentChooserDialogFragment;
import java.util.Random;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.databases.RulesetContract.RulesetEntry;
import com.amanoteam.unalix.databases.RulesetsDatabaseHelper;
import com.amanoteam.unalix.databinding.RulesetsFragmentBinding;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.content.SharedPreferences;
import com.amanoteam.unalix.activities.MainActivity;
import androidx.preference.PreferenceManager;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.libunalix.LibUnalix;
import com.amanoteam.unalix.core.Ruleset;
import com.amanoteam.unalix.utilities.RulesetsUtils;
import java.util.List;

public class RulesetsUpdateService extends Worker {
	public RulesetsUpdateService(
		Context context,
		WorkerParameters params) {
		super(context, params);
	}
   
	@Override
	public Result doWork() {
		final Context context = getApplicationContext();
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		//PackageUtils.showToast(context, "start");
		final List<Ruleset> rulesets = RulesetsUtils.getRulesetsFromDatabase(context);
		final int totalRulesets = rulesets.size();
		
		if (totalRulesets < 1) {
			return Result.success();
		}
		
		PackageUtils.createNotificationChannelIfNeeded(context);
		
		final LibUnalix unalix = new LibUnalix(context);
		
		final String notificationTitleFormat = "Checking updates for rulesets... (%d/%d)";
		
		final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, PackageUtils.DEFAULT_NOTIFICATION_CHANNEL)
			.setSmallIcon(R.drawable.refresh_icon)
			.setContentTitle(String.format(notificationTitleFormat, 0, totalRulesets))
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setProgress(totalRulesets, 0, false);
		
		final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.notify(PackageUtils.DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
		
		int current = 0;
		
		int totalUpdated = 0;
		int totalSkipped = 0;
		int totalErrors = 0;
		
		Log.i("UnalixRulesetsUpdate", String.format("There are %d rulesets in database, checking for updates", totalRulesets));
		
		for (final Ruleset ruleset : rulesets) {
			current++;
			
			notificationBuilder
				.setContentTitle(String.format(notificationTitleFormat, current, totalRulesets))
				.setProgress(totalRulesets, current, false);
			notificationManager.notify(PackageUtils.DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
			
			final String name = ruleset.getName();
			final String url = ruleset.getUrl();
			final String hashUrl = ruleset.getHashUrl();
			String filename = ruleset.getFilename();
			
			Log.i("UnalixRulesetsUpdate", String.format("Checking \"%s\" (%s)", name, url));
			
			if (!ruleset.isEnabled()) {
				Log.w("UnalixRulesetsUpdate", "This ruleset is disabled, skipping update check");
				totalSkipped++;
				
				continue;
			}
			
			if (filename == null) {
				final byte[] buffer = new byte[32];
				final Random random = new Random();
				random.nextBytes(buffer);
				
				final StringBuilder hex = new StringBuilder(2 * buffer.length);
				
				for (byte b : buffer) {
					hex.append(Integer.toHexString(0xff & b));
				}
				
				filename = context.getFilesDir() + File.separator + hex.toString() + ".json";
				
				final SQLiteDatabase database = new RulesetsDatabaseHelper(context)
					.getWritableDatabase();
				
				final ContentValues values = new ContentValues();
				
				values.put(RulesetEntry.COLUMN_RULESET_FILENAME, filename);
				
				final String whereClause = String.format("%s = ?", RulesetEntry.COLUMN_RULESET_URL);
				final String[] whereArgs = {
					url
				};
				
				database.update(
					RulesetEntry.TABLE_NAME,
					values,
					whereClause,
					whereArgs
				);
				
				database.close();
			}
			
			try {
				final boolean shouldUpdateRuleset = unalix.rulesetCheckUpdate(filename, url);
				
				if (!shouldUpdateRuleset) {
					Log.w("UnalixRulesetsUpdate", "The remote file for this ruleset was not modified since the last update, skipping it");
					totalSkipped++;
					
					continue;
				}
			} catch (final UnalixException e) {
				Log.e("UnalixRulesetsUpdate", "Cannot check for updates due to error", e);
				totalErrors++;
				
				continue;
			}
			
			Log.i("UnalixRulesetsUpdate", "Update available, fetching ruleset remote file");
			
			try {
				unalix.rulesetUpdate(filename, url, hashUrl, context.getCacheDir() + "/");
				
				Log.i("UnalixRulesetsUpdate", "Ruleset updated successfully");
				totalUpdated++;
			} catch (final UnalixException e) {
				Log.e("UnalixRulesetsUpdate", "Cannot update ruleset due to error", e);
				totalErrors++;
			}
		}
		
		Log.i("UnalixRulesetsUpdate", String.format("Update check finished: %d updated, %d skipped and %d errors", totalUpdated, totalSkipped, totalErrors));
		
		notificationBuilder
			.setSmallIcon(R.drawable.ic_launcher_foreground)
			.setContentTitle("Update check finished")
			.setContentText(String.format("%d updated, %d skipped and %d errors", totalUpdated, totalSkipped, totalErrors))
			.setProgress(0, 0, false);
		notificationManager.notify(PackageUtils.DEFAULT_NOTIFICATION_ID, notificationBuilder.build());
		
		return Result.success();
	}

}
