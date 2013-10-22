/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries({ @NamedQuery(name = "Ship:getByMmsi", query = "SELECT s FROM Ship s WHERE s.mmsi = :mmsi"),
        @NamedQuery(name = "Ship:getByMaritimeId", query = "SELECT s FROM Ship s WHERE s.maritimeId = :maritimeId"),
        @NamedQuery(name = "Ship:getByCallsign", query = "SELECT s FROM Ship s WHERE s.aisData.callsign = :callsign"),
        @NamedQuery(name = "Vessel:getMmsiList", query = "SELECT s FROM Ship s WHERE s.mmsi in :mmsiNumbers") })
public class Ship extends BaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(unique = true)
    private String maritimeId;

    @Column(nullable = true)
    private Long mmsi;

    @Column(nullable = true, length = 32)
    private String type;

    @Min(0)
    @Max(200)
    @Column(nullable = true)
    private BigDecimal maxSpeed;

    @Min(0)
    @Column(nullable = true)
    private Integer grossTonnage;

    @Column(nullable = true, length = 64)
    private String commCapabilities;

    @Min(0)
    @Column(nullable = true)
    private Integer persons;

    @Column(nullable = true, length = 32)
    private String iceClass;

    @Column(nullable = true)
    private Boolean helipad;

    @OneToMany(mappedBy = "ship", fetch = FetchType.LAZY)
    @OrderBy("reportTime")
    private List<ShipReport> reports;

    @OneToOne(mappedBy = "ship", cascade = { CascadeType.ALL })
    private VoyagePlan voyagePlan;

    @OneToOne(cascade = { CascadeType.ALL })
    private Voyage activeVoyage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private ShipOwnerRole owner;

    private AisData aisData = new AisData();

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public dk.dma.embryo.rest.json.Ship toJsonModel() {
        dk.dma.embryo.rest.json.Ship ship = new dk.dma.embryo.rest.json.Ship();

        ship.setName(getAisData().getName());
        ship.setCallSign(getAisData().getCallsign());
        ship.setMmsi(getMmsi());
        ship.setMaritimeId(getMaritimeId());
        ship.setImo(getAisData().getImoNo());
        ship.setType(getType());
        ship.setCommCapabilities(getCommCapabilities());
        ship.setLength(getAisData().getLength());
        ship.setWidth(getAisData().getWidth());
        ship.setGrossTon(getGrossTonnage());
        ship.setMaxSpeed(getMaxSpeed() == null ? null : getMaxSpeed().floatValue());
        ship.setIceClass(getIceClass());
        ship.setHelipad(getHelipad());

        return ship;
    }

    public dk.dma.embryo.rest.json.VesselDetails toJsonModel2() {
        dk.dma.embryo.rest.json.VesselDetails vessel = new dk.dma.embryo.rest.json.VesselDetails();
        vessel.setAis(getAisData().toJsonModel());
        
        vessel.setMmsi(getMmsi());
        vessel.setMaritimeId(getMaritimeId());
        vessel.setCommCapabilities(getCommCapabilities());
        vessel.setGrossTon(getGrossTonnage());
        vessel.setMaxSpeed(getMaxSpeed() == null ? null : getMaxSpeed().floatValue());
        vessel.setIceClass(getIceClass());
        vessel.setHelipad(getHelipad());

        return vessel;
    }

    public static Ship fromJsonModel(dk.dma.embryo.rest.json.Ship ship) {
        Ship result = new Ship(ship.getMaritimeId());

        result.setMmsi(ship.getMmsi());
        result.setType(ship.getType());
        result.setCommCapabilities(ship.getCommCapabilities());
        result.setGrossTonnage(ship.getGrossTon());
        result.setMaxSpeed(ship.getMaxSpeed() == null ? null : BigDecimal.valueOf(ship.getMaxSpeed()));
        result.setIceClass(ship.getIceClass());
        result.setHelipad(ship.getHelipad());

        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Ship(String maritimeId) {
        this.maritimeId = maritimeId;
    }

    public Ship() {
        this(UUID.randomUUID().toString());
    }

    public Ship(Long mmsi) {
        this();
        this.mmsi = mmsi;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getMaritimeId() {
        return maritimeId;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }

    public ShipOwnerRole getOwner() {
        return owner;
    }

    public void setOwner(ShipOwnerRole owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(BigDecimal maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Integer getGrossTonnage() {
        return grossTonnage;
    }

    public void setGrossTonnage(Integer tonnage) {
        this.grossTonnage = tonnage;
    }

    public String getCommCapabilities() {
        return commCapabilities;
    }

    public void setCommCapabilities(String commCapabilities) {
        this.commCapabilities = commCapabilities;
    }

    public String getIceClass() {
        return iceClass;
    }

    public void setIceClass(String iceClass) {
        this.iceClass = iceClass;
    }

    public Boolean getHelipad() {
        return helipad;
    }

    public void setHelipad(Boolean helipad) {
        this.helipad = helipad;
    }

    public List<ShipReport> getReports() {
        return reports;
    }

    public VoyagePlan getVoyagePlan() {
        return voyagePlan;
    }

    public void setVoyagePlan(VoyagePlan plan) {
        plan.ship = this;
        this.voyagePlan = plan;
    }

    public Voyage getActiveVoyage() {
        return activeVoyage;
    }

    public void setActiveVoyage(Voyage activeVoyage) {
        this.activeVoyage = activeVoyage;
    }

    public AisData getAisData() {
        return aisData;
    }

    public void setAisData(AisData aisData) {
        this.aisData = aisData;
    }

}
