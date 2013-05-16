package dk.dma.arcticweb.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

@Entity
public class Ship extends Stakeholder {
	private static final long serialVersionUID = 1L;
	
	private Long mmsi;
	private Long imoNo;
	private String callsign;
	private String type;
	private Integer maxSpeed;
	private Integer tonnage;	
	private String commCapabilities;
	private Integer rescueCapacity;
	private Integer width;
	private Integer length;
	private String iceClass;
	private Boolean helipad;
	private List<ShipReport> reports;
	private VoyageInformation voyageInformation;	
	private ShipOwner owner;
	
	public Ship() {
	}
	
	@Column(nullable = true)
	public Long getMmsi() {
		return mmsi;
	}
	
	public void setMmsi(Long mmsi) {
		this.mmsi = mmsi;
	}
	
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
	public ShipOwner getOwner() {
		return owner;
	}
	
	public void setOwner(ShipOwner owner) {
		this.owner = owner;
	}

	@Column(nullable = true)
	public Long getImoNo() {
		return imoNo;
	}

	public void setImoNo(Long imoNo) {
		this.imoNo = imoNo;
	}

	@Column(nullable = true, length = 32)
	public String getCallsign() {
		return callsign;
	}

	public void setCallsign(String callsign) {
		this.callsign = callsign;
	}
	
	@Column(nullable = true, length = 32)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(nullable = true)
	public Integer getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Integer maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	@Column(nullable = true)
	public Integer getTonnage() {
		return tonnage;
	}

	public void setTonnage(Integer tonnage) {
		this.tonnage = tonnage;
	}
	
	@Column(nullable = true, length = 32)
	public String getCommCapabilities() {
		return commCapabilities;
	}

	public void setCommCapabilities(String commCapabilities) {
		this.commCapabilities = commCapabilities;
	}

	@Column(nullable = true)
	public Integer getRescueCapacity() {
		return rescueCapacity;
	}

	public void setRescueCapacity(Integer rescueCapacity) {
		this.rescueCapacity = rescueCapacity;
	}

	@Column(nullable = true)
	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	@Column(nullable = true)
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	@Column(nullable = true, length = 32)
	public String getIceClass() {
		return iceClass;
	}

	public void setIceClass(String iceClass) {
		this.iceClass = iceClass;
	}

	@Column(nullable = true)
	public Boolean getHelipad() {
		return helipad;
	}

	public void setHelipad(Boolean helipad) {
		this.helipad = helipad;
	}
	
	@OneToMany(mappedBy = "ship", fetch = FetchType.LAZY)
	@OrderBy("reportTime")
	public List<ShipReport> getReports() {
		return reports;
	}
	
	public void setReports(List<ShipReport> reports) {
		this.reports = reports;
	}
	
	@OneToOne(mappedBy = "ship", cascade = { CascadeType.ALL })
	public VoyageInformation getVoyageInformation() {
		return voyageInformation;
	}
	
	public void setVoyageInformation(VoyageInformation voyageInformation) {
		this.voyageInformation = voyageInformation;
	}
	
}
