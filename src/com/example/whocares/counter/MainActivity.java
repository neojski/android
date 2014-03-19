package com.example.whocares.counter;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	Timer foregroundTimer = new Timer();
	Timer totalTimer = new Timer();
	
	@Override
	protected void onPause() {
		super.onPause();
		foregroundTimer.cancel();
	}
	
	int foreground;
	int total;
	
	@Override
	protected void onResume() {
		super.onResume();
		foregroundTimer = new Timer();
		foregroundTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView = (TextView) findViewById(R.id.foreground_time);
						textView.setText("" + ++foreground);
					}
				});
			}
		}, 0, 1000);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		totalTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						TextView textView = (TextView) findViewById(R.id.total_time);
						textView.setText("" + ++total);
					}
				});
				
			}
		}, 0, 1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
