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
import no.steras.bysykkel.client.dialog.AboutDialog;
import no.steras.bysykkel.client.map.DefaultGraphicsProvider;
import no.steras.bysykkel.client.map.GraphicsProvider;
import no.steras.bysykkel.client.map.Markers;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Sykkelkoll extends FragmentActivity {
	GoogleMap mMap;
	Activity activity;
	Marker myLocationMarker;

	private GraphicsProvider graphicsProvider;
	private final Timer stopWatch = new Timer();
	private StationsOpenHelper stationsHelper;

	private Markers markers;
	private Backend backend;

	private static final int MENU_ITEM_UPDATE = 1;
	private static final int MENU_ITEM_ABOUT = 2;
	private static final int MENU_ITEM_FEEDBACK = 3;
	
	private Tracker GATracker;
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_UPDATE, 1, "Oppdater");
		menu.add(0, MENU_ITEM_ABOUT, 1, "Om appen");
		menu.add(0, MENU_ITEM_FEEDBACK, 1, "Gi tilbakemelding");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_UPDATE:
			try {
				GATracker.sendEvent("Button actions", "updateButton", "Update stations", null);
				updateMarkers(markers);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return true;

		case MENU_ITEM_ABOUT:
			AboutDialog about = new AboutDialog(this);
			GATracker.sendEvent("Button actions", "aboutButton", "Open about dialog", null);
			about.setGoogleMapsAttribution(GooglePlayServicesUtil
					.getOpenSourceSoftwareLicenseInfo(activity));
			about.show();
			return true;

		case MENU_ITEM_FEEDBACK:
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL,
					new String[] { "rasmusson.stefan@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT,
					"Tilbakemelding på Oslo Bysykkel app");
			Email.putExtra(Intent.EXTRA_TEXT, "" + "");
			startActivity(Intent.createChooser(Email, "Send tilbakemelding:"));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		Timer totalLoadingTimeTimer = new Timer();
		totalLoadingTimeTimer.start();
		
		EasyTracker.getInstance().setContext(this);
		GATracker = EasyTracker.getTracker();
		
		setContentView(R.layout.main);

		setUpMapIfNeeded();
		initMyLocation();

		initDb();
		graphicsProvider = new DefaultGraphicsProvider(activity);

		backend = new Backend();

		toastIfNoConnection();

		List<Station> stations = null;
		try {
			stations = loadStations();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		markers = createMarkers(stations);

		addButtonListener();
		
		GATracker.sendTiming("Initial loading time", totalLoadingTimeTimer.getElapsedTime(), "totalLoadingTime", "Total loading time");

	}

	private void toastIfNoConnection() {
		if (!haveNetworkConnection()) {
			Toast.makeText(this, "Ingen Internett-tilkobling",
					Toast.LENGTH_LONG).show();
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
		stopWatch.start();
		Markers markers = new Markers();

		for (Station station : stations) {

			MarkerOptions bikesMarker = createMarker(station,
					station.getBikesReady());
			markers.getFreeBikesMarkers().add(bikesMarker);
			mMap.addMarker(bikesMarker);

			MarkerOptions locksMarker = createMarker(station,
					station.getLocksReady());
			markers.getFreeLocksMarkers().add(locksMarker);
		}

		markers.setShowingBikes(true);
		GATracker.sendTiming("Initial loading time", stopWatch.getElapsedTime(), "createMarkers", null);
		return markers;
	}

	private MarkerOptions createMarker(Station station, int pinNummer) {
		BitmapDescriptor icon = BitmapDescriptorFactory
				.fromResource(graphicsProvider.getPinResource(pinNummer));
		MarkerOptions marker = new MarkerOptions().position(
				station.getLocation()).icon(icon);

		return marker;
	}

	private void updateMarkers(Markers markers) throws JSONException {
		stopWatch.start();
		toastIfNoConnection();
		markers.getFreeBikesMarkers().clear();
		markers.getFreeLocksMarkers().clear();

		List<Station> stations = loadStations();
		for (Station station : stations) {
			MarkerOptions bikesMarker = createMarker(station,
					station.getBikesReady());
			markers.getFreeBikesMarkers().add(bikesMarker);

			MarkerOptions locksMarker = createMarker(station,
					station.getLocksReady());
			markers.getFreeLocksMarkers().add(locksMarker);
		}

		markers.reloadActiveMarkers(mMap);
		
		GATracker.sendTiming("General loading time", stopWatch.getElapsedTime(), "updateStations", "Loading time for update stations");
	}

	private List<Station> loadStations() throws JSONException {
		stopWatch.start();
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
		
		GATracker.sendTiming("Initial loading time", stopWatch.getElapsedTime(), "loadStations", null);
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
			Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(), e);
		}

		try {
			stationsHelper.openDataBase();

		} catch (SQLException e) {
			Log.e(getClass().getSimpleName() + "-" + "IO", e.getMessage(), e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkGoogleServicesStatus();
	}

	private void checkGoogleServicesStatus() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity.getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS) {
		} else if (resultCode == ConnectionResult.SERVICE_MISSING
				|| resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
				|| resultCode == ConnectionResult.SERVICE_DISABLED) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					activity, 1);
			dialog.show();
		}

	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
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
