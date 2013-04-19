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
package no.steras.bysykkel.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.steras.bysykkel.client.data.Backend;
import no.steras.bysykkel.client.data.Station;
import no.steras.bysykkel.client.data.StationsOpenHelper;
import no.steras.bysykkel.client.map.DefaultGraphicsProvider;
import no.steras.bysykkel.client.map.GraphicsProvider;
import no.steras.bysykkel.client.map.Markers;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/*
import java.io.BufferedReader;

import java.io.File;
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
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.steras.bysykkel.client.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

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
	private Map<Integer, Station> globalStationsMap;

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
			if (myLocOverlay.getMyLocation() != null) {
				mapView.getController().animateTo(myLocOverlay.getMyLocation());
				mapView.getController().setZoom(16);
			} else {
				CharSequence text = "Kan ikke fastslå position";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(this, text, duration);
				toast.show();
			}
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
					"Internett-tilkobling trengs for at bruke applikasjonen")
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
						render();
						dismissDialog(LOADING_STATIONS_DIALOG);
					} else if (renderReadyBikes == true) {
						Log.d(
								getClass().getSimpleName() + "-"
										+ LOG_TAG_BUTTON,
								"Toggle button show slots");
						renderReadyBikes = false;
						((Button) v).setText("Vis sykkler");
						showDialog(LOADING_STATIONS_DIALOG);
						render();
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
			globalStationsMap = getStationsFromDataBase();
			populateStationsWithBackendData(globalStationsMap);
			createOverlays();
			updateOverlays();
			render();
			return null;
		}

		private void updateOverlays() {
			for (int i = 0; i < bikeOverlay.size(); i++) {
				BikeOverlayItem overlayItem = bikeOverlay.getItem(i);
				overlayItem.setStation(globalStationsMap.get(overlayItem
						.getStation().getId()));
			}
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
		checkGoogleServicesStatus();
		if (myLocOverlay != null) {
			myLocOverlay.enableMyLocation();
		}
	}

	private void checkGoogleServicesStatus() {
		
	}

	public class StationsOpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 1;
		private static final String STATION_TABLE_NAME = "station";

		private static final String DB_PATH = "/data/data/no.sr/databases/";

		private static final String DB_NAME = "db";

		private SQLiteDatabase stationsDB;

		private Context context;

		StationsOpenHelper(Context context) {
			super(context, DB_NAME, null, 3);
			this.context = context;
		}

		public Cursor getStations() {
			return stationsDB.rawQuery("select * from station;", null);
		}

		public void addStation(Station newStation) {
			ContentValues params = new ContentValues();

			params.put("latitude", newStation.getLocation().getLatitudeE6());
			params.put("longitude", newStation.getLocation().getLongitudeE6());
			params.put("description", newStation.getDescription());
			params.put("_id", newStation.getId());
			stationsDB.insert("station", null, params);
		}

		public void createDataBase() throws IOException {

			boolean dbExist = checkDataBase();

			if (!dbExist) {
				Log.e("Sykkelkoll-" + "IO", "Creating DB");
				this.getReadableDatabase();

				try {

					copyDataBase();

				} catch (IOException e) {
					Log.e("Sykkelkoll-" + "IO", e.getMessage(), e);
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
						SQLiteDatabase.OPEN_READWRITE);

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
					SQLiteDatabase.OPEN_READWRITE);
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
			if (oldVersion < newVersion) {
			String myPath = DB_PATH + DB_NAME;
			new File(myPath).delete();
			try {
				copyDataBase();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
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

	private List<StationSmall> toStationSmallList(InputStream content)
			throws JSONException, IOException {
		List<StationSmall> stationSmallList = new ArrayList<StationSmall>();
		JSONArray jsonStationsArray = new JSONArray(streamToString(content));
		for (int i = 0; i < jsonStationsArray.length(); i++) {
			JSONObject jsonStation = jsonStationsArray.getJSONObject(i);
			StationSmall stationSmall = new StationSmall();
			stationSmall.setId(jsonStation.getInt("id"));
			stationSmall.setBikesReady(jsonStation.getInt("bikesReady"));
			stationSmall.setEmptyLocks(jsonStation.getInt("emptyLocks"));
			stationSmall.setOnline(jsonStation.getBoolean("online"));
			stationSmallList.add(stationSmall);
		}

		return stationSmallList;
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

	public void populateStationsWithBackendData(
			Map<Integer, Station> stationsMap) {
		stopWatch.start();

		HttpGet httpGet = new HttpGet();
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
				10000);

		URI uri = null;

		try {
			uri = new URI("http://bysykel-4.appspot.com/json2");
			httpGet.setURI(uri);

			HttpResponse httpResponse = httpClient.execute(httpGet);

			stopWatch.stop();
			Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING,
					"Download " + stopWatch.getElapsedTime());
			stopWatch.start();

			List<StationSmall> stationSmallList = null;
			try {
				stationSmallList = toStationSmallList(httpResponse.getEntity()
						.getContent());
			} catch (IllegalStateException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			} catch (JSONException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}

			stopWatch.stop();
			Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Parsing "
					+ stopWatch.getElapsedTime());

			//Remove stations that is no longer in the system
			Map<Integer, Station> cleanedStationsMap = new HashMap<Integer, Station>();
			for (StationSmall stationSmall : stationSmallList) {
				if (stationsMap.containsKey(stationSmall.getId())) {
					Station populatedStation = stationsMap.get(stationSmall.getId());
					populatedStation.populateFromStationSmall(stationSmall);
					cleanedStationsMap.put(stationSmall.getId(), populatedStation);
				
			}
					
			stationsMap.clear();
			stationsMap.putAll(cleanedStationsMap);
				
				

			}
		} catch (URISyntaxException e) {
			Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(), e);
		} catch (ClientProtocolException e) {
			Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(), e);
		} catch (IOException e) {
			Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(), e);
		}
	}

	private Station downloadNewStationInfo(Integer id, HttpClient httpClient) {
		String url = "http://www.adshel.no/js/getracknr.php?id=" + id;

		HttpGet httpGet = new HttpGet();
		Station station = null;
		try {
			httpGet.setURI(new URI(url));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			station = parseXMLStation(httpResponse.getEntity().getContent());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return station;

	}

	private Station parseXMLStation(InputStream content) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder;
		Station s = null;
		try {
			builder = builderFactory.newDocumentBuilder();

			Document doc = builder.parse(content);
			s = new Station();
			Integer longitude = new Double(Double.parseDouble(doc
					.getElementsByTagName("longitute").item(0).getChildNodes()
					.item(0).getNodeValue()) * 1000000).intValue();

			Integer latitude = new Double(Double.parseDouble(doc
					.getElementsByTagName("latitude").item(0).getChildNodes()
					.item(0).getNodeValue()) * 1000000).intValue();

			s.setLocation(new GeoPoint(latitude, longitude));
			String descripion[] = doc.getElementsByTagName("description").item(
					0).getChildNodes().item(0).getNodeValue().split("-");
			if (descripion.length == 2) {
				s.setDescription(descripion[1]);
			} else if (descripion.length == 1) {
				s.setDescription(descripion[0]);
			} else if (descripion.length > 2) {
				descripion[0] = "";
				s.setDescription(arrayToString(descripion, "").trim());
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;

	}

	public static String arrayToString(String[] a, String separator) {
		StringBuffer result = new StringBuffer();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	public Map<Integer, Station> getStationsFromDataBase() {

		Map<Integer, Station> stationsMap = new HashMap<Integer, Station>();
		Cursor c = stationsHelper.getStations();
		c.moveToFirst();
		int count = 0;
		int latitudeColumnIndex = c.getColumnIndex("latitude");
		int longitudeColumnIndex = c.getColumnIndex("longitude");
		int descriptionColumnIndex = c.getColumnIndex("description");
		int idColumnIndex = c.getColumnIndex("_id");
		while (c.isAfterLast() == false && count != 10) {

			Station station = new Station();
			station.setLocation(new GeoPoint(c.getInt(latitudeColumnIndex), c
					.getInt(longitudeColumnIndex)));

			station.setDescription(c.getString(descriptionColumnIndex));
			station.setId(c.getInt(idColumnIndex));
			stationsMap.put(station.getId(), station);
			c.moveToNext();
		}
		c.close();
		return stationsMap;
	}

	public void createOverlays() {
		stopWatch.start();
		Drawable drawable = graphicsProvider.getPinDrawable(0);
		drawable.setBounds(-drawable.getIntrinsicWidth(), -drawable
				.getIntrinsicHeight(), 0, 0);

		bikeOverlay = new BikeOverlay(drawable, this);

		this.mapView.getController().setCenter(this.mapView.getMapCenter());

		for (Integer stationId : globalStationsMap.keySet()) {
			if (globalStationsMap.get(stationId) != null) {
				BikeOverlayItem overlayitem = new BikeOverlayItem(
						globalStationsMap.get(stationId));
				overlayitem.setBikeMarker(graphicsProvider.getPinDrawable(0));
				bikeOverlay.addOverlay(overlayitem);
			}
		}
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING,
				"Create overlays " + stopWatch.getElapsedTime());

	}

	public void render() {
		stopWatch.start();
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.remove(bikeOverlay);

		int i;
		for (i = 0; i < bikeOverlay.size(); i++) {
			BikeOverlayItem bikeItem = bikeOverlay.getItem(i);

			Drawable bikePin;
			if (renderReadyBikes) {
				bikePin = graphicsProvider.getPinDrawable(bikeItem.getStation()
						.getBikesReady());
				bikePin.setBounds(-bikePin.getIntrinsicWidth(), -bikePin
						.getIntrinsicHeight(), 0, 0);
			} else {
				bikePin = graphicsProvider.getPinDrawable(bikeItem.getStation()
						.getLocksReady());
				bikePin.setBounds(-bikePin.getIntrinsicWidth(), -bikePin
						.getIntrinsicHeight(), 0, 0);
			}

			bikeItem.setBikeMarker(bikePin);
		}
		mapOverlays.add(bikeOverlay);

		mapView.getController().animateTo(mapView.getMapCenter());
		stopWatch.stop();
		Log.i(getClass().getSimpleName() + "-" + LOG_TAG_TIMING, "Render "
				+ stopWatch.getElapsedTime());

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}*/

public class Sykkelkoll extends FragmentActivity {
        GoogleMap mMap;
        Activity activity;
        Marker myLocationMarker;
        
        private GraphicsProvider graphicsProvider;
        private final Timer stopWatch = new Timer();
        private StationsOpenHelper stationsHelper;
        
        private Markers markers;
        private Backend backend;
        
        @Override
    	public boolean onCreateOptionsMenu(final Menu menu) {
    		super.onCreateOptionsMenu(menu);
    		menu.add(0, 1, 1, "Oppdater");
    		menu.add(0, 2, 1, "Om Oslo Bysykkel");

    		return true;
    	}

    	@Override
    	public boolean onOptionsItemSelected(final MenuItem item) {
    		switch (item.getItemId()) {
    		case 1:
    			try {
    				updateMarkers(markers);
				} catch (JSONException e) {
					e.printStackTrace();
				}
    			
    			return true;

    		case 2:
    			//about
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    		}
    	}

        
        
        public void onCreate(final Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                
                activity = this;
                setContentView(R.layout.main);
                
                setUpMapIfNeeded();
	            initMyLocation();                        
	            
	            initDb();
				graphicsProvider = new DefaultGraphicsProvider(activity);
	            
				backend = new Backend();
				
				if(!haveNetworkConnection()){
				    Toast.makeText(this,"No internet connection",Toast.LENGTH_LONG).show();
				}
				
				List<Station> stations = null;
				try {
					stations = loadStations();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				markers = createMarkers(stations);

				addButtonListener();
				
                new Handler(Looper.getMainLooper()).post(new RenderThread());
                                
        }
        class RenderThread implements Runnable {
			public void run() {
				
				
				
				
				
			}
		}
        
        @Override
        protected void onResumeFragments() {
        	super.onResumeFragments();
        	
        	
        	
        }

		private void addButtonListener() {
			Button toggleButton = (Button) findViewById(R.id.toggleButton);
            toggleButton.setText("Vis lås");
            toggleButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                            if (markers.isShowingBikes()) {   
                                    ((Button) v).setText("Vis sykkler");
                                    
                                    markers.showFreeLockMarkers(mMap);
                                    
                            } else if (!markers.isShowingBikes()) {
                                    ((Button) v).setText("Vis lås");
                                    
                                    markers.showFreeBikeMarkers(mMap);
                            }
                    }

					
            });
			
		}

		
		
		private Markers createMarkers(List<Station> stations) {
			Markers markers = new Markers();
			
			for (Station station : stations) {
				
				MarkerOptions bikesMarker = createMarker(station, station.getBikesReady());         
				markers.getFreeBikesMarkers().add(bikesMarker);
				mMap.addMarker(bikesMarker);
			
				MarkerOptions locksMarker = createMarker(station, station.getLocksReady()); 
				markers.getFreeLocksMarkers().add(locksMarker);				
			}
			
			markers.setShowingBikes(true);
			return markers;
		}
		
		private MarkerOptions createMarker(Station station, int pinNummer) {
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(graphicsProvider.getPinResource(pinNummer));
			MarkerOptions marker = new MarkerOptions()
                                   .position(station.getLocation())
								   .icon(icon);
                                    
            return marker;
		}
		
		private void updateMarkers(Markers markers) throws JSONException {
			
			markers.getFreeBikesMarkers().clear();
			markers.getFreeLocksMarkers().clear();
			
			List<Station> stations = loadStations();
			for (Station station : stations) {
				MarkerOptions bikesMarker = createMarker(station, station.getBikesReady());         
				markers.getFreeBikesMarkers().add(bikesMarker);
			
				MarkerOptions locksMarker = createMarker(station, station.getLocksReady()); 
				markers.getFreeLocksMarkers().add(locksMarker);	
			}
			
			markers.reloadActiveMarkers(mMap);
		}
		
		private List<Station> loadStations() throws JSONException {
			 List<Station> stations = new ArrayList<Station>();
             
			 
			 backend.loadData();
				
				Cursor c = stationsHelper.getStations();
                c.moveToFirst();
                while (c.isAfterLast() == false) {
						Station station = createStationFromDBData(c);
                        
						backend.populateWithBackEndData(station);
						
						stations.add(station);
                        c.moveToNext();
                }
                c.close();
                return stations;
			
			
		}
		
		
		

		
		
		
		private Station createStationFromDBData(Cursor c) {
			int latitudeColumnIndex = c.getColumnIndex("latitude");
            int longitudeColumnIndex = c.getColumnIndex("longitude");
            int descriptionColumnIndex = c.getColumnIndex("description");
            int idColumnIndex = c.getColumnIndex("_id");
			
			Station station = new Station();
            station.setLocation(new LatLng(c.getDouble(latitudeColumnIndex), c
                                        .getDouble(longitudeColumnIndex)));

            station.setDescription(c.getString(descriptionColumnIndex));
                        station.setId(c.getInt(idColumnIndex));
             
			 return station;
		}
      

        
        private void initDb() {
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
        }
        
        @Override
        protected void onResume() {
                super.onResume();
                checkGoogleServicesStatus();
        }
        
        private void checkGoogleServicesStatus() {
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
                if (resultCode == ConnectionResult.SUCCESS) {
                } else if (resultCode == ConnectionResult.SERVICE_MISSING ||
                           resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                           resultCode == ConnectionResult.SERVICE_DISABLED) {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 1);
                    dialog.show();
                }
                
        }

        private void setUpMapIfNeeded() {
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                                    .getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    // The Map is verified. It is now safe to manipulate the map.

                }
            }
        }
        
        private boolean haveNetworkConnection() {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
            return haveConnectedWifi || haveConnectedMobile;
        }
        
        
        private void initMyLocation() {
                stopWatch.start();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                
        }
        
        
     
        
        public Map<Integer, Station> getStationsFromDataBase() {

                Map<Integer, Station> stationsMap = new HashMap<Integer, Station>();
                Cursor c = stationsHelper.getStations();
                c.moveToFirst();
                int latitudeColumnIndex = c.getColumnIndex("latitude");
                int longitudeColumnIndex = c.getColumnIndex("longitude");
                int descriptionColumnIndex = c.getColumnIndex("description");
                int idColumnIndex = c.getColumnIndex("_id");
                while (c.isAfterLast() == false) {

                        Station station = new Station();
                        station.setLocation(new LatLng(c.getDouble(latitudeColumnIndex), c
                                        .getDouble(longitudeColumnIndex)));

                        station.setDescription(c.getString(descriptionColumnIndex));
                        station.setId(c.getInt(idColumnIndex));
                        stationsMap.put(station.getId(), station);
                        c.moveToNext();
                }
                c.close();
                return stationsMap;
        }
}






