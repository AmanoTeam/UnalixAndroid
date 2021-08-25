package com.amanoteam.unalix;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import unalix.Unalix;

public class ClearURLActivity extends Activity {
	
	private static final Unalix unalix = new Unalix();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 	
		final Intent intent = getIntent();
		final String action = intent.getAction();
		final Intent sendIntent = new Intent();
		
		String actionName = "";
		
		if (action.equals(Intent.ACTION_SEND)) {
			final String uglyUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
			final String cleanedUrl = unalix.clearUrl(uglyUrl);
			
			actionName = "Share with";
			
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, cleanedUrl);
			sendIntent.setType("text/plain");
		} else if (action.equals(Intent.ACTION_VIEW)) {
			final Uri uglyUrl = intent.getData();
			final String cleanedUrl = unalix.clearUrl(uglyUrl.toString());
			
			actionName = "Open with";
			
			sendIntent.setAction(Intent.ACTION_VIEW);
			sendIntent.setData(Uri.parse(cleanedUrl));
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			final ArrayList<ComponentName> excludeTargets = new ArrayList<>();
			
			excludeTargets.add(new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.ClearURLActivity"));
			excludeTargets.add(new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.UnshortURLActivity"));
			
			final Intent chooserIntent = Intent.createChooser(sendIntent, actionName);
			chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeTargets.toArray(new ComponentName[0]));
			
			startActivity(chooserIntent);
		} else {
			final List<Intent> intentsList = new ArrayList<>();
			final List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(sendIntent, 0);
			
			for (ResolveInfo resolveInfoItem : resolveInfoList) {
				Intent targetIntent = (Intent) sendIntent.clone();
				
				String packageName = resolveInfoItem.activityInfo.packageName;
				String activityName = resolveInfoItem.activityInfo.name;
				
				if (activityName.equals("com.amanoteam.unalix.ClearURLActivity") || activityName.equals("com.amanoteam.unalix.UnshortURLActivity")) {
					continue;
				}
				
				targetIntent.setComponent(new ComponentName(packageName, activityName));
				intentsList.add(targetIntent);
			}

			final Intent chooserIntent = Intent.createChooser(intentsList.remove(0), actionName);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentsList.toArray(new Parcelable[intentsList.size()]));
			
			startActivity(chooserIntent);
		}
		
		finish();
	}

}