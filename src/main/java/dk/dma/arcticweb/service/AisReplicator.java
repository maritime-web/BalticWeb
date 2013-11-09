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
package dk.dma.arcticweb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.dataformats.shapefile.ProjectionFileParser;
import dk.dma.embryo.restclients.FullAisViewService;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.VesselDao;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.AisData;
import dk.dma.embryo.domain.Vessel;
import dk.dma.embryo.restclients.AisViewService;
import dk.dma.embryo.restclients.AisViewService.VesselListResult;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AisReplicator implements AisReplicatorService {

    private List<String[]> vesselsInArcticCircle = new ArrayList<>();

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private FullAisViewService aisView;

    @Resource
    private TimerService service;

    @Property(value = "embryo.vessel.aisjob.enabled")
    @Inject
    private String enabled;

    @Inject
    @Property("embryo.vessel.aisjob.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.aisCircle.latitude")
    private double aisCircleLatitude;

    @Inject
    @Property("embryo.aisCircle.longitude")
    private double aisCircleLongitude;

    @Inject
    @Property("embryo.aisCircle.radius")
    private double aisCircleRadius;

    @Inject
    private Logger logger;

    public AisReplicator() {
    }

    public AisReplicator(VesselDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    @PostConstruct
    public void startTimer() {
        logger.info("Setting up ais replication job");

        if (enabled != null && "true".equals(enabled.trim().toLowerCase()) && cron != null) {
            TimerConfig config = new TimerConfig(null, false);
            service.createCalendarTimer(cron, config);
        } else {
            logger.info("Ais replication job not enabled");
        }
    }

    /**
     * Executes on startup
     */
    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    void updateAis() {
        logger.debug("UPDATE AIS VESSEL DATA");

        VesselListResult result = aisView.vesselList(0);

        List<Vessel> awVesselsAsList = vesselRepository.getAll(Vessel.class);

        logger.debug("aisView returns "+result.getVesselList().getVessels().size()+" items - " +
                "repository returns "+awVesselsAsList.size()+" items.");

        Map<Long, Vessel> awVesselsAsMap = new HashMap<>();

        for (Vessel v : awVesselsAsList) {
            awVesselsAsMap.put(v.getMmsi(), v);
        }

        for (Entry<String, String[]> aisVessel : result.getVesselList().getVessels().entrySet()) {
            Long mmsi = asLong(aisVessel.getValue()[6]);
            String name = asString(aisVessel.getValue()[7]);
            String callSign = asString(aisVessel.getValue()[8]);
            Long imo = asLong(aisVessel.getValue()[9]);

            if (mmsi != null) {
                Vessel vessel = awVesselsAsMap.get(mmsi);

                if (vessel != null && name != null && callSign != null) {
                    if (!isUpToDate(vessel.getAisData(), name, callSign, imo)) {
                        vessel.getAisData().setCallsign(callSign);
                        vessel.getAisData().setImoNo(imo);
                        vessel.getAisData().setName(name);
                        logger.debug("Updating vessel {}/{}", mmsi, name);
                        saveVessel(vessel);
                    } else {
                        logger.debug("Vessel {}/{} is up to date", mmsi, name);
                    }
                }
            }
        }

        vesselsInArcticCircle = new ArrayList<>();

        for (String[] aisVessel : result.getVesselList().getVessels().values()) {
            double x = Double.parseDouble(aisVessel[2]);
            double y = Double.parseDouble(aisVessel[1]);

            Long mmsi = asLong(aisVessel[6]);

            if (isWithinAisCircle(x, y) ||
                    awVesselsAsMap.containsKey(mmsi)) {
                vesselsInArcticCircle.add(aisVessel);
            }
        }

        logger.debug("Vessels in arctic circle: "+vesselsInArcticCircle.size());
    }

    public boolean isWithinAisCircle(double x, double y) {
        return Position.create(y, x).distanceTo(Position.create(aisCircleLatitude, aisCircleLongitude), CoordinateSystem.GEODETIC) < aisCircleRadius;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void saveVessel(Vessel vessel) {
        vesselRepository.saveEntity(vessel);
    }

    private boolean isUpToDate(AisData aisData, String name, String callSign, Long imo) {
        return ObjectUtils.equals(aisData.getName(), name) && ObjectUtils.equals(aisData.getCallsign(), callSign)
                && ObjectUtils.equals(aisData.getImoNo(), imo);
    }

    private Long asLong(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : Long
                .valueOf(value);
    }

    private String asString(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : value;
    }

    public List<String[]> getVesselsInArcticCircle() {
        return vesselsInArcticCircle;
    }
}
