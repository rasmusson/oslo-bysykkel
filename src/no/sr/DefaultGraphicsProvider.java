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

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DefaultGraphicsProvider implements GraphicsProvider {

	private final Context context;

	public DefaultGraphicsProvider(final Context context) {
		this.context = context;
	}

	@Override
	public Drawable getPinDrawable(final int pinNumber) {
		Drawable alternatePin = null;
		if (pinNumber == 0) {
			alternatePin = context.getResources().getDrawable(R.drawable.m0);
		}
		else if (pinNumber == 1) {
			alternatePin = context.getResources().getDrawable(R.drawable.m1);
		}
		else if (pinNumber == 2) {
			alternatePin = context.getResources().getDrawable(R.drawable.m2);
		}
		else if (pinNumber == 3) {
			alternatePin = context.getResources().getDrawable(R.drawable.m3);
		}
		else if (pinNumber == 4) {
			alternatePin = context.getResources().getDrawable(R.drawable.m4);
		}
		else if (pinNumber == 5) {
			alternatePin = context.getResources().getDrawable(R.drawable.m5);
		}
		else if (pinNumber == 6) {
			alternatePin = context.getResources().getDrawable(R.drawable.m6);
		}
		else if (pinNumber == 7) {
			alternatePin = context.getResources().getDrawable(R.drawable.m7);
		}
		else if (pinNumber == 8) {
			alternatePin = context.getResources().getDrawable(R.drawable.m8);
		}
		else if (pinNumber == 9) {
			alternatePin = context.getResources().getDrawable(R.drawable.m9);
		}
		else if (pinNumber == 10) {
			alternatePin = context.getResources().getDrawable(R.drawable.m10);
		}
		else if (pinNumber == 11) {
			alternatePin = context.getResources().getDrawable(R.drawable.m11);
		}
		else if (pinNumber == 12) {
			alternatePin = context.getResources().getDrawable(R.drawable.m12);
		}
		else if (pinNumber == 13) {
			alternatePin = context.getResources().getDrawable(R.drawable.m13);
		}
		else if (pinNumber == 14) {
			alternatePin = context.getResources().getDrawable(R.drawable.m14);
		}
		else if (pinNumber == 15) {
			alternatePin = context.getResources().getDrawable(R.drawable.m15);
		}
		else if (pinNumber == 16) {
			alternatePin = context.getResources().getDrawable(R.drawable.m16);
		}
		else if (pinNumber == 17) {
			alternatePin = context.getResources().getDrawable(R.drawable.m17);
		}
		else if (pinNumber == 18) {
			alternatePin = context.getResources().getDrawable(R.drawable.m18);
		}
		else if (pinNumber == 19) {
			alternatePin = context.getResources().getDrawable(R.drawable.m19);
		}
		else if (pinNumber == 20) {
			alternatePin = context.getResources().getDrawable(R.drawable.m20);
		} else {
			alternatePin = context.getResources().getDrawable(R.drawable.m20);
		}

		return alternatePin;

	}

}
