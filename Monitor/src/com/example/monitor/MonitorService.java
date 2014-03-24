package com.example.monitor;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class MonitorService extends Service {
	Map<URL, String> map = new HashMap<URL, String>();
	private final int UPDATED_NOTIFICATION = 0;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private void notify(Uri uri) {
		// will open browser
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(uri);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Monitor update")
				.setContentText(uri.toString())
				.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(UPDATED_NOTIFICATION, mBuilder.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notify(Uri.parse("http://google.com"));

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
