package no.steras.bysykkel.client.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

public class Markers {
	private List<MarkerOptions> freeBikesMarkerList = new ArrayList<MarkerOptions>();
	private List<MarkerOptions> freeLocksMarkerList = new ArrayList<MarkerOptions>();

	private Boolean showingBikes;

	public List<MarkerOptions> getFreeBikesMarkers() {
		return freeBikesMarkerList;
	}

	public List<MarkerOptions> getFreeLocksMarkers() {
		return freeLocksMarkerList;
	}

	public void showFreeLockMarkers(GoogleMap map) {
		reloadMarkers(getFreeLocksMarkers(), map);

		showingBikes = false;
	}

	public void showFreeBikeMarkers(GoogleMap map) {
		reloadMarkers(getFreeBikesMarkers(), map);

		showingBikes = true;
	}

	public void reloadActiveMarkers(GoogleMap map) {
		reloadMarkers(getActiveMarkers(), map);
		showingBikes = true;
	}

	private void reloadMarkers(List<MarkerOptions> markers, GoogleMap map) {
		map.clear();

		for (MarkerOptions marker : markers) {
			map.addMarker(marker);
		}
	}

	public List<MarkerOptions> getActiveMarkers() {
		if (showingBikes) {
			return freeBikesMarkerList;
		} else {
			return freeLocksMarkerList;
		}
	}

	public Boolean isShowingBikes() {
		return showingBikes;
	}

	public void setShowingBikes(Boolean showingBikes) {
		this.showingBikes = showingBikes;
	}
}
