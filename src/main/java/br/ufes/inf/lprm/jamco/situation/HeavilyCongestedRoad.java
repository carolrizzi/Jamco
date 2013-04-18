package br.ufes.inf.lprm.jamco.situation;

import br.ufes.inf.lprm.jamco.model.Road;
import br.ufes.inf.lprm.scene.publishing.Publish;
import br.ufes.inf.lprm.sinos.common.PublicSituationType;
import br.ufes.inf.lprm.situation.Role;

@Publish(host = "localhost", port = 4000)
public class HeavilyCongestedRoad extends PublicSituationType {

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

