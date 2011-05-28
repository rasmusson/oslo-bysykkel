package no.sr;

import com.google.android.maps.GeoPoint;

public class Station {

	private Integer id;
	private GeoPoint location;
	private String description;
	private Integer bikesReady;
	private Integer locksReady;
	private Boolean onlone;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public GeoPoint getLocation() {
		return location;
	}
	public void setLocation(GeoPoint location) {
		this.location = location;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getBikesReady() {
		return bikesReady;
	}
	public void setBikesReady(Integer bikesReady) {
		this.bikesReady = bikesReady;
	}
	public Integer getLocksReady() {
		return locksReady;
	}
	public void setLocksReady(Integer locksReady) {
		this.locksReady = locksReady;
	}
	public Boolean getOnlone() {
		return onlone;
	}
	public void setOnlone(Boolean onlone) {
		this.onlone = onlone;
	}
	
	public void populateFromStationSmall(StationSmall stationSmall) {
		setBikesReady(stationSmall.getBikesReady());
		setLocksReady(stationSmall.getEmptyLocks());
		setOnlone(stationSmall.getOnline());
		setId(stationSmall.getId());
	}
}
