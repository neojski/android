package com.example.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MonitorService extends Service {
	private Map<URL, Long> lastModified;
	
	private final String filename = "modified_data"; 
	
	@Override
	public void onCreate() {
		// XXX: no easier way than to read it on create?
		File file = new File(getFilesDir(), filename);
		ObjectInputStream objectInputStream;
		try {
			objectInputStream = new ObjectInputStream(new FileInputStream(file));
			lastModified = (Map<URL, Long>) objectInputStream.readObject();
		} catch (Exception e) {
			Log.d("error", "can't load hashmap");
			lastModified = new HashMap<URL, Long>();
		}
	}
	
	synchronized private void saveData() {
		try {
			File file = new File(getFilesDir(), filename);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					new FileOutputStream(file));
			objectOutputStream.writeObject(lastModified);
		} catch (IOException e) {
			// yeah, die freely!
			e.printStackTrace();
		}
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
		mNotificationManager.notify(uri.hashCode(), mBuilder.build());

		uri.hashCode();
	}

	private void checkUpdates() {
		for (final String url : URLListHolder.getList(this)) {
			// XXX: new thread for every list?
			new Thread(new Runnable() {
				@Override
				public void run() {
					checkUpdate(url);
				}
			}).start();
		}
	}

	synchronized private void checkUpdate(String urlstr) {
		try {
			URL url = new URL(urlstr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			long modified = 0;
			if (lastModified.containsKey(url)) {
				modified = lastModified.get(url);
			}
			con.setIfModifiedSince(modified);

			if (con.getResponseCode() == 304) {
				// not modified
			}
			if (con.getResponseCode() == 200) {
				// modified and ok
				notify(Uri.parse(urlstr));
				lastModified.put(url, con.getLastModified());
				saveData();
			}

			Log.d("data", con.getLastModified() + " " + con.getResponseCode());
		} catch (MalformedURLException e) {
			// who cares, yeah
			Log.d("update", "malformed url", e);
		} catch (IOException e) {
			// who care 2
			Log.d("update", "connection problem", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		checkUpdates();

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
