package com.amanoteam.unalix.services;

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
public class UnalixService extends Service {

	private ServiceHandler serviceHandler;

	@Override
	public void onCreate() {
		final HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		final Looper serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		final Message msg = serviceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		serviceHandler.sendMessage(msg);

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		System.exit(0);
	}

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(final Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(final Message msg) {
			final Intent intent = (Intent) msg.obj;
			final String action = intent.getStringExtra("originalAction");
			final String uglyUrl = intent.getStringExtra("uglyUrl");
			final String whatToDo = intent.getStringExtra("whatToDo");

			final Context context = getApplicationContext();
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

			final LibUnalix unalix = new LibUnalix(context);

			String cleanUrl;

			if (whatToDo.equals("cleanUrl")) {
				cleanUrl = unalix.cleanUrl(uglyUrl);
			} else {
				final int notificationId = PackageUtils.showNotification(context, "Unalix is running in background", "Resolving URL... please be patient");

				cleanUrl = unalix.unshortUrl(uglyUrl);

				PackageUtils.cancelNotification(context, notificationId);
			}

			final boolean preferNativeIntentChooser = preferences.getBoolean("preferNativeIntentChooser", false);
			
			if (preferNativeIntentChooser) {
				PackageUtils.createChooser(context, cleanUrl, Intent.ACTION_VIEW);
			} else {
				/*
				new Handler(Looper.getMainLooper()).post(() -> {
					PackageUtils.createChooserNewK(null, UnalixService.this, cleanUrl, Intent.ACTION_VIEW);
				});
				*/
			}

			stopSelf(msg.arg1);
		}
	}

}
