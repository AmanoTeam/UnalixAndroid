package com.amanoteam.unalix;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.widget.Toast;

import java.lang.InterruptedException;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;

import unalix.Unalix;

public class UnalixService extends Service {
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
			public ServiceHandler(Looper looper) {
					super(looper);
			}
			@Override
			public void handleMessage(Message msg) {
					
					final Intent intent = (Intent) msg.obj;
					final String action = intent.getStringExtra("originalAction");
					final String uglyUrl = intent.getStringExtra("uglyUrl");
					final Intent sendIntent = new Intent();
					
					final Unalix unalix = new Unalix();
					
					String actionName = "";
					
					if (action.equals(Intent.ACTION_SEND)) {
						final String cleanedUrl = unalix.aunshortUrl(uglyUrl);
						
						actionName = "Share with";
						
						sendIntent.setAction(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_TEXT, cleanedUrl);
						sendIntent.setType("text/plain");
					} else if (action.equals(Intent.ACTION_VIEW)) {
						final String cleanedUrl = unalix.aunshortUrl(uglyUrl);
						
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
					
					stopSelf(msg.arg1);
			}
	}

	@Override
	public void onCreate() {
		final HandlerThread thread = new HandlerThread("ServiceStartArguments",
						Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
			Toast.makeText(this, "unshort url", Toast.LENGTH_SHORT).show();

			Message msg = serviceHandler.obtainMessage();
			msg.arg1 = startId;
			msg.obj = (Object) intent;
			serviceHandler.sendMessage(msg);
			
			return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
			return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "unshort url done", Toast.LENGTH_SHORT).show();
		
		// https://stackoverflow.com/a/4379822
		Process.killProcess(Process.myPid());
	}


}