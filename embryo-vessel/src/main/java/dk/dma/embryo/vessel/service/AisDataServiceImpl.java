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
package dk.dma.embryo.vessel.service;

import dk.dma.embryo.common.area.AreaFilter;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.vessel.integration.AisSourceFilter;
import dk.dma.embryo.vessel.integration.AisStoreClient;
import dk.dma.embryo.vessel.integration.AisTrackClient;
import dk.dma.embryo.vessel.integration.AisTrackClient.AisTrack;
import dk.dma.embryo.vessel.integration.AisTrackRequestParamBuilder;
import dk.dma.embryo.vessel.integration.AisVessel;
import dk.dma.embryo.vessel.json.TrackPos;
import dk.dma.embryo.vessel.model.Vessel;
import dk.dma.embryo.vessel.persistence.VesselDao;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.dma.embryo.vessel.integration.AisStoreClient.TrackPosition;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class AisDataServiceImpl implements AisDataService {

    @Inject
    private Logger logger;

    @Inject
    @Property("embryo.aisDataLimit.latitude")
    private double aisDataLimitLatitude;

    @Inject
    @Property("embryo.ais.filters.baseArea")
    private String baseAreaFilter;

    @Inject
    @Property("embryo.ais.filters.defaultSources")
    private String defaultSources;

    @Inject
    @Property("embryo.ais.filters.defaultArea")
    private String defaultArea;

    @Inject
    private VesselDao vesselRepository;

    @Inject
    private AreaFilter areaFilter;

    @Inject
    private AisSourceFilter aisSourceFilter;

    @Inject
    private AisTrackClient aisTraclClient;

    @Inject
    private AisStoreClient aisStoreClient;


    @Inject
    EmbryoLogService embryoLogService;

    public List<AisVessel> getAisVessels() {
        AisTrackRequestParamBuilder fb = null;
        try {
            final List<Vessel> arcticWebVessels = vesselRepository.getAll(Vessel.class);

            fb = new AisTrackRequestParamBuilder();
            fb.includeVessels(arcticWebVessels).setBaseArea(baseAreaFilter);
            fb.setSourceFilter(defaultSources, aisSourceFilter).addUserSelectedAreas(areaFilter).setDefaultArea(defaultArea);

            logger.trace("AisTrackClient.vessels({}, {}, {},{})", fb.getMmsiNumbers(), fb.getBaseArea(), fb.getUserSelectedAreas(), fb.getSourceFilter());
            List<AisTrack> aisTracks = aisTraclClient.vessels(fb.getMmsiNumbers(), fb.getBaseArea(), fb.getUserSelectedAreas(), fb.getSourceFilter());
            logger.trace("AisTrackClient.vessels({}, {}, {}, {}) : {}", fb.getMmsiNumbers(), fb.getBaseArea(), fb.getUserSelectedAreas(), fb.getSourceFilter(), aisTracks);

            Function<AisVessel, AisVessel> addMaxSpeedFunc = AisVessel.addMaxSpeedFn(Vessel.asMap(arcticWebVessels));
            Stream<AisVessel> vesselStream = aisTracks.stream().filter(AisTrack.valid()).map(AisTrack.toJsonVesselFn()).map(addMaxSpeedFunc);
            List<AisVessel> vessels = vesselStream.collect(Collectors.toList());

            String msg = "Fetched " + vessels.size() + " vessels using request parameters " + fb.requestValuesAsString();
            logger.info(msg);
            embryoLogService.info(msg);
            return vessels;
        } catch (Exception e) {
            String msg = "Error fetching vessels using request parameters " + (fb == null ? "null" : fb.requestValuesAsString());
            logger.error(msg, e);
            embryoLogService.error(msg, e);
            throw e;
        }
    }

    /**
     * Method to retrieve a AIS information for specific vessels from the AIS server based on a list of MMSI numbers.
     *
     * This method is NOT subject to surveillance, as it is expected invoked by the job AisReplicatorJob which is under surveillance.
     *
     * @param mmsiNumbers
     * @return
     */
    public List<AisVessel> getAisVesselsByMmsi(List<Long> mmsiNumbers){
        AisTrackRequestParamBuilder fb = null;
        try {
            fb = new AisTrackRequestParamBuilder();
            fb.setSourceFilter(defaultSources, null);

            logger.trace("AisTrackClient.vessels({}, {})", mmsiNumbers, fb.getSourceFilter());
            List<AisTrack> aisTracks = aisTraclClient.vesselsByMmsis(mmsiNumbers, fb.getSourceFilter());
            logger.trace("AisTrackClient.vessels({}, {}) : {}", mmsiNumbers, fb.getSourceFilter(), aisTracks);

            Stream<AisVessel> vesselStream = aisTracks.stream().filter(AisTrack.valid()).map(AisTrack.toJsonVesselFn());
            List<AisVessel> vessels = vesselStream.collect(Collectors.toList());

            logger.debug("Fetched {} vessels using request parameters {}", vessels.size(), fb.requestValuesAsString() );
            return vessels;
        } catch (Exception e) {
            String msg = "Error fetching vessels using request parameters " + (fb == null ? "null" : fb.requestValuesAsString());
            logger.error(msg, e);
            throw e;
        }
    }

    /**
     * Method to retrieve a AIS information for a specific vessels from the AIS server based on a MMSI number.
     *
     * This method IS subject to surveillance.
     *
     * @param mmsi
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public AisVessel getAisVesselByMmsi(Long mmsi) {
        AisTrackRequestParamBuilder fb = null;
        try {
            fb = new AisTrackRequestParamBuilder();
            fb.setSourceFilter(defaultSources, aisSourceFilter);

            logger.trace("AisTrackClient.vessel({}, {})", mmsi, fb.getSourceFilter());
            AisTrack aisTrack = aisTraclClient.vessel(mmsi, fb.getSourceFilter());
            logger.trace("AisTrackClient.vessel({" +
                    "}, {}) : {}", mmsi, fb.getSourceFilter(), aisTrack);

            AisVessel aisVessel = aisTrack.toJsonVessel();

            String msg = "Fetched AIS track info for vessel with mmsi=" + mmsi + ", name=" + aisVessel.getName();
            logger.info(msg);
            embryoLogService.info(msg);
            return aisVessel;
        } catch (Exception e) {
            String msg = "Error fetching AIS track info for vessel with mmsi=" + mmsi + " using source filter " + (fb == null ? "null" : fb.requestValuesAsString());
            logger.error(msg, e);
            embryoLogService.error(msg, e);
            throw e;
        }
    }

    /**
     * Determines if it is allowed to display historical tracks for the AisVessel in question
     *
     * This method is NOT subject to surveillance.
     *
     * @param aisVessel
     * @return
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isHistoricalTrackAllowed(AisVessel aisVessel){
        boolean historicalTrack = false;

        if (aisVessel != null && aisVessel.getLat() != null && aisVessel.getLon() != null) {
            historicalTrack = aisVessel.getLat() > aisDataLimitLatitude;
        }

        return historicalTrack;
    }

    /**
     * Method to retrieve a AIS information for a specific vessels from the AIS server based on a MMSI number.
     * <p/>
     * This method IS subject to surveillance.
     *
     * @param mmsi
     * @return
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<TrackPos> historicalTrack(Long mmsi) {

        // TODO prevent call outside base area

        AisTrackRequestParamBuilder fb = null;
        try {
            fb = new AisTrackRequestParamBuilder();
            fb.setSourceFilter(defaultSources, aisSourceFilter);

            String duration = AisStoreClient.LOOK_BACK_PT24H;
            logger.trace("AisStoreClient.historicalTrack({}, {}, {})", mmsi, fb.getSourceFilter(), duration);
            List<TrackPosition> historicalTrack = aisStoreClient.pastTrack(mmsi, fb.getSourceFilter(), duration);
            logger.trace("AisStoreClient.historicalTrack({}, {}, {}) : {}", mmsi, fb.getSourceFilter(), duration, historicalTrack);

            List<TrackPosition> downSampled = TrackPosition.downSample(historicalTrack, 500);

            List<TrackPos> result = downSampled.stream().map(track -> track.toTrackPos()).collect(Collectors.toList());

            String msg = "Fetched historical track for vessel with mmsi=" + mmsi;
            logger.info(msg);
            embryoLogService.info(msg);
            return result;
        } catch (Exception e) {
            String msg = "Error fetching historical track for vessel with mmsi=" + mmsi + " using source filter " + (fb == null ? "null" : fb.getSourceFilter());
            logger.error(msg, e);
            embryoLogService.error(msg, e);
            throw e;
        }
    }
}
