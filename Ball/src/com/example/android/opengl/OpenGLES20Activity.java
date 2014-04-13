/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

public class OpenGLES20Activity extends Activity implements SensorEventListener {

	private MyGLSurfaceView mGLView;
	private SensorManager sensorManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
		
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();

		Log.d("test", "staring sensor?");
		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	private float x;
	private float y;
	
	private float vx;
	private float vy;

	@Override
	public void onSensorChanged(SensorEvent event) {
		float dx = event.values[0] / 250;
		float dy = -event.values[1] / 250;
		
		if(mGLView.mRenderer.mSquare == null) return; // FIXME: not yet initialized!
		
		int w = mGLView.getWidth();
		int h = mGLView.getHeight();
		int m = Math.max(w, h);
		
		float maxh = (float)w / m - 0.1f;
		float maxv = (float)h / m - 0.1f;
		
		vx += dx;
		vy += dy;
		
		if (Math.abs(x + vx) > maxh) {
			vx = -vx;
		}
		if (Math.abs(y + vy) > maxv) {
			vy = -vy;
		}
		
		vx *= .8;
		vy *= .8;
		
		x += vx;
    	y += vy;
    	
		mGLView.mRenderer.mSquare.translate(x, y);
	}
}