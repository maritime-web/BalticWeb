/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.vessel.model;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.vessel.json.VesselDetails;
import org.apache.commons.lang.ObjectUtils;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Vessel mergeNonReferenceFields(Vessel vessel) {
        setMmsi(vessel.getMmsi());
        setCommCapabilities(vessel.getCommCapabilities());
        setGrossTonnage(vessel.getGrossTonnage());
        setPersons(vessel.getPersons());
        setMaxSpeed(vessel.getMaxSpeed());
        setIceClass(vessel.getIceClass());
        setHelipad(vessel.getHelipad());
        setAisData(vessel.getAisData());
        return this;
    }

    public VesselDetails toJsonModel() {
        VesselDetails vessel = new VesselDetails();

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
        if(details.getMaxSpeed() != null) {
            result.setMaxSpeed(BigDecimal.valueOf(details.getMaxSpeed()));
        }
        return result;
    }

    public static List<Long> extractMmsiNumbers(List<Vessel> vessels){
        return vessels.stream().map(vessel -> vessel.getMmsi()).collect(Collectors.toList());
    }

    public static Map<Long, Vessel> asMap(List<Vessel> vessels){
        return vessels.stream().collect(Collectors.toMap(Vessel::getMmsi, Function.identity()));
    }
    // //////////////////////////////////////////////////////////////////////
    // Business Logic
    // //////////////////////////////////////////////////////////////////////
    public boolean isUpToDate(String name, String callSign, Long imo) {
        return ObjectUtils.equals(getAisData().getName(), name) && ObjectUtils.equals(getAisData().getCallsign(), callSign)
                && ObjectUtils.equals(getAisData().getImoNo(), imo);
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
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public void addVoyageEntry(Voyage entry) {
        schedule.add(entry);
        entry.vessel = this;
    }

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
