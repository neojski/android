package com.example.whocares.counter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	Timer timer;
	long startTime;
	long resumeTime;
	long previousForegroundTime;
	
	private long getTime() {
		return System.currentTimeMillis();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		previousForegroundTime += getTime() - resumeTime;
	}
	
	private void updateStuff() {
		long currentTime = getTime();
		
		TextView totalTimeView = (TextView) findViewById(R.id.total_time);
		totalTimeView.setText("total: " + (currentTime - startTime));
		
		long currentForegroundTime = currentTime - resumeTime;
		TextView foregroundTimeView = (TextView) findViewById(R.id.foreground_time);
		foregroundTimeView.setText("current foreground: " + currentForegroundTime);
		
		TextView currentforegroundTimeView = (TextView) findViewById(R.id.current_foreground_time);
		currentforegroundTimeView.setText("total foreground: " + (currentForegroundTime + previousForegroundTime));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		resumeTime = getTime();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateStuff();
					}
				});
			}
		}, 0, 1000);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startTime = getTime();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
