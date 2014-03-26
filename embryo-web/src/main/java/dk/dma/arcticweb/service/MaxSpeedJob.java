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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.jboss.resteasy.client.ClientResponseFailure;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.component.MaxSpeedExtractor;
import dk.dma.embryo.domain.AdministratorRole;
import dk.dma.embryo.restclients.LimitedAisViewService;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.authorization.Roles;

@Singleton
@Startup
public class MaxSpeedJob {
    @Inject
    private AisDataService aisDataService;

    @Inject
    private LimitedAisViewService limitedAisView;

    @Resource
    private TimerService service;

    @Property(value = "embryo.vessel.maxspeedjob.enabled")
    @Inject
    private String enabled;

    @Inject
    @Property("embryo.vessel.maxspeedjob.cron")
    private ScheduleExpression cron;

    @Inject
    @Property("embryo.vessel.maxspeedjob.updatefrequency")
    private Integer updateFrequenceMinutes;

    @Inject
    @Property("embryo.vessel.maxspeedjob.lastrecordedlimit")
    private Integer lastRecordedLimit;

    @Inject
    private Logger logger;

    @Inject
    private EmbryoLogService embryoLogService;

    public MaxSpeedJob() {
    }

    @PostConstruct
    public void startTimer() {
        logger.info("Setting up Max Speed job");

        if (enabled != null && "true".equals(enabled.trim().toLowerCase()) && cron != null) {
            TimerConfig config = new TimerConfig(null, false);
            service.createCalendarTimer(cron, config);
        } else {
            logger.info("Max Speed job not enabled");
        }
    }

    @Interceptors(value = AuthorizationChecker.class)
    @Roles(AdministratorRole.class)
    public void update() {
        updateMaxSpeeds();
    }

    private void logException(Long mmsi, String name, Exception e) {
        String msg = "Error updating max speed for vessel " + mmsi + "/" + name;
        logger.error(msg, e);
        embryoLogService.error(msg, e);
    }

    /**
     * Executes on startup
     */
    @Timeout
    void updateMaxSpeeds() {
        try {
            logger.debug("Update Max Speed Recordings");
            Map<Long, MaxSpeedRecording> oldRecordings = aisDataService.getMaxSpeeds();
            Map<Long, MaxSpeedRecording> newRecordings = new HashMap<>(1000);

            DateTime last = DateTime.now(DateTimeZone.UTC).minusDays(7);
            DateTime lastUpdatedLimit = DateTime.now(DateTimeZone.UTC).minusMinutes(updateFrequenceMinutes);

            List<String[]> vesselsInAisCircle = aisDataService.getVesselsInAisCircle();
            Set<Long> mmsiNumbers = new HashSet<Long>(1000);

            int errorCount = 0;
            int updateCount = 0;
            for (int i = 0; i < vesselsInAisCircle.size() && errorCount < 10; i++) {
                String[] vessel = vesselsInAisCircle.get(i);
                Long mmsi = asLong(vessel[6]);
                mmsiNumbers.add(mmsi);

                MaxSpeedRecording rec = oldRecordings.get(mmsi);
                if (rec == null || rec.getCreated().isBefore(lastUpdatedLimit)) {
                    try {
                        Map<String, Object> result = limitedAisView.vesselTargetDetails(mmsi, 1);
                        MaxSpeedRecording newRec = new MaxSpeedExtractor().extractMaxSpeed(result);
                        newRecordings.put(mmsi, newRec);
                        updateCount++;
                        logger.debug("Updated max speed for vessel {}/{}: {}", mmsi, vessel[7], newRec);
                    } catch (ClientResponseFailure e) {
                        if (e.getResponse() != null && e.getResponse().getStatus() == 400) {
                            newRecordings.put(mmsi, new MaxSpeedRecording(0.0));
                            updateCount++;
                            logger.debug("Failed fetching track for vessel {}/{}: {}. It may be out of AIS circle",
                                    mmsi, vessel[7], e.getMessage());
                        } else {
                            logException(mmsi, vessel[7], e);
                            errorCount++;
                        }
                    } catch (Exception e) {
                        logException(mmsi, vessel[7], e);
                        errorCount++;
                    }
                } else {
                    logger.debug("Reuse of max speed for vessel {}: {}", mmsi, rec);
                    newRecordings.put(mmsi, rec);
                }
            }

            // Vessels not in AIS circle may have disappeared temporarily because their AIS position has been
            // transmitted with errors or because their AIS equipment is not working. Keep their maximum recorded speed
            // for a time period.
            Set<Long> notInAisCircle = oldRecordings.keySet();
            notInAisCircle.removeAll(mmsiNumbers);
            int maintainCount = 0;
            for (Long mmsi : notInAisCircle) {
                MaxSpeedRecording rec = oldRecordings.get(mmsi);
                if (rec.getCreated().isAfter(last)) {
                    maintainCount++;
                    logger.debug("Maintain max speed for vessel {}: {}", mmsi, rec);
                    newRecordings.put(mmsi, rec);
                }
            }

            aisDataService.setMaxSpeeds(newRecordings);

            logger.info("Updated max speed recordings. Updated: {}. Maintained {}: Total count: {}, Errors: {}",
                    updateCount, maintainCount, newRecordings.size(), errorCount);
            if (errorCount > 0) {
                embryoLogService.error("Some max speeds could not be updated. Error count: " + errorCount);
            } else {
                embryoLogService.info("Max speeds recorded. Total count: " + newRecordings.size());
            }
        } catch (Exception e) {
            logger.error("MaxSpeedJob failed", e);
            embryoLogService.error("" + e, e);
        }
    }

    private Long asLong(String value) {
        return value == null || value.trim().length() == 0 || value.trim().toUpperCase().equals("N/A") ? null : Long
                .valueOf(value);
    }

    public static class MaxSpeedRecording {
        private double maxSpeed;
        private DateTime created;

        public MaxSpeedRecording(double maxSpeed) {
            this.maxSpeed = maxSpeed;
            this.created = DateTime.now(DateTimeZone.UTC);
        }

        public double getMaxSpeed() {
            return maxSpeed;
        }

        public DateTime getCreated() {
            return created;
        }

        @Override
        public String toString() {
            return "MaxSpeedRecording [maxSpeed=" + maxSpeed + ", created=" + created + "]";
        }
    }
}
