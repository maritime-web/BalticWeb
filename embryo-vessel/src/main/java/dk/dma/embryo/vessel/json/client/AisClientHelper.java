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
package dk.dma.embryo.vessel.json.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import dk.dma.embryo.vessel.job.MaxSpeedByShipTypeMapper;
import dk.dma.embryo.vessel.job.ShipTypeCargo.ShipType;
import dk.dma.embryo.vessel.job.ShipTypeMapper;
import dk.dma.embryo.vessel.json.VesselOverview;
import dk.dma.embryo.vessel.json.client.AisRestDataService.AisVessel;
import dk.dma.embryo.vessel.json.client.Vessel.MaxSpeedOrigin;

public class AisClientHelper {

    public static final boolean WITH_EXACT_EARTH = true;
    public static final boolean WITHOUT_EXACT_EARTH = false;
    
    public static final String getSourceFilter(boolean withExactEarth) {
        
        String regionsToExclude = "";
        if(withExactEarth) {
            regionsToExclude = stringsToCommaSeparatedList(AisRestDataService.REGION_ORBCOMM);
        } else {
            regionsToExclude = stringsToCommaSeparatedList(AisRestDataService.REGION_EXACTEARTH, AisRestDataService.REGION_ORBCOMM);
        }
        
        return String.format(AisRestDataService.SOURCE_FILTER_EXCLUDE_FORMAT, regionsToExclude);
    }
    
    public static final String getMmsiAsSeparatedList(List<dk.dma.embryo.vessel.model.Vessel> arcticWebVessels) {
        
        return arcticWebVessels.stream().map(vessel -> vessel.getMmsi().toString()).collect(Collectors.joining(","));
    }
    
    public static List<Vessel> mapAisVesselsToVessels(List<AisVessel> aisVessels) {
        
        List<Vessel> vessels = new ArrayList<Vessel>();
        for (AisVessel aisVessel : aisVessels) {
            
            Vessel vessel = aisVessel.mapToRestVesselType();
            
            if(vessel != null) {
                vessels.add(vessel);
            }
        }
        return vessels;
    }
    
    public static void updateMaxSpeedForVessels(List<Vessel> mergedVesselsWithExactEarth, Map<Long, dk.dma.embryo.vessel.model.Vessel> arcticWebVesselAsMap) {
        
        for (Vessel vessel : mergedVesselsWithExactEarth) {
            
            setMaxSpeedOnAisVessel(vessel, arcticWebVesselAsMap.get(vessel.getMmsi()));
        }
    }
    
    public static Map<Long, dk.dma.embryo.vessel.model.Vessel> mapifyVessels(List<dk.dma.embryo.vessel.model.Vessel> articWebVesselsAsList) {

        Map<Long, dk.dma.embryo.vessel.model.Vessel> awVesselsAsMap = new HashMap<>();

        for (dk.dma.embryo.vessel.model.Vessel v : articWebVesselsAsList) {
            awVesselsAsMap.put(v.getMmsi(), v);
        }

        return awVesselsAsMap;
    }

    public static List<Vessel> mergeAisVesselsWithArcticVessels(List<Vessel> aisAllAllowed, List<Vessel> arcticWebVessels) {

        // The 2 List's are merged and duplicates are removed
        List<Vessel> mergedVessels = Stream.concat(aisAllAllowed.stream(), arcticWebVessels.stream()).distinct().collect(Collectors.toList());
        
        return mergedVessels;
    }
    
    public static List<VesselOverview> mapAisVessels(List<Vessel> vessels) {

        List<VesselOverview> vesselOverviewsResponse = new ArrayList<VesselOverview>();

        for (Vessel vessel : vessels) {

            VesselOverview vesselOverview = new VesselOverview();
            Long mmsi = vessel.getMmsi();

            vesselOverview.setX(vessel.getLon());
            vesselOverview.setY(vessel.getLat());
            vesselOverview.setAngle(vessel.getCog() != null ? vessel.getCog() : 0);
            vesselOverview.setMmsi(mmsi);
            vesselOverview.setName(vessel.getName());
            vesselOverview.setCallSign(vessel.getCallsign());
            vesselOverview.setMoored(vessel.getMoored() != null ? vessel.getMoored() : false);

            ShipType shipTypeFromSubType = ShipType.getShipTypeFromSubType(vessel.getVesselType());
            String type = ShipTypeMapper.getInstance().getColor(shipTypeFromSubType).ordinal() + "";
            vesselOverview.setType(type);

            vesselOverview.setInAW(false);
            
            mapMaxSpeed(vessel.getMaxSpeed(), vessel.getMaxSpeedOrigin(), vesselOverview);
            
            vesselOverviewsResponse.add(vesselOverview);
        }

        return vesselOverviewsResponse;
    }
    
    private static void mapMaxSpeed(Double maxSpeed, MaxSpeedOrigin maxSpeedOrigin, VesselOverview vesselOverview) {

        if(maxSpeedOrigin == MaxSpeedOrigin.AW) {
            vesselOverview.setAwsog(maxSpeed);
        } else if (maxSpeedOrigin == MaxSpeedOrigin.TABLE) {
            vesselOverview.setSsog(maxSpeed);
        } else if (maxSpeedOrigin == MaxSpeedOrigin.SOG) {
            vesselOverview.setSog(maxSpeed);
        } 
    }
    
    public static void setMaxSpeedOnAisVessel(Vessel aisVessel, dk.dma.embryo.vessel.model.Vessel awVesselFromDatabase) {
        
        boolean isMaxSpeedSetOnAisVessel = false;
        
        // If exists set Max Speed from ArcticWeb vessel in the database
        if(awVesselFromDatabase != null && awVesselFromDatabase.getMaxSpeed() != null && awVesselFromDatabase.getMaxSpeed().doubleValue() > 0) {
            aisVessel.setMaxSpeed(awVesselFromDatabase.getMaxSpeed().doubleValue());
            aisVessel.setMaxSpeedOrigin(Vessel.MaxSpeedOrigin.AW);
            isMaxSpeedSetOnAisVessel = true;
        }
        
        // If not already set from ArcticWeb vessel in database -> set it vessel type
        if(!isMaxSpeedSetOnAisVessel && aisVessel.getVesselType() != null) {
            Double maxSpeedByVesselType = MaxSpeedByShipTypeMapper.mapAisShipTypeToMaxSpeed(aisVessel.getVesselType());
            if(maxSpeedByVesselType > 0.0) {
                aisVessel.setMaxSpeed(maxSpeedByVesselType);
                aisVessel.setMaxSpeedOrigin(Vessel.MaxSpeedOrigin.TABLE);
                isMaxSpeedSetOnAisVessel = true;
            }
        }
        
        // If not already set from ArcticWeb vessel in database or from vessel type -> set sog 
        if(!isMaxSpeedSetOnAisVessel && aisVessel.getSog() != null) {
            aisVessel.setMaxSpeed(aisVessel.getSog());
            aisVessel.setMaxSpeedOrigin(Vessel.MaxSpeedOrigin.SOG);
            isMaxSpeedSetOnAisVessel = true;
        }
        
        // Fallback - set 0.0
        if(!isMaxSpeedSetOnAisVessel) {
            aisVessel.setMaxSpeed(0.0);
            aisVessel.setMaxSpeedOrigin(Vessel.MaxSpeedOrigin.DEFAULT);
        }
    }

    private static String stringsToCommaSeparatedList(String... strings) {
        
        return StringUtils.join(strings, ",");
    }
    
}
