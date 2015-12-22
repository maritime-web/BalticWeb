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
package dk.dma.embryo.vessel.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.embryo.vessel.json.VesselOverview;
import dk.dma.embryo.vessel.model.Vessel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AisVessel {

    private String country;
    private String sourceRegion;
//    private String type;
    private Date lastReport;
    private Long mmsi;
    private String sourceCountry;
    private String sourceType;
    private String targetType;
    private String callsign;
    private Double cog;
    private String destination;
    private Double draught;
    private Date eta;
    private Double heading;
    private Long imoNo;
//    private String lastPosReport;
//    private String lastStaticReport;
    private Double lat;
    private Double length;
    private Double lon;
    private Boolean moored;
    private String name;
    private String navStatus;
    private Double rot;
    private Double sog;
//    private String vesselCargo;
@JsonIgnore
private VesselType type;
    private String vesselType;
    private Double width;
    private Double maxSpeed;
    private MaxSpeedOrigin maxSpeedOrigin;

    public enum MaxSpeedOrigin {
        AW, TABLE, SOG, DEFAULT
    }

    // //////////////////////////////////////////////////////////////////////
    // Business Logic
    // //////////////////////////////////////////////////////////////////////
    public static Function<AisVessel, AisVessel> addMaxSpeedFn(Map<Long, Vessel> arcticWebVesselAsMap){
        return vessel -> {
            vessel.deduceAndSetMaxSpeed(arcticWebVesselAsMap.get(vessel.getMmsi()));
            return vessel;
        };
    }

    public void deduceAndSetMaxSpeed(Vessel awVesselFromDatabase) {

        boolean isMaxSpeedSetOnAisVessel = false;

        // If exists set Max Speed from ArcticWeb vessel in the database
        if(awVesselFromDatabase != null && awVesselFromDatabase.getMaxSpeed() != null && awVesselFromDatabase.getMaxSpeed().doubleValue() > 0) {
            this.setMaxSpeed(awVesselFromDatabase.getMaxSpeed().doubleValue());
            this.setMaxSpeedOrigin(AisVessel.MaxSpeedOrigin.AW);
            isMaxSpeedSetOnAisVessel = true;
        }

        // If not already set from ArcticWeb vessel in database -> set it vessel type
        if(!isMaxSpeedSetOnAisVessel && this.getVesselType() != null) {
            Double maxSpeedByVesselType = ServiceSpeedByShipTypeMapper.lookupSpeed(this.getType());
            if(maxSpeedByVesselType > 0.0) {
                this.setMaxSpeed(maxSpeedByVesselType);
                this.setMaxSpeedOrigin(AisVessel.MaxSpeedOrigin.TABLE);
                isMaxSpeedSetOnAisVessel = true;
            }
        }

        // If not already set from ArcticWeb vessel in database or from vessel type -> set sog
        if(!isMaxSpeedSetOnAisVessel && this.getSog() != null) {
            this.setMaxSpeed(this.getSog());
            this.setMaxSpeedOrigin(AisVessel.MaxSpeedOrigin.SOG);
            isMaxSpeedSetOnAisVessel = true;
        }

        // Fallback - set 0.0
        if(!isMaxSpeedSetOnAisVessel) {
            this.setMaxSpeed(0.0);
            this.setMaxSpeedOrigin(AisVessel.MaxSpeedOrigin.DEFAULT);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public static Map<Long, AisVessel> asMap(List<AisVessel> vessels){
        return vessels.stream().collect(Collectors.toMap(AisVessel::getMmsi, Function.identity()));
    }


    public VesselOverview toVesselOverview() {
        VesselOverview vesselOverview = new VesselOverview();
        vesselOverview.setX(getLon());
        vesselOverview.setY(getLat());
        vesselOverview.setAngle(getCog() != null ? getCog() : 0);
        vesselOverview.setMmsi(getMmsi());
        vesselOverview.setName(getName());
        vesselOverview.setCallSign(getCallsign());
        vesselOverview.setMoored(getMoored() != null ? getMoored() : false);

        VesselType vesType = VesselType.getShipTypeFromTypeText(getVesselType());
        String type = ShipTypeMapper.getInstance().getColor(vesType).ordinal() + "";
        vesselOverview.setType(type);

        vesselOverview.setInAW(false);
        mapMaxSpeed(vesselOverview, getMaxSpeed(), getMaxSpeedOrigin());
        return vesselOverview;
    }

    private static void mapMaxSpeed(VesselOverview vesselOverview, Double maxSpeed, MaxSpeedOrigin maxSpeedOrigin) {
        if(maxSpeedOrigin == MaxSpeedOrigin.AW) {
            vesselOverview.setAwsog(maxSpeed);
        } else if (maxSpeedOrigin == MaxSpeedOrigin.TABLE) {
            vesselOverview.setSsog(maxSpeed);
        } else if (maxSpeedOrigin == MaxSpeedOrigin.SOG) {
            vesselOverview.setSog(maxSpeed);
        }
    }

    public static List<VesselOverview> toVesselOverview(List<AisVessel> vessels) {
        return toVesselOverviewStream(vessels).collect(Collectors.toList());
    }

    public static Stream<VesselOverview> toVesselOverviewStream(List<AisVessel> vessels) {
        return vessels.stream().map(vessel -> vessel.toVesselOverview());
    }

    public static AisVessel create(Vessel vessel){
        AisVessel aisVessel = new AisVessel();
        aisVessel.setCallsign(vessel.getAisData().getCallsign());
        aisVessel.setImoNo(vessel.getAisData().getImoNo());
        aisVessel.setMmsi(vessel.getMmsi());
        aisVessel.setName(vessel.getAisData().getName());
        return aisVessel;
    }

    /**
     * This will match the list of aisVessels and vessels on MMSI number, and if any vessels are not in the aisVessels, they are automatically transformed and added.
     * @param aisVessels
     * @param vessels
     * @return
     */
    public static List<AisVessel> addMissingVessels(List<AisVessel> aisVessels, List<Vessel> vessels){
        List result = new ArrayList<>(aisVessels.size() + vessels.size());
        Set<Long> mmsiNumbers = aisVessels.stream().map(aisVessel -> aisVessel.getMmsi()).collect(Collectors.toSet());
        for(Vessel vessel : vessels){
            if(!mmsiNumbers.contains(vessel.getMmsi())){
                result.add(AisVessel.create(vessel));
            }
        }
        if(result.size() > 0){
            result.addAll(aisVessels);
            return result;
        }
        return aisVessels;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mmsi == null) ? 0 : mmsi.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object otherObject) {
        
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (getClass() != otherObject.getClass()) {
            return false;
        }
        AisVessel otherVessel = (AisVessel) otherObject;
        if (mmsi == null) {
            if (otherVessel.mmsi != null) {
                return false;
            }
        } else if (!mmsi.equals(otherVessel.mmsi)){
            return false;
        }
        
        return true;
    }
    
    
    
    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSourceRegion() {
        return sourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    public Date getLastReport() {
        return lastReport;
    }

    public void setLastReport(Date lastReport) {
        this.lastReport = lastReport;
    }

    public Long getMmsi() {
        return mmsi;
    }

    public void setMmsi(Long mmsi) {
        this.mmsi = mmsi;
    }

    public String getSourceCountry() {
        return sourceCountry;
    }

    public void setSourceCountry(String sourceCountry) {
        this.sourceCountry = sourceCountry;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public Double getCog() {
        return cog;
    }

    public void setCog(Double cog) {
        this.cog = cog;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDraught() {
        return draught;
    }

    public void setDraught(Double draught) {
        this.draught = draught;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Long getImoNo() {
        return imoNo;
    }

    public void setImoNo(Long imoNo) {
        this.imoNo = imoNo;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Boolean getMoored() {
        return moored;
    }

    public void setMoored(Boolean moored) {
        this.moored = moored;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNavStatus() {
        return navStatus;
    }

    public void setNavStatus(String navStatus) {
        this.navStatus = navStatus;
    }

    public Double getRot() {
        return rot;
    }

    public void setRot(Double rot) {
        this.rot = rot;
    }

    public Double getSog() {
        return sog;
    }

    public void setSog(Double sog) {
        this.sog = sog;
    }

    public String getVesselType() {
        return vesselType;
    }

    public void setVesselType(String vesselType) {
        this.vesselType = vesselType;
    }

    public VesselType getType() {
        return type;
    }

    public void setType(VesselType type) {
        this.type = type;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public MaxSpeedOrigin getMaxSpeedOrigin() {
        return maxSpeedOrigin;
    }

    public void setMaxSpeedOrigin(MaxSpeedOrigin maxSpeedOrigin) {
        this.maxSpeedOrigin = maxSpeedOrigin;
    }

}
