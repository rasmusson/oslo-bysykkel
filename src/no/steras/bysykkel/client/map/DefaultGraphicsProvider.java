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
package no.steras.bysykkel.client.map;

import no.steras.bysykkel.client.R;
import android.content.Context;

public class DefaultGraphicsProvider implements GraphicsProvider {

	private final Context context;

	public DefaultGraphicsProvider(final Context context) {
		this.context = context;
	}

	@Override
	public int getPinResource(final int pinNumber) {
		int pinResource;
		if (pinNumber == 0) {
			pinResource = R.drawable.m0;
		} else if (pinNumber == 1) {
			pinResource = R.drawable.m1;
		} else if (pinNumber == 2) {
			pinResource = R.drawable.m2;
		} else if (pinNumber == 3) {
			pinResource = R.drawable.m3;
		} else if (pinNumber == 4) {
			pinResource = R.drawable.m4;
		} else if (pinNumber == 5) {
			pinResource = R.drawable.m5;
		} else if (pinNumber == 6) {
			pinResource = R.drawable.m6;
		} else if (pinNumber == 7) {
			pinResource = R.drawable.m7;
		} else if (pinNumber == 8) {
			pinResource = R.drawable.m8;
		} else if (pinNumber == 9) {
			pinResource = R.drawable.m9;
		} else if (pinNumber == 10) {
			pinResource = R.drawable.m10;
		} else if (pinNumber == 11) {
			pinResource = R.drawable.m11;
		} else if (pinNumber == 12) {
			pinResource = R.drawable.m12;
		} else if (pinNumber == 13) {
			pinResource = R.drawable.m13;
		} else if (pinNumber == 14) {
			pinResource = R.drawable.m14;
		} else if (pinNumber == 15) {
			pinResource = R.drawable.m15;
		} else if (pinNumber == 16) {
			pinResource = R.drawable.m16;
		} else if (pinNumber == 17) {
			pinResource = R.drawable.m17;
		} else if (pinNumber == 18) {
			pinResource = R.drawable.m18;
		} else if (pinNumber == 19) {
			pinResource = R.drawable.m19;
		} else if (pinNumber == 20) {
			pinResource = R.drawable.m20;
		} else {
			pinResource = R.drawable.m20;
		}

		return pinResource;

	}

}
