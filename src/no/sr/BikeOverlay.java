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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;

public class BikeOverlay extends ItemizedOverlay<BikeOverlayItem> {

	private ArrayList<BikeOverlayItem> mOverlays = new ArrayList<BikeOverlayItem>();

	public BikeOverlay(final Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public BikeOverlay(final Drawable defaultMarker, final Context context) {
		super(defaultMarker);
	}

	public void addOverlay(final BikeOverlayItem overlay) {
		boundCenterBottom(overlay.getBikeMarker());
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected BikeOverlayItem createItem(final int i) {
		return mOverlays.get(i);
	}
	
	@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub
		return super.onTap(index);
	}
}
