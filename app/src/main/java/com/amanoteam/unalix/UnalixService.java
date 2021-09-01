package com.amanoteam.unalix;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import unalix.Unalix;

public class UnalixService extends Service {
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	private PackageManager packageManager;
	
	private final ArrayList<ComponentName> excludeTargets = new ArrayList<>();
	
	private final ComponentName clearUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.ClearURLActivity");
	private final ComponentName unshortUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.UnshortURLActivity");
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
			public ServiceHandler(final Looper looper) {
					super(looper);
			}
			@Override
			public void handleMessage(final Message msg) {
					
					String cleanedUrl = null;
					
					final Intent intent = (Intent) msg.obj;
					final String action = intent.getStringExtra("originalAction");
					final String uglyUrl = intent.getStringExtra("uglyUrl");
					final String whatToDo = intent.getStringExtra("whatToDo");
					final Intent sendIntent = new Intent();
					
					final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					
					final boolean ignoreReferralMarketing = settings.getBoolean("ignoreReferralMarketing", false);
					final boolean ignoreRules = settings.getBoolean("ignoreRules", false);
					final boolean ignoreExceptions = settings.getBoolean("ignoreExceptions", false);
					final boolean ignoreRawRules = settings.getBoolean("ignoreRawRules", false);
					final boolean ignoreRedirections = settings.getBoolean("ignoreRedirections", false);
					final boolean skipBlocked = settings.getBoolean("skipBlocked", false);
					final boolean stripDuplicates = settings.getBoolean("stripDuplicates", false);
					final boolean stripEmpty = settings.getBoolean("stripEmpty", false);
					
					final Unalix unalix = new Unalix();
					
					unalix.setIgnoreReferralMarketing(ignoreReferralMarketing);
					unalix.setIgnoreRules(ignoreRules);
					unalix.setIgnoreExceptions(ignoreExceptions);
					unalix.setRawRules(ignoreRawRules);
					unalix.setIgnoreRedirections(ignoreRedirections);
					unalix.setSkipBlocked(skipBlocked);
					unalix.setStripDuplicates(stripDuplicates);
					unalix.setStripEmpty(stripEmpty);
					
					if (whatToDo.equals("clearUrl")) {
						cleanedUrl = unalix.clearUrl(uglyUrl);
					} else {
						final int maxRedirects = Integer.valueOf(settings.getString("maxRedirects", "13"));
						final int connectTimeout = Integer.valueOf(settings.getString("connectTimeout", "3000"));
						final int readTimeout = Integer.valueOf(settings.getString("readTimeout", "3000"));
						final int readChunkSize = Integer.valueOf(settings.getString("readChunkSize", "1024"));
						final String dohUrl = settings.getString("dohUrl", "https://cloudflare-dns.com/dns-query");
						final String dohAddress = settings.getString("dohAddress", "1.1.1.1");
						final int dohPort = Integer.valueOf(settings.getString("dohPort", "443"));
						final String userAgent = settings.getString("userAgent", "UnalixAndroid/0.1 (+https://github.com/AmanoTeam/UnalixAndroid)");

						unalix.setMaxRedirects(maxRedirects);
						unalix.setConnectTimeout(connectTimeout);
						unalix.setReadTimeout(readTimeout);
						unalix.setReadChunkSize(readChunkSize);
						unalix.setDohUrl(dohUrl);
						unalix.setDohAddress(dohAddress);
						unalix.setDohPort(dohPort);
						unalix.setUserAgent(userAgent);
						
						cleanedUrl = unalix.unshortUrl(uglyUrl);
					}
					
					String actionName = null;
					
					if (action.equals(Intent.ACTION_SEND)) {
						actionName = "Share with";
						
						sendIntent.setAction(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_TEXT, cleanedUrl);
						sendIntent.setType("text/plain");
					} else if (action.equals(Intent.ACTION_VIEW)) {
						actionName = "Open with";
						
						sendIntent.setAction(Intent.ACTION_VIEW);
						sendIntent.setData(Uri.parse(cleanedUrl));
					}
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						final Intent chooserIntent = Intent.createChooser(sendIntent, actionName);
						chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeTargets.toArray(new ComponentName[0]));
						startActivity(chooserIntent);
					} else {
						final List<Intent> intentsList = new ArrayList<>();
						final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
						
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
		// Set package manager
		packageManager = getPackageManager();
		
		// Set exclude targets
		excludeTargets.add(clearUrlActivity);
		excludeTargets.add(unshortUrlActivity);
		
		final HandlerThread thread = new HandlerThread("ServiceStartArguments",
						Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
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
		System.exit(0);
	}


}