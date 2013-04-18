package br.ufes.inf.lprm.jamco.model;

import br.ufes.inf.lprm.sinos.common.PublicSituationParticipant;

public class Bus extends PublicSituationParticipant {

	private static final long serialVersionUID = 1L;

	private String id;
	private double speed;
	private String address;
	
	public Bus (String id) {
		this(0, null, id);
	}
	
	public Bus(double speed, String address, String id){
		this.speed = speed;
		this.address = address; 
		this.id = id;
	}

	public String getId(){
		return id;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	
}
