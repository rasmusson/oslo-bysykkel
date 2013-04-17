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

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BikeOverlayItem extends OverlayItem {

	private Drawable bikeMarker;
	private Drawable placeMarker;
	private Station station;
	
	public Drawable getBikeMarker() {
		return bikeMarker;
	}

	public void setBikeMarker(final Drawable bikeMarker) {
		this.bikeMarker = bikeMarker;
	}

	public Drawable getPlaceMarker() {
		return placeMarker;
	}

	public void setPlaceMarker(final Drawable placeMarker) {
		this.placeMarker = placeMarker;
	}

	public BikeOverlayItem(Station station) {
		super(station.getLocation(), station.getDescription(), null);
		this.station = station;
	}

	@Override
	public Drawable getMarker(final int stateBitset) {
		return bikeMarker;
	}
	
	public Station getStation() {
		return station;
	}
	
	public void setStation(Station station) {
		this.station = station;
	}
}
