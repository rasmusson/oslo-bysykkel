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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import hotchemi.android.rate.AppRate;

public class Sykkelkoll extends ActionBarActivity {
	GoogleMap mMap;
	Activity activity;

	private GraphicsProvider graphicsProvider;
	private final Timer stopWatch = new Timer();
	private StationsOpenHelper stationsHelper;

	private Markers markers;

	private Tracker GATracker;
	private RelativeLayout findBikeButton;
	private RelativeLayout findLockButton;
	private WebOnSperateThreadLoader webOnSperateThreadLoader;

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuAbout:
			AboutDialog about = new AboutDialog(this);
			GATracker.sendEvent("Button actions", "aboutButton", "Open about dialog", null);
			about.setGoogleMapsAttribution(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(activity));
			about.show();
			return true;

		case R.id.menuFeedback:
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("text/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "rasmusson.stefan@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Tilbakemelding på Oslo Bysykkel app");
			Email.putExtra(Intent.EXTRA_TEXT, "" + "");
			startActivity(Intent.createChooser(Email, "Send tilbakemelding:"));
			return true;
			// case R.id.menuRefresh:
			// GATracker.sendEvent("Button actions", "updateButton",
			// "Update stations", null);
			// try {
			// updateMarkers(markers);
			// } catch (JSONException e) {
			// throw new RuntimeException(e);
			// }
			// return true;
		case R.id.menuPosition:

			Location findme = mMap.getMyLocation();
			if (findme != null) {
                double latitude = findme.getLatitude();
                double longitude = findme.getLongitude();
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            } else {
                Toast.makeText(this, "Kan ikke finne posisjon", Toast.LENGTH_LONG).show();
            }


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

        AppRate.setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(0) // default 10
                .monitor(this);
        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

		initDb();
		graphicsProvider = new DefaultGraphicsProvider(activity);

		initWebLoader();

		toastIfNoConnection();

		addFindBikesButtonListener();
		addFindLocksButtonListener();

		GATracker.sendTiming("Initial loading time", totalLoadingTimeTimer.getElapsedTime(), "totalLoadingTime", "Total loading time");

	}

	private void initWebLoader() {
		webOnSperateThreadLoader = new WebOnSperateThreadLoader(Sykkelkoll.this);
		getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<List<Station>>() {

			@Override
			public android.support.v4.content.Loader<List<Station>> onCreateLoader(int arg0, Bundle arg1) {
				return webOnSperateThreadLoader;
			}

			@Override
			public void onLoadFinished(android.support.v4.content.Loader<List<Station>> arg0, List<Station> stations) {

				createAndShowMarkers(stations);
			}

			@Override
			public void onLoaderReset(android.support.v4.content.Loader<List<Station>> arg0) {

			}
		});
	}

	private void toastIfNoConnection() {
		if (!haveNetworkConnection()) {
			Toast.makeText(this, "Ingen Internett-tilkobling", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

	}

	private void addFindBikesButtonListener() {
		findBikeButton = (RelativeLayout) findViewById(R.id.findBikeButton);
		findBikeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findBikeButton.setBackgroundColor(getResources().getColor(R.color.button_pressed));
				findLockButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_image_button_no_button_background));
                if (markers != null && mMap != null) {
                    markers.showFreeBikeMarkers(mMap);
                }
			}

		});

	}

	private void addFindLocksButtonListener() {
		findLockButton = (RelativeLayout) findViewById(R.id.findLockButton);
		findLockButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findLockButton.setBackgroundColor(getResources().getColor(R.color.button_pressed));
				findBikeButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_image_button_no_button_background));
                if (markers != null && mMap != null) {
                    markers.showFreeLockMarkers(mMap);
                }
			}

		});

	}

	private void createAndShowMarkers(List<Station> stations) {
		Log.d(getClass().getName(), "createMarkers for station count: " + (stations != null ? stations.size() : "0"));
		stopWatch.start();
		if (markers == null)
			markers = new Markers();
		else
			markers.clearOldMarkers();

		mMap.clear();

		for (Station station : stations) {

			MarkerOptions bikesMarker = createMarker(station, station.getBikesReady());
			markers.getFreeBikesMarkers().add(bikesMarker);
			if (markers.isShowingBikes())
				mMap.addMarker(bikesMarker);

			MarkerOptions locksMarker = createMarker(station, station.getLocksReady());
			markers.getFreeLocksMarkers().add(locksMarker);
			if (!markers.isShowingBikes())
				mMap.addMarker(locksMarker);
		}

		GATracker.sendTiming("Initial loading time", stopWatch.getElapsedTime(), "createMarkers", null);
	}

	private MarkerOptions createMarker(Station station, int pinNummer) {

		BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(graphicsProvider.getPinResource(pinNummer));
		MarkerOptions marker = new MarkerOptions().position(station.getLocation()).icon(icon);
		return marker;
	}

	private void updateMarkers(Markers markers) throws JSONException {
		stopWatch.start();
		toastIfNoConnection();
		markers.getFreeBikesMarkers().clear();
		markers.getFreeLocksMarkers().clear();

		List<Station> stations = null;// loadStations();
		for (Station station : stations) {
			MarkerOptions bikesMarker = createMarker(station, station.getBikesReady());
			markers.getFreeBikesMarkers().add(bikesMarker);

			MarkerOptions locksMarker = createMarker(station, station.getLocksReady());
			markers.getFreeLocksMarkers().add(locksMarker);
		}

		markers.reloadActiveMarkers(mMap);

		GATracker.sendTiming("General loading time", stopWatch.getElapsedTime(), "updateStations", "Loading time for update stations");
	}

	private void initDb() {
		stationsHelper = new StationsOpenHelper(this);
		stationsHelper.initDb();
	}

	@Override
	protected void onResume() {
		super.onResume();
		webOnSperateThreadLoader.forceLoad();

	}

	private void checkGoogleServicesStatus() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
		if (resultCode == ConnectionResult.SUCCESS) {
		} else if (resultCode == ConnectionResult.SERVICE_MISSING || resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
				|| resultCode == ConnectionResult.SERVICE_DISABLED) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 1);
			dialog.show();
		}

	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setZoomControlsEnabled(false);

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
			station.setLocation(new LatLng(c.getDouble(latitudeColumnIndex), c.getDouble(longitudeColumnIndex)));

			station.setDescription(c.getString(descriptionColumnIndex));
			station.setId(c.getInt(idColumnIndex));
			stationsMap.put(station.getId(), station);
			c.moveToNext();
		}
		c.close();
		return stationsMap;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stationsHelper.close();
	}
}
