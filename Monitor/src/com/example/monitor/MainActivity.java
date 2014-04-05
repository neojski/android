package com.example.monitor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	private ArrayAdapter<String> adapter;
	private ArrayList<String> adapterList;
	
	// List singleton
	private List<String> getList() {
		try {
			return URLListHolder.getList(this);
		} catch (Exception e) {
			Log.e("list", "can't load list");
			return null;
		}
	}

	private void addURL() {
		EditText editText = (EditText) findViewById(R.id.editText1);
		String url = editText.getText().toString();
		adapter.add(url);
		getList().add(url);
		editText.setText("");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// initialize with some if-modified-since compatible sites
		if (getList().size() == 0) {
			getList().add("http://w3.org");
			getList().add("http://nodejs.org");
			getList().add("http://bbc.co.uk");
			getList().add("http://google.com");
		}

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		adapterList = new ArrayList<String>(getList());
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, adapterList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				getList().remove(position);
				adapterList.remove(position);
				adapter.notifyDataSetChanged();
				
				return true;
			}
		});

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addURL();
			}
		});
		
		Button checkNowButton = (Button) findViewById(R.id.button2);
		final MainActivity mainActivity = this;
		checkNowButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startService(new Intent(mainActivity, MonitorService.class));
			}
		});

		// start service every 10 seconds
		Intent intent = new Intent(this, MonitorService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				10 * 1000, pintent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		URLListHolder.saveList(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			return rootView;
		}
	}

}
