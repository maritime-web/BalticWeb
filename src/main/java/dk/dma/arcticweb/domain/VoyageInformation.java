package dk.dma.arcticweb.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class VoyageInformation extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	private Integer personsOnboard;
	private Boolean doctorOnboard;
	private Ship ship;
	
	public VoyageInformation() {
		
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	@Override
	public Integer getId() {
		return id;
	}
	
	@Column(unique = false, nullable = true)
	public Integer getPersonsOnboard() {
		return personsOnboard;
	}
	
	public void setPersonsOnboard(Integer personsOnboard) {
		this.personsOnboard = personsOnboard;
	}
	
	@Column(unique = false, nullable = true)
	public Boolean getDoctorOnboard() {
		return doctorOnboard;
	}
	
	public void setDoctorOnboard(Boolean doctorOnboard) {
		this.doctorOnboard = doctorOnboard;
	}
	
	@OneToOne(optional = false)
	public Ship getShip() {
		return ship;
	}
	
	public void setShip(Ship ship) {
		this.ship = ship;
	}
	
}
