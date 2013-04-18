package br.ufes.inf.lprm.jamco.situation;

import br.ufes.inf.lprm.jamco.model.Bus;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

public class StuckBus extends SituationType{

	private static final long serialVersionUID = 1L;

	@Role(label = "$bus")
	private Bus bus;

	public Bus getBus() {
		return bus;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
}
