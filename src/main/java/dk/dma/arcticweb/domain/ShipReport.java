package dk.dma.arcticweb.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ShipReport extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	private Double lat;
	private Double lon;
	private String weather;
	private String iceObservations;
	private Date reportTime;
	private Date created;
	private Ship ship;
	
	public ShipReport() {
	}	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	@Override
	public Integer getId() {
		return id;
	}

	@Column(unique = false, nullable = true)
	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	@Column(unique = false, nullable = true)
	public Double getLon() {
		return lon;
	}


	public void setLon(Double lon) {
		this.lon = lon;
	}

	@Column(unique = false, nullable = true)
	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	@Column(unique = false, nullable = true)
	public String getIceObservations() {
		return iceObservations;
	}
	
	public void setIceObservations(String iceObservations) {
		this.iceObservations = iceObservations;
	}

	@Column(unique = false, nullable = false)
	public Date getReportTime() {
		return reportTime;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	@Column(unique = false, nullable = false, updatable = false)
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Ship getShip() {
		return ship;
	}
	
	public void setShip(Ship ship) {
		this.ship = ship;
	}
	
}
