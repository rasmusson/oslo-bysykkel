/*  Copyright (C) 2010  Stefan Rasmusson

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    rasmusson.stefan@gmail.com
 */
package no.sr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SlidingDrawer;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class Sykkelkoll extends MapActivity {

	private static final String LOG_TAG_TIMING = "Timing";
	private static final String LOG_TAG_BUTTON = "Button";
	private static final String LOG_TAG_LIFECYCLE = "Lifecycle";
	private static final int NO_INTERNET_ACCESS_DIALOG = 1;
	private static final int LOADING_STATIONS_DIALOG = 2;

	private Boolean renderReadyBikes = true;
	private MapView mapView;
	private BikeOverlay bikeOverlay;
	private GraphicsProvider graphicsProvider;
	private MyLocationOverlay myLocOverlay;
	private final Timer stopWatch = new Timer();
	private StationsOpenHelper stationsHelper;

	private String[] freeBikesArray;

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, "Oppdater");
		menu.add(0, 2, 1, "Min posisjon");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			new RenderTask().execute();
			return true;

		case 2:
			mapView.getController().animateTo(myLocOverlay.getMyLocation());
			mapView.getController().setZoom(16);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isInternetEnabled() {
		NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NO_INTERNET_ACCESS_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Internett-tilkobling trengs for at bruke applik asjonen")
					.setCancelable(false).setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			Dialog dialog = builder.create();
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Sykkelkoll.this.finish();

				}
			});
			return dialog;
		case LOADING_STATIONS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Oppdaterer stasjoner");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			return progressDialog;
		default:
			dialog = null;
		}
		return null;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		stopWatch.start();
		Log.d(getClass().getSimpleName() + "-" + LOG_TAG_LIFECYCLE,
				"Application starts");
		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if (!isInternetEnabled()) {
			showDialog(NO_INTERNET_ACCESS_DIALOG);
		} else {
			Thread
					.setDefaultUncaughtExceptionHandler(new ExceptionHandler(
							this));
			String stackTrace = getStoredStackTrace();
			if (stackTrace != null) {
				Log.i(getClass().getSimpleName() + "-" + "mailStackTrace",
						"Found stacktrace file");
				mailErrorReport(stackTrace, "rasmusson.stefan@gmail.com");
			}

			graphicsProvider = new DefaultGraphicsProvider(this);
			stopWatch.stop();
			Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Startup "
					+ stopWatch.getElapsedTime());

			mapView = (MapView) findViewById(R.id.mapview);
			
						
			// Init DB
			stationsHelper = new StationsOpenHelper(this);
			try {
				stationsHelper.createDataBase();
			} catch (IOException e) {
				Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(),
						e);
			}

			try {
				stationsHelper.openDataBase();

			} catch (SQLException e) {
				Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(),
						e);
			}
			
			new RenderTask().execute();
			prepareOverlays();
			initMapView();
			initMyLocation();
			Button toggleButton = (Button) findViewById(R.id.toggleButton);
			toggleButton.setText("Vis lås");
			toggleButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.d(getClass().getSimpleName() + "-" + LOG_TAG_BUTTON,
							"Click toggle button");
					if (renderReadyBikes == false) {
						Log.d(
								getClass().getSimpleName() + "-"
										+ LOG_TAG_BUTTON,
								"Toggle button show bikes");
						renderReadyBikes = true;
						((Button) v).setText("Vis lås");
						showDialog(LOADING_STATIONS_DIALOG);
						renderPins();
						dismissDialog(LOADING_STATIONS_DIALOG);
					} else if (renderReadyBikes == true) {
						Log.d(
								getClass().getSimpleName() + "-"
										+ LOG_TAG_BUTTON,
								"Toggle button show slots");
						renderReadyBikes = false;
						((Button) v).setText("Vis sykkler");
						showDialog(LOADING_STATIONS_DIALOG);
						renderPins();
						dismissDialog(LOADING_STATIONS_DIALOG);
					}
				}
			});


		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
	}

	private class RenderTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(LOADING_STATIONS_DIALOG);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dismissDialog(Sykkelkoll.LOADING_STATIONS_DIALOG);
		}

		@Override
		protected Void doInBackground(Void... params) {
			updateStations();
			
			while(bikeOverlay.size() != freeBikesArray.length) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			render();
			return null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (myLocOverlay != null) {
			myLocOverlay.disableMyLocation();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (myLocOverlay != null) {
			myLocOverlay.enableMyLocation();
		}
	}

	public class StationsOpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 1;
		private static final String STATION_TABLE_NAME = "station";

		private static final String DB_PATH = "/data/data/no.sr/databases/";

		private static final String DB_NAME = "db";

		private SQLiteDatabase stationsDB;

		private Context context;

		StationsOpenHelper(Context context) {
			super(context, DB_NAME, null, 1);
			this.context = context;
		}

		public Cursor getStations() {
			return stationsDB.rawQuery("select * from station;", null);
		}

		public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			if (!dbExist) {
				Log.e("Sykkelkoll-" + "IO", "Creating DB");
				this.getReadableDatabase();

				try {

					copyDataBase();

				} catch (IOException e) {
					Log.e("Sykkelkoll-" + "IO", e.getMessage(),
							e);
				}
			} else {
				Log.e("Sykkelkoll-" + "IO", "else");
			}

		}

		private boolean checkDataBase() {

			SQLiteDatabase checkDB = null;

			try {
				String myPath = DB_PATH + DB_NAME;
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);

			} catch (SQLiteException e) {
				Log.e("Sykkelkoll-" + "IO", e.getMessage(), e);
			}

			if (checkDB != null) {

				checkDB.close();

			}
			Log.e("Sykkelkoll-" + "IO", "Creating DB");
			return checkDB != null ? true : false;
		}

		private void copyDataBase() throws IOException {

			InputStream myInput = context.getAssets().open(DB_NAME);

			String outFileName = DB_PATH + DB_NAME;

			OutputStream myOutput = new FileOutputStream(outFileName);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}

			myOutput.flush();
			myOutput.close();
			myInput.close();

		}

		public void openDataBase() throws SQLException {
			String myPath = DB_PATH + DB_NAME;
			stationsDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
		}

		@Override
		public synchronized void close() {

			if (stationsDB != null)
				stationsDB.close();

			super.close();

		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	private void initMapView() {
		stopWatch.start();
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(13);
		mapView.getController().animateTo(new GeoPoint(59920786, 10741711));
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING,
				"Init map view " + stopWatch.getElapsedTime());
	}

	private void mailErrorReport(final String stackTrace, final String recipient) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		String subject = "Error report";
		String body = stackTrace + "\n\n";

		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { recipient });
		sendIntent.putExtra(Intent.EXTRA_TEXT, body);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		sendIntent.setType("message/rfc822");

		Sykkelkoll.this.startActivity(Intent
				.createChooser(sendIntent, "Title:"));
		Sykkelkoll.this.deleteFile("stacktrace.log");
	}

	private String getStoredStackTrace() {
		StringBuilder storedStackTrace = new StringBuilder();
		try {
			String stackTraceLine;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					Sykkelkoll.this.openFileInput("stacktrace.log")));
			while ((stackTraceLine = reader.readLine()) != null) {
				storedStackTrace.append(stackTraceLine);
				storedStackTrace.append("\n");
			}
		} catch (FileNotFoundException fnfe) {
			return null;
		} catch (IOException ioe) {
			Log.e(getClass().getSimpleName() + "-" + "mailStackTrace", ioe
					.getMessage(), ioe);
		}
		return storedStackTrace.toString();
	}

	public void updateStations() {
		stopWatch.start();

		HttpGet httpGet = new HttpGet();
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
				10000);

		URI uri = null;

		freeBikesArray = null;
		while (freeBikesArray == null) {
			try {
				uri = new URI("http://bysykel.appspot.com/csv");
				httpGet.setURI(uri);

				HttpResponse httpResponse = httpClient.execute(httpGet);

				stopWatch.stop();
				Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING,
						"Download " + stopWatch.getElapsedTime());
				stopWatch.start();

				String content = streamToString(
						httpResponse.getEntity().getContent());
				freeBikesArray = content.split(",");
			} catch (URISyntaxException e) {
				Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(),
						e);
			} catch (ClientProtocolException e) {
				Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(),
						e);
			} catch (IOException e) {
				Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(),
						e);
			}
		}

	}

	private String streamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private void initMyLocation() {
		stopWatch.start();
		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		mapView.getOverlays().add(myLocOverlay);
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING,
				"Init my location " + stopWatch.getElapsedTime());

	}
	
	public void prepareOverlays() {
		stopWatch.start();
		Drawable drawable = this.getResources().getDrawable(R.drawable.m0);
		drawable.setBounds(-drawable.getIntrinsicWidth(), -drawable
				.getIntrinsicHeight(), 0, 0);

		bikeOverlay = new BikeOverlay(drawable, this);

		this.mapView.getController().setCenter(this.mapView.getMapCenter());

		Cursor c = stationsHelper.getStations();
		c.moveToFirst();
		int count = 0;
		int latitudeColumnIndex = c.getColumnIndex("latitude");
		int longitudeColumnIndex = c.getColumnIndex("longitude");
		int descriptionColumnIndex = c.getColumnIndex("description");
		int idColumnIndex = c.getColumnIndex("_id");
		int freeBikesColumnIndex = c.getColumnIndex("totalBikes");
        while (c.isAfterLast() == false && count != 10) {
        	
        	GeoPoint point;
			point = new GeoPoint(c.getInt(latitudeColumnIndex),
					c.getInt(longitudeColumnIndex));
			
			BikeOverlayItem overlayitem = new BikeOverlayItem(point, c.getString(descriptionColumnIndex), "");
			overlayitem.setId(c.getInt(idColumnIndex));
			overlayitem.setMaxBikes(c.getInt(freeBikesColumnIndex));
			overlayitem.setBikeMarker(graphicsProvider.getPinDrawable(0));
			bikeOverlay.addOverlay(overlayitem);

			
			c.moveToNext();
		}
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Prepare "
				+ stopWatch.getElapsedTime());
		
		
	} 

	public void render() {
		stopWatch.start();
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.remove(bikeOverlay);
		
		int i;
		for(i=0; i < bikeOverlay.size(); i++) {
			BikeOverlayItem bikeItem = bikeOverlay.getItem(i);
			
			int number;
			int freeBikes = Integer.parseInt(freeBikesArray[bikeItem.getId()]);
			if (freeBikes < 0) {
				number = 0;
			} else {
				if (renderReadyBikes) {
					number = freeBikes;
				} else {
					number = bikeItem.getMaxBikes() - freeBikes;
				}
			}
			
			
			Drawable bikePin = graphicsProvider.getPinDrawable(number);
			bikePin.setBounds(-bikePin.getIntrinsicWidth(), -bikePin
					.getIntrinsicHeight(), 0, 0);

			
			bikeItem.setBikeMarker(bikePin);
			
		}
		mapOverlays.add(bikeOverlay);
		mapView.getController().animateTo(mapView.getMapCenter());
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Render "
				+ stopWatch.getElapsedTime());
		
	}
	
	public void renderPins() {
		stopWatch.start();
		Drawable drawable = this.getResources().getDrawable(R.drawable.m0);
		drawable.setBounds(-drawable.getIntrinsicWidth(), -drawable
				.getIntrinsicHeight(), 0, 0);

		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.remove(bikeOverlay);
		bikeOverlay = new BikeOverlay(drawable, this);

		this.mapView.getController().setCenter(this.mapView.getMapCenter());

		Cursor c = stationsHelper.getStations();
		c.moveToFirst();
		int count = 0;
		int latitudeColumnIndex = c.getColumnIndex("latitude");
		int longitudeColumnIndex = c.getColumnIndex("longitude");
		int descriptionColumnIndex = c.getColumnIndex("description");
		int idColumnIndex = c.getInt(c.getColumnIndex("_id"));
		int freeBikesColumnIndex = c.getColumnIndex("totalBikes");
        while (c.isAfterLast() == false && count != 10) {
        	
        	GeoPoint point;
			point = new GeoPoint(c.getInt(latitudeColumnIndex),
					c.getInt(longitudeColumnIndex));
			
			BikeOverlayItem overlayitem = new BikeOverlayItem(point, c.getString(descriptionColumnIndex), "");
			int freeBikes = Integer.parseInt(freeBikesArray[c.getInt(idColumnIndex)]); 
			int number;
			if (freeBikes < 0) {
				number = 0;
			} else {
				if (renderReadyBikes) {
					number = freeBikes;
				} else {
					number = c.getInt(freeBikesColumnIndex) - freeBikes;
				}
			}
			
			Drawable bikePin = graphicsProvider.getPinDrawable(number);
			bikePin.setBounds(-bikePin.getIntrinsicWidth(), -bikePin
					.getIntrinsicHeight(), 0, 0);

			overlayitem.setBikeMarker(bikePin);
			bikeOverlay.addOverlay(overlayitem);

			
			c.moveToNext();
		}
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Render "
				+ stopWatch.getElapsedTime());
		mapOverlays.add(bikeOverlay);
		mapView.getController().animateTo(mapView.getMapCenter());
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}