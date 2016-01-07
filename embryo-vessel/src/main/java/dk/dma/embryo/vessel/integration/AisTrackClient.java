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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author Jesper Tejlgaard
 *
 * Client to AISTrack server. See https://github.com/dma-ais/AisTrack
 *
 */
@Path("/ais-track")
public interface AisTrackClient {

    /**
     *  Full JSON

     {
     "source": {
     "country": "DE",
     "bs": "2190077",
     "id": "AISW"
     },
     "target": {
     "mmsi": 211211450,
     "country": "DE",
     "lastReport": "2015-05-28T09:05:19.869Z",
     "created": "2015-05-28T09:06:56.578Z",
     "vesselStatic": {
     "mmsi": 211211450,
     "received": "2015-05-28T09:06:56.579Z",
     "sourceTimestamp": "2015-05-28T09:05:19.869Z",
     "created": "2015-05-28T09:06:56.579Z",
     "name": "ALSTER",
     "callsign": "DRHF",
     "shipType": 35,
     "shipTypeCargo": {
     "shipType": "MILITARY",
     "code": 35,
     "shipCargo": "UNDEFINED"
     },
     "dimensions": {
     "dimBow": 21,
     "dimStern": 63,
     "dimPort": 10,
     "dimStarboard": 4
     },

     "imoNo" : 50602062,
     "destination" : "RIO DE JANEIRO",
     "eta" : "2015-05-28T05:00:57.958Z",
     "posType" : 6,
     "draught" : 6.0,
     "version": 1,
     "dte": 0
     },
     "vesselPosition": {
     "mmsi": 211211450,
     "received": "2015-05-28T09:06:56.579Z",
     "sourceTimestamp": "2015-05-28T09:06:10.677Z",
     "created": "2015-05-28T09:06:56.578Z",
     "sog": 0.1,
     "cog": 353.2,
     "heading" : 298.0,
     "pos": {
     "lat": 54.42892,
     "lon": 13.697369
     },
     "posAcc": 1,
     "utcSec": 9,
     "raim": 0,
     "rot": 128,
     "navStatus": 1,
     "specialManIndicator": 0
     },
     "targetType": "A"
     }
     }
     *
     */
    @GET
    @Path("/tracks")
    List<AisTrack> vesselsByMmsis(@QueryParam("mmsi") List<Long> mmsi, @QueryParam("sourceFilter") String commaSeparatedListOfRegions);

    @GET
    @Path("/tracks")
    List<AisTrack> vessels(@QueryParam("mmsi") List<Long> mmsi, @QueryParam("baseArea") String baseArea, @QueryParam("area") List<String> areas, @QueryParam("sourceFilter") String commaSeparatedListOfRegions);

    @GET
    @Path("/track/{mmsi}")
    AisTrack vessel(@PathParam("mmsi") Long mmsi, @QueryParam("sourceFilter") String commaSeparatedListOfRegions);


    /**
     * @author ThomasBerg
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AisTrack {

        private Source source;
        private Target target;

        public AisVessel toJsonVessel() {
            AisVessel vesselType = new AisVessel();

            if(this.getSource() != null) {
                vesselType.setSourceCountry(this.getSource().getCountry());
                vesselType.setSourceRegion(this.getSource().getRegion());
                vesselType.setSourceType(this.getSource().getType());
            }

            if(this.getTarget() != null) {
                Target target = this.getTarget();

                vesselType.setCountry(target.getCountry());
                vesselType.setLastReport(target.getLastReport());
                vesselType.setMmsi(target.getMmsi());
                vesselType.setTargetType(target.getTargetType());

                if(target.getVesselStatic() != null) {
                    VesselStatic vesselStatic = target.getVesselStatic();

                    vesselType.setCallsign(vesselStatic.getCallsign());
                    vesselType.setDestination(vesselStatic.getDestination());
                    vesselType.setDraught(vesselStatic.getDraught());
                    vesselType.setEta(vesselStatic.getEta());
                    vesselType.setImoNo(vesselStatic.getImoNo());
                    vesselType.setName(vesselStatic.getName());

                    if(vesselStatic.getShipTypeCargo() != null) {
                        ShipTypeCargo shipTypeCargo = vesselStatic.getShipTypeCargo();
                        vesselType.setVesselType(shipTypeCargo.getShipType());
                        VesselType type = VesselType.getShipTypeFromTypeText(shipTypeCargo.getShipType());
                        vesselType.setType(type);
                    }

                    if(vesselStatic.getDimensions() != null) {
                        Dimensions dimensions = vesselStatic.getDimensions();
                        vesselType.setLength(dimensions.getLength());
                        vesselType.setWidth(dimensions.getWidth());
                    }
                }

                if(target.getVesselPosition() != null) {
                    VesselPosition vesselPosition = target.getVesselPosition();

                    vesselType.setCog(vesselPosition.getCog());
                    vesselType.setHeading(vesselPosition.getHeading());
                    vesselType.setMoored(vesselPosition.getMoored());

                    vesselType.setNavStatus(vesselPosition.getNavStatus() == null ? null : vesselPosition.getNavStatus().toString());
                    vesselType.setRot(vesselPosition.getRot());
                    vesselType.setSog(vesselPosition.getSog());

                    if(vesselPosition.getPos() != null) {
                        Pos pos = vesselPosition.getPos();

                        vesselType.setLat(pos.getLat());
                        vesselType.setLon(pos.getLon());
                    }
                }

            }

            // minimum
            if(minimumCriteriaFulfilled(vesselType)) {
                return vesselType;
            } else {
                return null;
            }
        }

        public static Function<AisTrack, AisVessel> toJsonVesselFn() {
            return track -> track.toJsonVessel();
        }

        public boolean minimumCriteriaFulfilled(AisVessel vesselType) {
            return vesselType.getMmsi() != null && vesselType.getLat() != null && vesselType.getLon() != null;
        }

        public static Predicate<AisTrack> valid() {
            return track -> track.minimumCriteriaFulfilled();
        }

        public boolean minimumCriteriaFulfilled() {
            if (target == null || target.getVesselPosition() == null || target.getVesselPosition().getPos() == null) {
                return false;
            }
            return target.getMmsi() != null && target.getVesselPosition().getPos().getLat() != null && target.getVesselPosition().getPos().getLon() != null;
        }


        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }

        public Target getTarget() {
            return target;
        }
        public void setTarget(Target target) {
            this.target = target;
        }

        public Source getSource() {
            return source;
        }
        public void setSource(Source source) {
            this.source = source;
        }
    }

    /**
     *
     * @author ThomasBerg
     *
     * "source": {
    "id": "AISW",
    "bs": "2190077",
    "country": "NO",
    "region": "808"
    }
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Source {

        private String id;
        private Integer bs;
        private String country;
        private String type;
        private String region;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }

        public String getRegion() {
            return region;
        }
        public void setRegion(String region) {
            this.region = region;
        }

        public Integer getBs() {
            return bs;
        }
        public void setBs(Integer bs) {
            this.bs = bs;
        }

        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }

        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     *
     * @author ThomasBerg
     *
     * "target": {
    "mmsi": 211211450,
    "country": "DE",
    "lastReport": "2015-05-28T09:05:19.869Z",
    "created": "2015-05-28T09:06:56.578Z",
    "vesselStatic": {
    "mmsi": 211211450,
    "received": "2015-05-28T09:06:56.579Z",
    "sourceTimestamp": "2015-05-28T09:05:19.869Z",
    "created": "2015-05-28T09:06:56.579Z",
    "name": "ALSTER",
    "callsign": "DRHF",
    "shipType": 35,
    "shipTypeCargo": {
    "shipType": "MILITARY",
    "code": 35,
    "shipCargo": "UNDEFINED"
    },
    "dimensions": {
    "dimBow": 21,
    "dimStern": 63,
    "dimPort": 10,
    "dimStarboard": 4
    },

    "imoNo" : 50602062,
    "destination" : "RIO DE JANEIRO",
    "eta" : "2015-05-28T05:00:57.958Z",
    "posType" : 6,
    "draught" : 6.0,
    "version": 1,
    "dte": 0
    },
    "vesselPosition": {
    "mmsi": 211211450,
    "received": "2015-05-28T09:06:56.579Z",
    "sourceTimestamp": "2015-05-28T09:06:10.677Z",
    "created": "2015-05-28T09:06:56.578Z",
    "sog": 0.1,
    "cog": 353.2,
    "heading" : 298.0,
    "pos": {
    "lat": 54.42892,
    "lon": 13.697369
    },
    "posAcc": 1,
    "utcSec": 9,
    "raim": 0,
    "rot": 128,
    "navStatus": 1,
    "specialManIndicator": 0
    },
    "targetType": "A"
    }
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Target {

        private Long mmsi;
        private String country;
        private Date lastReport;
        private Date created;
        private String targetType;
        private VesselStatic vesselStatic;
        private VesselPosition vesselPosition;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }

        public Long getMmsi() {
            return mmsi;
        }
        public void setMmsi(Long mmsi) {
            this.mmsi = mmsi;
        }

        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }

        public Date getLastReport() {
            return lastReport;
        }
        public void setLastReport(Date lastReport) {
            this.lastReport = lastReport;
        }

        public Date getCreated() {
            return created;
        }
        public void setCreated(Date created) {
            this.created = created;
        }

        public String getTargetType() {
            return targetType;
        }
        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public VesselStatic getVesselStatic() {
            return vesselStatic;
        }
        public void setVesselStatic(VesselStatic vesselStatic) {
            this.vesselStatic = vesselStatic;
        }

        public VesselPosition getVesselPosition() {
            return vesselPosition;
        }
        public void setVesselPosition(VesselPosition vesselPosition) {
            this.vesselPosition = vesselPosition;
        }
    }

    /**
     *
     * @author ThomasBerg
     *
     * "vesselStatic": {
    "mmsi": 211211450,
    "received": "2015-05-28T09:06:56.579Z",
    "sourceTimestamp": "2015-05-28T09:05:19.869Z",
    "created": "2015-05-28T09:06:56.579Z",
    "name": "ALSTER",
    "callsign": "DRHF",
    "shipType": 35,
    "shipTypeCargo": {
    "shipType": "MILITARY",
    "code": 35,
    "shipCargo": "UNDEFINED"
    },
    "dimensions": {
    "dimBow": 21,
    "dimStern": 63,
    "dimPort": 10,
    "dimStarboard": 4
    },

    "imoNo" : 50602062,
    "destination" : "RIO DE JANEIRO",
    "eta" : "2015-05-28T05:00:57.958Z",
    "posType" : 6,
    "draught" : 6.0,
    "version": 1,
    "dte": 0
    }
     */

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class VesselStatic {

        private Integer mmsi;
        private Date received;
        private Date sourceTimestamp;
        private Date created;
        private String name;
        private String callsign;
        private Integer shipType;
        private Long imoNo;
        private String destination;
        private Date eta;
        private Double draught;

        // postType er 0-15
        private Integer postType;

        // versino er 0-3
        private Integer version;

        // DTE er 0 eller 1
        private Integer dte;
        private ShipTypeCargo shipTypeCargo;
        private Dimensions dimensions;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        public Integer getMmsi() {
            return mmsi;
        }
        public void setMmsi(Integer mmsi) {
            this.mmsi = mmsi;
        }

        public Date getReceived() {
            return received;
        }
        public void setReceived(Date received) {
            this.received = received;
        }

        public Date getSourceTimestamp() {
            return sourceTimestamp;
        }
        public void setSourceTimestamp(Date sourceTimestamp) {
            this.sourceTimestamp = sourceTimestamp;
        }

        public Date getCreated() {
            return created;
        }
        public void setCreated(Date created) {
            this.created = created;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getCallsign() {
            return callsign;
        }
        public void setCallsign(String callsign) {
            this.callsign = callsign;
        }

        public Integer getShipType() {
            return shipType;
        }
        public void setShipType(Integer shipType) {
            this.shipType = shipType;
        }

        public Integer getPostType() {
            return postType;
        }
        public void setPostType(Integer postType) {
            this.postType = postType;
        }

        public Integer getVersion() {
            return version;
        }
        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getDte() {
            return dte;
        }
        public void setDte(Integer dte) {
            this.dte = dte;
        }

        public ShipTypeCargo getShipTypeCargo() {
            return shipTypeCargo;
        }
        public void setShipTypeCargo(ShipTypeCargo shipTypeCargo) {
            this.shipTypeCargo = shipTypeCargo;
        }

        public Dimensions getDimensions() {
            return dimensions;
        }
        public void setDimensions(Dimensions dimensions) {
            this.dimensions = dimensions;
        }

        public Long getImoNo() {
            return imoNo;
        }
        public void setImoNo(Long imoNo) {
            this.imoNo = imoNo;
        }

        public String getDestination() {
            return destination;
        }
        public void setDestination(String destination) {
            this.destination = destination;
        }

        public Date getEta() {
            return eta;
        }
        public void setEta(Date eta) {
            this.eta = eta;
        }

        public Double getDraught() {
            return draught;
        }
        public void setDraught(Double draught) {
            this.draught = draught;
        }
    }

    /**
     "vesselPosition": {
     "mmsi": 211211450,
     "received": "2015-05-28T09:06:56.579Z",
     "sourceTimestamp": "2015-05-28T09:06:10.677Z",
     "created": "2015-05-28T09:06:56.578Z",
     "sog": 0.1,
     "cog": 353.2,
     "heading" : 298.0,
     "pos": {
     "lat": 54.42892,
     "lon": 13.697369
     },
     "posAcc": 1,
     "utcSec": 9,
     "raim": 0,
     "rot": 128,
     "navStatus": 1,
     "specialManIndicator": 0
     }
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class VesselPosition {

        private Integer mmsi;
        private Date received;
        private Date sourceTimestamp;
        private Date created;
        private Double sog;
        private Double cog;
        private Double heading;
        private Pos pos;
        private Integer posAcc;
        private Integer utcSec;
        private Integer raim;
        private Double rot;
        private Integer specialManIndicator;

        /**
         0 = under way using engine,
         1 = at anchor,
         2 = not under command,
         3 = restricted maneuverability,
         4 = constrained by her draught,
         5 = moored,
         6 = aground,
         7 = engaged in fishing,
         8 = under way sailing,
         9 = reserved for future amendment of navigational status for ships carrying DG, HS, or MP, or IMO hazard or pollutant category C, high speed craft (HSC),
         10 = reserved for future amendment of navigational status for ships carrying dangerous goods (DG), harmful substances (HS) or marine pollutants (MP), or IMO hazard or pollutant category A, wing in ground (WIG);11 = power- driven vessel towing astern (regional use),
         12 = power-driven vessel pushing ahead or towing alongside (regional use);
         13 = reserved for future use,
         14 = AIS-SART (active), MOB-AIS, EPIRB-AIS
         15 = undefined = default (also used by AIS-SART, MOB-AIS and EPIRB- AIS under test)
         */
        private Integer navStatus;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        public boolean getMoored() {

            return navStatus != null && navStatus == 5;
        }

        public Integer getMmsi() {
            return mmsi;
        }
        public void setMmsi(Integer mmsi) {
            this.mmsi = mmsi;
        }

        public Date getReceived() {
            return received;
        }
        public void setReceived(Date received) {
            this.received = received;
        }

        public Date getSourceTimestamp() {
            return sourceTimestamp;
        }
        public void setSourceTimestamp(Date sourceTimestamp) {
            this.sourceTimestamp = sourceTimestamp;
        }

        public Date getCreated() {
            return created;
        }
        public void setCreated(Date created) {
            this.created = created;
        }

        public Double getSog() {
            return sog;
        }
        public void setSog(Double sog) {
            this.sog = sog;
        }

        public Double getCog() {
            return cog;
        }
        public void setCog(Double cog) {
            this.cog = cog;
        }

        public Pos getPos() {
            return pos;
        }
        public void setPos(Pos pos) {
            this.pos = pos;
        }

        public Integer getPosAcc() {
            return posAcc;
        }
        public void setPosAcc(Integer posAcc) {
            this.posAcc = posAcc;
        }

        public Integer getUtcSec() {
            return utcSec;
        }
        public void setUtcSec(Integer utcSec) {
            this.utcSec = utcSec;
        }

        public Integer getRaim() {
            return raim;
        }
        public void setRaim(Integer raim) {
            this.raim = raim;
        }

        public Double getRot() {
            return rot;
        }
        public void setRot(Double rot) {
            this.rot = rot;
        }

        public Integer getNavStatus() {
            return navStatus;
        }
        public void setNavStatus(Integer navStatus) {
            this.navStatus = navStatus;
        }

        public Integer getSpecialManIndicator() {
            return specialManIndicator;
        }
        public void setSpecialManIndicator(Integer specialManIndicator) {
            this.specialManIndicator = specialManIndicator;
        }

        public Double getHeading() {
            return heading;
        }
        public void setHeading(Double heading) {
            this.heading = heading;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Pos {

        private Double lat;
        private Double lon;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        public Double getLat() {
            return lat;
        }
        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }
        public void setLon(Double lon) {
            this.lon = lon;
        }
    }

    /**
     *
     * @author ThomasBerg
     * "shipTypeCargo": {
    "shipType": "UNDEFINED",
    "code": 0,
    "shipCargo": "UNDEFINED"
    }
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class ShipTypeCargo {

        /*
        public enum ShipType {
        UNDEFINED, WIG, PILOT, SAR, TUG, PORT_TENDER, ANTI_POLLUTION, LAW_ENFORCEMENT, MEDICAL, FISHING, TOWING, TOWING_LONG_WIDE, DREDGING, DIVING, MILITARY, SAILING, PLEASURE, HSC, PASSENGER, CARGO, TANKER, SHIPS_ACCORDING_TO_RR, UNKNOWN
        }
         */
        private String shipType;

        /*
        public enum CargoType {
        UNDEFINED, A, B, C, D
        }
        */
        private String shipCargo;

        // Code er et tal 00-99
        private Integer code;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        public String getShipType() {
            return shipType;
        }
        public void setShipType(String shipType) {
            this.shipType = shipType;
        }

        public String getShipCargo() {
            return shipCargo;
        }
        public void setShipCargo(String shipCargo) {
            this.shipCargo = shipCargo;
        }

        public Integer getCode() {
            return code;
        }
        public void setCode(Integer code) {
            this.code = code;
        }
    }


    /**
     *
     * @author ThomasBerg
     *
    "dimensions": {
    "dimBow": 0,
    "dimStern": 0,
    "dimPort": 0,
    "dimStarboard": 0
    },

    // Det er tal. I enheden meter.heltal
     */
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Dimensions {

        private Integer dimBow;
        private Integer dimStern;
        private Integer dimPort;
        private Integer dimStarboard;

        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }

        // dimStern + dimBow
        public Double getLength() {

            Double length = null;
            if(dimStern != null && dimBow != null) {
                Number lengthAsNumber =  dimStern + dimBow;
                length = lengthAsNumber.doubleValue();
            }

            return length;
        }

        // dimPort + dimStarboard
        public Double getWidth() {

            Double width = null;
            if(dimPort != null && dimStarboard != null) {
                Number widthAsNumber =  dimPort + dimStarboard;
                width = widthAsNumber.doubleValue();
            }

            return width;
        }

        public Integer getDimBow() {
            return dimBow;
        }

        public void setDimBow(Integer dimBow) {
            this.dimBow = dimBow;
        }

        public Integer getDimStern() {
            return dimStern;
        }
        public void setDimStern(Integer dimStern) {
            this.dimStern = dimStern;
        }

        public Integer getDimPort() {
            return dimPort;
        }
        public void setDimPort(Integer dimPort) {
            this.dimPort = dimPort;
        }

        public Integer getDimStarboard() {
            return dimStarboard;
        }
        public void setDimStarboard(Integer dimStarboard) {
            this.dimStarboard = dimStarboard;
        }
    }
}
