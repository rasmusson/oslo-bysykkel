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

public class StationSmall {
	private Integer id;
	private Integer bikesReady;
	private Integer emptyLocks;
	private Boolean online;

	public void setBikesReady(final Integer bikesReady) {
		this.bikesReady = bikesReady;
	}

	public Integer getBikesReady() {
		return bikesReady;
	}

	public void setEmptyLocks(final Integer emptyLocks) {
		this.emptyLocks = emptyLocks;
	}

	public Integer getEmptyLocks() {
		return emptyLocks;
	}

	public void setOnline(final Boolean online) {
		this.online = online;
	}

	public Boolean getOnline() {
		return online;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
