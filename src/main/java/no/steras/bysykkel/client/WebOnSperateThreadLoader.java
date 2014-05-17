package no.steras.bysykkel.client;

import java.util.ArrayList;
import java.util.List;

import no.steras.bysykkel.client.data.Backend;
import no.steras.bysykkel.client.data.Station;
import no.steras.bysykkel.client.data.StationsOpenHelper;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.maps.model.LatLng;

public class WebOnSperateThreadLoader extends AsyncTaskLoader<List<Station>> {

	private Backend backend;
	private final Timer stopWatch;
	private StationsOpenHelper stationsHelper;
	private Tracker GATracker;

	public WebOnSperateThreadLoader(Context context) {
		super(context);
		backend = new Backend();
		stopWatch = new Timer();
		stationsHelper = new StationsOpenHelper(context);
		GATracker = EasyTracker.getTracker();
	}

	@Override
	public List<Station> loadInBackground() {
		try {
			return loadStations();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
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
		station.setLocation(new LatLng(c.getDouble(latitudeColumnIndex), c.getDouble(longitudeColumnIndex)));

		station.setDescription(c.getString(descriptionColumnIndex));
		station.setId(c.getInt(idColumnIndex));

		return station;
	}

}
