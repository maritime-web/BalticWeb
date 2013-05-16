package dk.dma.arcticweb.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class ShipOwner extends Stakeholder {
	private static final long serialVersionUID = 1L;
	
	private Set<Ship> ships = new HashSet<Ship>();
	
	public ShipOwner() {
	}

	@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "owner")
	public Set<Ship> getShips() {
		return ships;
	}
	
	public void setShips(Set<Ship> ships) {
		this.ships = ships;
	}

}
