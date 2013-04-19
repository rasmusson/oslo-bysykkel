package no.steras.bysykkel.client.data;

import no.steras.bysykkel.client.StationSmall;

import com.google.android.gms.maps.model.LatLng;

public class Station {

	private Integer id;
	private LatLng location;
	private String description;
	private Integer bikesReady = 0;
	private Integer locksReady = 0;
	private Boolean online = false;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public LatLng getLocation() {
		return location;
	}
	public void setLocation(LatLng location) {
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
	public Boolean getOnline() {
		return online;
	}
	public void setOnline(Boolean online) {
		this.online = online;
	}
}
