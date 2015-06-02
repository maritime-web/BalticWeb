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
package dk.dma.embryo.vessel.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.vessel.json.client.AisClientHelper;
import dk.dma.embryo.vessel.json.client.AisRestDataService;
import dk.dma.embryo.vessel.json.client.AisRestDataService.AisVessel;
import dk.dma.embryo.vessel.json.client.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;

@Singleton
public class AisDataServiceImpl implements AisDataService {

    private List<Vessel> vesselsAllowed = new ArrayList<>();
    private List<Vessel> vesselsInAisCircle = new ArrayList<>();
    private List<Vessel> vesselsOnMap = new ArrayList<>();
    private Map<Long, Double> maxSpeeds =  new HashMap<Long, Double>();
    
    @Inject
    private Logger logger;
    
    @Inject
    @Property("embryo.aisDataLimit.latitude")
    private double aisDataLimitLatitude;
    
    @Inject
    @Property("embryo.aisCircle.default.latitude")
    private double aisCircleLatitude;

    @Inject
    @Property("embryo.aisCircle.default.longitude")
    private double aisCircleLongitude;

    @Inject
    @Property("embryo.aisCircle.default.radius")
    private double aisCircleRadius;

    @Inject
    private VesselDao vesselRepository;
    
    @Inject
    private AisRestDataService aisRestDataService;
    
    
    public List<Vessel> getAisVessels(final boolean withExactEarth, final boolean userHasAnyActiveSelectionGroups) {
        
        final List<dk.dma.embryo.vessel.model.Vessel> articWebVesselsAsList = this.getArcticWebVesselsFromDatabase();

        List<Vessel> allAisVessels = AisClientHelper.mergeAisVesselsWithArcticVessels(
                this.getAisVesselsFromRestService(withExactEarth, userHasAnyActiveSelectionGroups),
                this.getArcticWebVesselsFromRestService(withExactEarth, AisClientHelper.getMmsiAsSeparatedList(articWebVesselsAsList))
        );
        
        // It does not matter if update source is with or without ExactEarth
        Map<Long, dk.dma.embryo.vessel.model.Vessel> arcticWebVesselAsMap = this.updateArcticWebVesselInDatabase(allAisVessels, articWebVesselsAsList);
        
        AisClientHelper.updateMaxSpeedForVessels(allAisVessels, arcticWebVesselAsMap);
        
        logger.info("AIS Vessels : " + allAisVessels.size() + " - ExactEarth -> " + withExactEarth);
        
        return allAisVessels; 
    }
    
    private List<Vessel> getAisVesselsFromRestService(final boolean withExactearth, final boolean userHasAnyActiveSelectionGroups) {
        
        
        List<AisVessel> aisAllAllowed = this.aisRestDataService.vesselListAllAllowed(
                !userHasAnyActiveSelectionGroups ? AisRestDataService.AREA_FILTER_CIRCLEBOX : AisRestDataService.AREA_FILTER_ALLOWED, 
                AisClientHelper.getSourceFilter(withExactearth));
        
        List<Vessel> vessels = AisClientHelper.mapAisVesselsToVessels(aisAllAllowed);
        
        // Default view of vessels is full arctic circle if user has not active SelectionGroups
        if(!userHasAnyActiveSelectionGroups) {
            vessels = filterVesselsWithinCircle(vessels);
        }
        
        logger.info("AIS server returns " + vessels.size() + " vessel targets above 57 latitude - ExactEarth = " + withExactearth + ", userHasAnyActiveSelectionGroups = " + userHasAnyActiveSelectionGroups);
        
        return vessels;
    }

    private List<Vessel> filterVesselsWithinCircle(List<Vessel> aisAllAllowed) {
        
         return aisAllAllowed.stream().filter
                 (vessel -> isWithinAisCircle(vessel.getLon(), vessel.getLat()))
                 .collect(Collectors.toList());
    }
    
    private List<Vessel> getArcticWebVesselsFromRestService(final boolean withExactearth,final String mmsiAsSeparatedList) {
        
        List<AisVessel> awVesselsFromAisServer = this.aisRestDataService.vesselListByMmsis(mmsiAsSeparatedList, AisClientHelper.getSourceFilter(withExactearth));
        
        List<Vessel> vessels = AisClientHelper.mapAisVesselsToVessels(awVesselsFromAisServer);
        logger.info("AIS server returns " + vessels.size() + " ArcticWeb vessel targets - ExactEarth = " + withExactearth);
        
        return vessels;
    }
    
    private List<dk.dma.embryo.vessel.model.Vessel> getArcticWebVesselsFromDatabase() {
 
        List<dk.dma.embryo.vessel.model.Vessel> articWebVesselsAsList = this.vesselRepository.getAll(dk.dma.embryo.vessel.model.Vessel.class);
        logger.info("Repository returns " + articWebVesselsAsList.size() + " vessels from the database.");
        return articWebVesselsAsList;
    }
    
    
    public Map<Long, dk.dma.embryo.vessel.model.Vessel> updateArcticWebVesselInDatabase(
            List<Vessel> aisVessels,
            List<dk.dma.embryo.vessel.model.Vessel> articWebVesselsAsList) {

        Map<Long, dk.dma.embryo.vessel.model.Vessel> awVesselsAsMap = AisClientHelper.mapifyVessels(articWebVesselsAsList);

        for (Vessel aisVessel : aisVessels) {
            Long mmsi = aisVessel.getMmsi();
            String name = aisVessel.getName();
            String callSign = aisVessel.getCallsign();
            Long imo = aisVessel.getImoNo();

            if (mmsi != null) {
                dk.dma.embryo.vessel.model.Vessel vessel = awVesselsAsMap.get(mmsi);

                if (vessel != null && name != null && callSign != null) {
                    if (!isUpToDate(vessel.getAisData(), name, callSign, imo)) {
                        vessel.getAisData().setCallsign(callSign);
                        vessel.getAisData().setImoNo(imo);
                        vessel.getAisData().setName(name);
                        logger.debug("Updating vessel {}/{}", mmsi, name);
                        vesselRepository.saveEntity(vessel);
                    } else {
                        logger.debug("Vessel {}/{} is up to date", mmsi, name);
                    }
                }
            }
        }

        return awVesselsAsMap;
    }

    private boolean isUpToDate(dk.dma.embryo.vessel.model.AisData aisData, String name, String callSign, Long imo) {
        return ObjectUtils.equals(aisData.getName(), name) && ObjectUtils.equals(aisData.getCallsign(), callSign)
                && ObjectUtils.equals(aisData.getImoNo(), imo);
    }
    
    
    
    public boolean isWithinAisCircle(double longitude, double latitude) {
        return 
                Position.create(latitude, longitude).distanceTo(
                        Position.create(aisCircleLatitude, aisCircleLongitude), 
                        CoordinateSystem.GEODETIC) < aisCircleRadius;
    }
    
    public boolean isAllowed(double latitude) {
        return latitude > aisDataLimitLatitude;
    }
    
    public Vessel getAisVesselByMmsi(Long mmsi) {
        if(this.vesselsAllowed != null && !this.vesselsAllowed.isEmpty()) {
            for (Vessel aisVessel : this.vesselsAllowed) {
                if(aisVessel.getMmsi().longValue() == mmsi.longValue()) {
                    return aisVessel;
                }
            }
        }
        return null;
    }
   
    
    public List<Vessel> getVesselsAllowed() {
        return new ArrayList<>(vesselsAllowed);
    }
    public void setVesselsAllowed(List<Vessel> vesselsAllowed) {
        this.vesselsAllowed = vesselsAllowed;
    }

    public List<Vessel> getVesselsOnMap() {
        return new ArrayList<>(vesselsOnMap);
    }
    public void setVesselsOnMap(List<Vessel> vessels) {
        this.vesselsOnMap = vessels;
    }

    public void setVesselsInAisCircle(List<Vessel> vesselsInAisCircle) {
        this.vesselsInAisCircle = vesselsInAisCircle;
    }
    public List<Vessel> getVesselsInAisCircle() {
        return new ArrayList<>(vesselsInAisCircle);
    }

    public Map<Long, Double> getMaxSpeeds() {
        return new HashMap<>(maxSpeeds);
    }
    public void setMaxSpeeds(Map<Long, Double> maxSpeeds) {
        this.maxSpeeds = maxSpeeds;
    }
}
