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
import javax.validation.constraints.NotNull;

import dk.dma.embryo.rest.json.Ship;

@Entity
@NamedQueries({ @NamedQuery(name = "Ship:getByMmsi", query = "SELECT s FROM Ship2 s WHERE s.mmsi = :mmsi"),
        @NamedQuery(name = "Ship:getByMaritimeId", query = "SELECT s FROM Ship2 s WHERE s.maritimeId = :maritimeId"),
        @NamedQuery(name = "Ship:getByCallsign", query = "SELECT s FROM Ship2 s WHERE s.callsign = :callsign") })
public class Ship2 extends BaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    @Column(nullable = true, length = 128)
    private String name;

    @NotNull
    @Column(unique = true)
    private String maritimeId;

    @Column(nullable = true)
    private Long mmsi;

    @Column(nullable = true)
    private Long imoNo;

    @Column(nullable = true, length = 32)
    private String callsign;

    @Column(nullable = true, length = 32)
    private String type;

    @Column(nullable = true)
    private Integer maxSpeed;

    @Column(nullable = true)
    private Integer tonnage;

    @Column(nullable = true, length = 32)
    private String commCapabilities;

    // TODO REMOVE ?
    @Column(nullable = true)
    private Integer rescueCapacity;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer length;

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

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public Ship toJsonModel(){
        Ship ship = new Ship();

        ship.setName(getName());
        ship.setCallSign(getCallsign());
        ship.setMmsi(getMmsi());
        ship.setMaritimeId(getMaritimeId());
        ship.setImo(getImoNo());
        ship.setType(getType());
        ship.setCommCapabilities(getCommCapabilities());
        ship.setLength(getLength());
        ship.setWidth(getWidth());
        ship.setTonnage(getTonnage());
        ship.setMaxSpeed(getMaxSpeed());
        ship.setIceClass(getIceClass());
        ship.setHelipad(getHelipad());
        
        return ship;
    }

    public static Ship2 fromJsonModel(Ship ship){
        Ship2 result = new Ship2(ship.getMaritimeId());

        result.setName(ship.getName());
        result.setCallsign(ship.getCallSign());
        result.setMmsi(ship.getMmsi());
        result.setImoNo(ship.getImo());
        result.setType(ship.getType());
        result.setCommCapabilities(ship.getCommCapabilities());
        result.setLength(ship.getLength());
        result.setWidth(ship.getWidth());
        result.setTonnage(ship.getTonnage());
        result.setMaxSpeed(ship.getMaxSpeed());
        result.setIceClass(ship.getIceClass());
        result.setHelipad(ship.getHelipad());
        
        return result;
    }

    
    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Ship2(String maritimeId) {
        this.maritimeId = maritimeId;
    }

    public Ship2() {
        this(UUID.randomUUID().toString());
    }

    public Ship2(Long mmsi) {
        this();
        this.mmsi = mmsi;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getMaritimeId() {
        return maritimeId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getImoNo() {
        return imoNo;
    }

    public void setImoNo(Long imoNo) {
        this.imoNo = imoNo;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Integer maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Integer getTonnage() {
        return tonnage;
    }

    public void setTonnage(Integer tonnage) {
        this.tonnage = tonnage;
    }

    public String getCommCapabilities() {
        return commCapabilities;
    }

    public void setCommCapabilities(String commCapabilities) {
        this.commCapabilities = commCapabilities;
    }

    public Integer getRescueCapacity() {
        return rescueCapacity;
    }

    public void setRescueCapacity(Integer rescueCapacity) {
        this.rescueCapacity = rescueCapacity;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
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
}
