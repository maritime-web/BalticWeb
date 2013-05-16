package dk.dma.arcticweb.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class Stakeholder extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Set<User> users = new HashSet<User>();
	
	public Stakeholder() {
		
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	@Override
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(nullable = true, length = 128)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "stakeholder")
	public Set<User> getUsers() {
		return users;
	}
	
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	@Transient
	public String getStakeholderType() {
		return this.getClass().getSimpleName();
	}
	
	@Transient
	public boolean isShip() {
		return (this instanceof Ship);
	}
	
	@Transient
	public boolean isAuthority() {
		return (this instanceof Authority);
	}
	
	@Transient
	public boolean isShipOwner() {
		return (this instanceof ShipOwner);
	}
	
	@Transient
	public boolean isShoreStakeholder() {
		return (this instanceof ShoreStakeholder);
	}

}
