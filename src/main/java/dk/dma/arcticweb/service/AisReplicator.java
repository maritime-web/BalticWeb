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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.ShipDao;
import dk.dma.configuration.Property;
import dk.dma.embryo.domain.AisData;
import dk.dma.embryo.domain.Ship;
import dk.dma.embryo.restclients.AisViewService;
import dk.dma.embryo.restclients.AisViewService.VesselListResult;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AisReplicator {

    @Inject
    private ShipDao vesselRepository;

    @Inject
    private AisViewService aisView;

    @Resource
    private TimerService service;

    @Property(value = "embryo.vessel.aisjob.enabled")
    @Inject
    private String enabled;

    @Property(value = "embryo.vessel.aisjob.minutes")
    @Inject
    private Integer minute;

    @Inject
    private Logger logger;

    public AisReplicator() {
    }

    public AisReplicator(ShipDao vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    @PostConstruct
    public void startTimer() {
        logger.info("Setting up ais replication job executing every {} minutes", minute);

        if (enabled != null && "true".equals(enabled.trim().toLowerCase())) {
            logger.info("Setting up ais replication job executing every {} minutes", minute);

            ScheduleExpression exp = new ScheduleExpression();
            exp.minute("*/" + minute).hour("*");
            service.createCalendarTimer(exp);

            updateAis();
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

        List<Long> mmsis = new ArrayList<>((int) result.getVesselList().getVesselCount());

        logger.debug("");

        for (Entry<String, String[]> vessel : result.getVesselList().getVessels().entrySet()) {
            String mmsi = vessel.getValue()[6];
            if (mmsi != null && mmsi.trim().length() > 0) {
                mmsis.add(Long.valueOf(vessel.getValue()[6]));
            }
        }

        Map<Long, Ship> vessels = vesselRepository.getVessels(mmsis);

        for (Entry<String, String[]> aisVessel : result.getVesselList().getVessels().entrySet()) {
            Long mmsi = asLong(aisVessel.getValue()[6]);
            String name = asString(aisVessel.getValue()[7]);
            String callSign = asString(aisVessel.getValue()[8]);
            Long imo = asLong(aisVessel.getValue()[9]);

            if (mmsi != null) {
                Ship vessel = vessels.get(mmsi);

                if (vessel != null) {
                    if (!isUpToDate(vessel.getAisData(), callSign, imo)) {
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

    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    void saveVessel(Ship vessel) {
        vesselRepository.saveEntity(vessel);
    }

    private boolean isUpToDate(AisData aisData, String callSign, Long imo) {
        return ObjectUtils.equals(aisData.getCallsign(), callSign) && ObjectUtils.equals(aisData.getImoNo(), imo);
    }

    private Long asLong(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : Long
                .valueOf(value);
    }

    private String asString(String value) {
        return value == null || value.trim().length() == 0 ? null : value;
    }
}
