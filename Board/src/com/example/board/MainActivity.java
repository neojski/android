package com.example.board;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
	private final String stateURL = "http://grzegorzgutowski.staff.tcs.uj.edu.pl/board/state/";
	private final String uploadURL = "http://grzegorzgutowski.staff.tcs.uj.edu.pl/board/newpath/";

	private InputStream downloadUrl(String urlString) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(stateURL);
		HttpResponse resp = client.execute(get);
		return resp.getEntity().getContent();
	}

	private class DownloadBoardState extends AsyncTask<String, Void, Document> {
		Set<List<Point>> linesUploadedBeforeStart = new HashSet<List<Point>>();

		@Override
		protected Document doInBackground(String... params) {
			linesUploadedBeforeStart.addAll(uploadedLines);

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
			canvas.drawColor(Color.parseColor("#ffffff"));

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
			removeUploadedLines(linesUploadedBeforeStart);
			dv.invalidate();
			scheduleUpdate(1000);
		}

	}

	private void scheduleUpdate(int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				new DownloadBoardState().execute(stateURL);
			};
		}, delay);
	}

	private String buildUploadXML(List<Point> polyline) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version='1.0'?><newpath userId='");
		sb.append(deviceId);
		sb.append("'>");

		for (Point point : polyline) {
			sb.append(point.toString());
		}

		sb.append("</newpath>");
		return sb.toString();
	}

	private class UploadBoardState extends
			AsyncTask<List<Point>, Void, Boolean> {
		List<Point> polyline;

		@Override
		protected Boolean doInBackground(List<Point>... params) {
			polyline = params[0];
			Log.d("upload", "background");
			try {
				// chunk polyline to 50 points (server restriction)
				for (int i = 0; i < polyline.size(); i += 50) {
					List<Point> chunk = polyline.subList(i,
							Math.min(i + 50, polyline.size()));

					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(uploadURL);
					StringEntity stringEntity = null;
					stringEntity = new StringEntity(buildUploadXML(chunk));
					post.setEntity(stringEntity);

					HttpResponse resp;

					resp = client.execute(post);

					InputStream content = resp.getEntity().getContent();

					Scanner scanner = new Scanner(content);
					String result = "";
					while (scanner.hasNextLine()) {
						result += scanner.nextLine();
					}
					Log.d("upload", result);
				}
			} catch (IOException e) {
				Log.e("upload", "error", e);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				lineUploaded(polyline);
			}
		}
	}

	public void uploadPoints(final List<Point> polyline) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				// XXX: thank you java for being crap with generics!
				new UploadBoardState().execute(polyline);
			}
		});
	}

	private String deviceId;
	private DrawingView dv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dv = new DrawingView(this);
		setContentView(dv);

		TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = mngr.getDeviceId();

		scheduleUpdate(0);
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

	private class Point {
		public float x;
		public float y;

		public Point(float _x, float _y) {
			x = _x;
			y = _y;
		}

		@Override
		public String toString() {
			return "<point x='" + x + "' y='" + y + "'/>";
		}
	}

	public void lineUploaded(List<Point> polyline) {
		uploadedLines.add(polyline);
	}

	private void removeUploadedLines(Set<List<Point>> linesUploadedBeforeStart) {
		cachedLines.removeAll(linesUploadedBeforeStart);
		uploadedLines.removeAll(linesUploadedBeforeStart);
	}

	private Set<List<Point>> cachedLines = new HashSet<List<Point>>();
	private Set<List<Point>> uploadedLines = new HashSet<List<Point>>();

	public class DrawingView extends View {

		private Path mPath;
		private Paint circlePaint;
		private Path circlePath;
		private List<Point> currentPolyline;

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

			canvas.drawPath(mPath, getPaint(Color.RED));

			canvas.drawPath(circlePath, circlePaint);

			// draw all not-yet-uploaded polylines
			for (List<Point> polyline : cachedLines) {
				for (int i = 0; i < polyline.size() - 1; i++) {
					Point p1 = polyline.get(i);
					Point p2 = polyline.get(i + 1);

					canvas.drawLine(p1.x * width, p1.y * height, p2.x * width,
							p2.y * height, getPaint(Color.BLUE));
				}
			}
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 5;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;

			currentPolyline = new LinkedList<Point>();
			currentPolyline.add(new Point(x / width, y / height));
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

				currentPolyline.add(new Point(x / width, y / height));
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			circlePath.reset();
			// kill this so we don't double draw
			mPath.reset();

			uploadPoints(currentPolyline);
			cachedLines.add(currentPolyline);
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