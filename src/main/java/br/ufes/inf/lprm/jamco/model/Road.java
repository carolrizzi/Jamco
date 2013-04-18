package br.ufes.inf.lprm.jamco.model;

import br.ufes.inf.lprm.sinos.common.PublicSituationParticipant;

public class Road extends PublicSituationParticipant{
	
	private static final long serialVersionUID = 1L;

	private String address;
	private double minSpeed;
	
	public Road(String address, double minSpeed) {
		this.address = address;
		this.minSpeed = minSpeed;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}
	
}
