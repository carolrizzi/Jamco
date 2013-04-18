package br.ufes.inf.lprm.jamco.model;

import java.io.Serializable;

public class Location implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private double latitude;
	private double longitude;
	
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public boolean equals(Object obj) {
		Location location = (Location) obj;
		if(this.latitude == location.getLatitude() && this.longitude == location.getLongitude())
			return true;
		return false;
	}
	
}
