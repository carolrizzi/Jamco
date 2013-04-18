package br.ufes.inf.lprm.jamco.situation;

import br.ufes.inf.lprm.jamco.model.Road;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

public class FlowingRoad extends SituationType {

	private static final long serialVersionUID = 1L;

	@Role(label = "$road")
	private Road road;

	public Road getRoad() {
		return road;
	}

	public void setRoad(Road road) {
		this.road = road;
	}
	
}

