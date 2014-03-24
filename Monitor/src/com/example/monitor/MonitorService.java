package com.example.monitor;

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
	Map<Uri, String> map = new HashMap<Uri, String>();
	Map<Uri, Integer> idMap = new HashMap<Uri, Integer>();
	int count = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		addUri("http://google.com");
	}

	private void addUri(String url) {
		Uri uri = Uri.parse(url);
		map.put(uri, "lol");
		idMap.put(uri, ++count);
	}

	private void removeUri(String url) {
		Uri uri = Uri.parse(url);
		map.remove(uri);
		idMap.remove(uri);
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
		mNotificationManager.notify(idMap.get(uri), mBuilder.build());
	}

	private void doTheJob(Intent intent) {
		String url = intent.getStringExtra("add");
		if (url != null) {
			addUri(url);
			return;
		}
		url = intent.getStringExtra("remove");
		if (url != null) {
			removeUri(url);
			return;
		}
		for (Uri uri : map.keySet()) {
			notify(uri);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		doTheJob(intent);

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
