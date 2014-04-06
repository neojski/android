package com.example.board;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {

	// private final String stateURL =
	// "http://grzegorz_gutowski.staff.tcs.uj.edu.pl/board/state/";
	private final String stateURL = "http://192.168.1.26:8000/state.xml";

	DrawingView dv;
	private Paint mPaint;

	private InputStream downloadUrl(String urlString) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(stateURL);
		HttpResponse resp = client.execute(get);
		return resp.getEntity().getContent();

		/*
		 * HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 * conn.setReadTimeout(10000); conn.setConnectTimeout(15000);
		 * conn.setRequestMethod("GET"); conn.setDoInput(true); conn.connect();
		 * InputStream stream = conn.getInputStream(); return stream;
		 */
	}

	private class DownloadBoardState extends AsyncTask<String, Void, Document> {

		@Override
		protected Document doInBackground(String... params) {
			try {
				InputStream stream = downloadUrl(stateURL);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				return dBuilder.parse(stream);
			} catch (IOException e) {
				Log.e("download", "io", e);
			} catch (ParserConfigurationException e) {
				Log.e("download", "parse", e);
			} catch (SAXException e) {
				Log.e("download", "parse", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Document doc) {
			NodeList paths = doc.getElementsByTagName("path");
			for (int i = 0; i < paths.getLength(); i++) {
				Element path = (Element) paths.item(i);
				String color = path.getAttribute("color");

				Paint paint = getPaint(Color.parseColor(color));

				NodeList points = path.getElementsByTagName("point");
				for (int j = 0; j < points.getLength() - 1; j++) {
					Element point1 = (Element) points.item(j);
					Element point2 = (Element) points.item(j + 1);

					canvas.drawLine(
							Float.parseFloat(point1.getAttribute("x")) * width,
							Float.parseFloat(point1.getAttribute("y")) * height,
							Float.parseFloat(point2.getAttribute("x")) * width,
							Float.parseFloat(point2.getAttribute("y")) * height,
							paint);
				}
			}
			scheduleUpdate();
		}
	}
	
	private void scheduleUpdate() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				new DownloadBoardState().execute(stateURL);	
			};
		}, 1000);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dv = new DrawingView(this);
		setContentView(dv);
		mPaint = getPaint(Color.GREEN);

		TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mngr.getDeviceId();
		
		scheduleUpdate();
	}

	private Bitmap mBitmap;
	private Canvas canvas;
	int width;
	int height;

	private Paint getPaint(int color) {
		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(color);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);
		return mPaint;
	}

	public class DrawingView extends View {

		private Path mPath;
		private Paint circlePaint;
		private Path circlePath;

		public DrawingView(Context c) {
			super(c);
			mPath = new Path();
			circlePaint = new Paint();
			circlePath = new Path();
			circlePaint.setAntiAlias(true);
			circlePaint.setColor(Color.BLUE);
			circlePaint.setStyle(Paint.Style.STROKE);
			circlePaint.setStrokeJoin(Paint.Join.MITER);
			circlePaint.setStrokeWidth(4f);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);

			width = w;
			height = h;

			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.drawBitmap(mBitmap, 0, 0, null);

			canvas.drawPath(mPath, mPaint);

			canvas.drawPath(circlePath, circlePaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 100;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;

				circlePath.reset();
				circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			circlePath.reset();
			// commit the path to our offscreen
			canvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
	}
}