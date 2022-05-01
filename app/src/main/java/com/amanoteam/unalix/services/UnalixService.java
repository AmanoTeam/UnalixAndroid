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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.os.Build;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.utilities.PackageUtils;
import com.amanoteam.unalix.wrappers.Unalix;


public class UnalixService extends Service {

	private Looper serviceLooper;
	private ServiceHandler serviceHandler;

	@Override
	public void onCreate() {
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
		msg.obj = (Intent) intent;
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

			// libunalix stuff
			final Unalix unalix = new Unalix();
			unalix.setFromPreferences(context);

			String cleanUrl = null;
			
			if (whatToDo.equals("clearUrl")) {
				cleanUrl = unalix.clearUrl(uglyUrl);
			} else {
				final String notification_channel = "unalix_notification_channel";
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					final NotificationChannel channel = new NotificationChannel(notification_channel, "Unalix", NotificationManager.IMPORTANCE_DEFAULT);
					channel.setDescription("Default notification channel for Unalix");
					
					final NotificationManager notificationManager = getSystemService(NotificationManager.class);
					notificationManager.createNotificationChannel(channel);
				}

				final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notification_channel)
					.setSmallIcon(R.drawable.baseline_cleaning_services_24)
					.setContentTitle("Unalix is running in background")
					.setContentText("Resolving URL... please be patient")
					.setPriority(NotificationCompat.PRIORITY_LOW);
				
				final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
				
				final int notificationId = 1;
				notificationManager.notify(notificationId, builder.build());

				cleanUrl = unalix.unshortUrl(uglyUrl);
				
				notificationManager.cancel(notificationId);
			}

			final Intent chooser = PackageUtils.createChooser(context, cleanUrl, action);

			startActivity(chooser);

			stopSelf(msg.arg1);
		}
	}

}