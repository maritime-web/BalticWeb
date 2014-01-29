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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import dk.dma.embryo.rest.json.VesselDetails;

@Entity
@NamedQueries({
        @NamedQuery(name = "Vessel:getByMmsi", query = "SELECT v FROM Vessel v WHERE v.mmsi = :mmsi"),
        @NamedQuery(name = "Vessel:getByCallsign", query = "SELECT v FROM Vessel v WHERE v.aisData.callsign = :callsign"),
        @NamedQuery(name = "Vessel:getMmsiList", query = "SELECT v FROM Vessel v WHERE v.mmsi in :mmsiNumbers") })
public class Vessel extends BaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(unique = true)
    private Long mmsi;

    @Min(0)
    @Max(200)
    @Column(nullable = true)
    private BigDecimal maxSpeed;

    @Min(0)
    @Column(nullable = true)
    private Integer grossTonnage;

    @Column(nullable = true, length = 64)
    private String commCapabilities;

    /**
     * Maximum capacity for the number of persons on board. This information is (hopefully) usable in a rescue scenario.
     */
    @Min(0)
    @Column(nullable = true)
    private Integer persons;

    @Column(nullable = true, length = 32)
    private String iceClass;

    @Column(nullable = true)
    private Boolean helipad;

    @OneToMany(mappedBy = "vessel", cascade = { CascadeType.ALL })
    private List<Voyage> schedule = new ArrayList<>();

    @OneToOne(cascade = { CascadeType.ALL })
    private Voyage activeVoyage;

    private AisData aisData = new AisData();

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

    public dk.dma.embryo.rest.json.VesselDetails toJsonModel() {
        dk.dma.embryo.rest.json.VesselDetails vessel = new dk.dma.embryo.rest.json.VesselDetails();
        vessel.setAis(getAisData().toJsonModel());

        vessel.setMmsi(getMmsi());
        vessel.setCommCapabilities(getCommCapabilities());
        vessel.setGrossTon(getGrossTonnage());
        vessel.setMaxPersons(getPersons());
        vessel.setMaxSpeed(getMaxSpeed() == null ? null : getMaxSpeed().floatValue());
        vessel.setIceClass(getIceClass());
        vessel.setHelipad(getHelipad());

        return vessel;
    }

    public static Vessel fromJsonModel(VesselDetails details) {
        Vessel result = new Vessel();
        result.setMmsi(details.getMmsi());
        result.setCommCapabilities(details.getCommCapabilities());
        result.setGrossTonnage(details.getGrossTon());
        result.setPersons(details.getMaxPersons());
        result.setHelipad(details.getHelipad());
        result.setIceClass(details.getIceClass());
        result.setMaxSpeed(BigDecimal.valueOf(details.getMaxSpeed()));
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Vessel() {

    }

    public Vessel(Long mmsi) {
        this();
        this.mmsi = mmsi;
    }


    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return "Vessel [mmsi=" + mmsi + ", maxSpeed=" + maxSpeed + ", grossTonnage=" + grossTonnage
                + ", commCapabilities=" + commCapabilities + ", persons=" + persons + ", iceClass=" + iceClass
                + ", helipad=" + helipad + ", aisData=" + aisData + "]";
    }

    

    // //////////////////////////////////////////////////////////////////////
    // Utility
    // //////////////////////////////////////////////////////////////////////

    public void addVoyageEntry(Voyage entry) {
        schedule.add(entry);
        entry.vessel = this;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
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

    public Integer getPersons() {
        return persons;
    }

    public void setPersons(Integer persons) {
        this.persons = persons;
    }

    public List<Voyage> getSchedule() {
        return Collections.unmodifiableList(schedule);
    }

    public Voyage getActiveVoyage() {
        return activeVoyage;
    }

    public void setActiveVoyage(Voyage activeVoyage) {
        this.activeVoyage = activeVoyage;
    }

    public AisData getAisData() {
        if (aisData == null) {
            aisData = new AisData();
        }
        return aisData;
    }

    public void setAisData(AisData aisData) {
        this.aisData = aisData;
    }

}
