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

package dk.dma.embryo.vessel.component;

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;
import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.persistence.GeographicDao;
import dk.dma.embryo.vessel.persistence.ScheduleDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by jesper on 8/14/14.
 */
@Named
public class ScheduleUploadPostProcessor {

    @Inject
    ScheduleDao scheduleDao;

    @Inject
    GeographicDao geographicDao;

    public ScheduleResponse validate(ScheduleResponse schedule, Long mmsi, Date lastDeparture) {
        if (schedule.getVoyages() == null || schedule.getVoyages().length == 0) {
            schedule.setErrors(new String[]{"No voyages found in document."});
            return schedule;
        }
        VoyageFilter voyageFilter = new VoyageFilter(scheduleDao, mmsi);
        DateValidator dateValidator = new DateValidator(lastDeparture);
        LocationFinder locationFinder = new LocationFinder(geographicDao);

        List<Voyage> voyages = new ArrayList<>(schedule.getVoyages().length);

        for (Voyage voyage : schedule.getVoyages()) {
            if (voyageFilter.test(voyage)) {
                voyage = dateValidator.apply(voyage);
                voyage = locationFinder.apply(voyage);
                voyages.add(voyage);
            }
        }
        List<String> errors = new ArrayList<>();
        errors.addAll(voyageFilter.getIdErrors());
        if (dateValidator.getDepartureErrors() > 0) {
            errors.add(dateValidator.getDepartureErrors() + " departure dates were before last existing departure date, please enter new departure dates.");
        }
        if (dateValidator.getArrivalErrors() > 0) {
            errors.add(dateValidator.getArrivalErrors() + " arrival dates were before last existing departure date, please enter new arrival dates.");
        }
        if (locationFinder.getLocationErrors() > 0) {
            errors.add(locationFinder.getLocationErrors() + " locations could not be found, please add them manually.");
        }
        schedule.setErrors(errors.toArray(new String[0]));
        schedule.setVoyages(voyages.size() == 0 ? null : voyages.toArray(new Voyage[0]));

        return schedule;
    }

    static class VoyageFilter implements Predicate<Voyage> {
        private final List<String> idErrors = new ArrayList<>();
        private final List<String> idsUsedByOtherVessels = new ArrayList<>();
        private final Set<String> ids = new HashSet<>();
        private final ScheduleDao scheduleDao;
        private final Long requiredMmsi;

        public VoyageFilter(final ScheduleDao scheduleDao, final Long requiredMmsi) {
            this.scheduleDao = scheduleDao;
            this.requiredMmsi = requiredMmsi;
        }

        @Override
        public boolean test(Voyage voyage) {
            if (voyage.getMaritimeId() != null) {
                if (ids.contains(voyage.getMaritimeId())) {
                    String str = "Id " + voyage.getMaritimeId() + " discovered more than once. Only using data for first occurrence.";
                    if (!idErrors.contains(str)) {
                        idErrors.add(str);
                    }
                    return false;
                } else {
                    dk.dma.embryo.vessel.model.Voyage v = scheduleDao.getVoyageByEnavId(voyage.getMaritimeId());
                    if (v != null && !requiredMmsi.equals(v.getVessel().getMmsi())) {
                        if(!idsUsedByOtherVessels.contains(voyage.getMaritimeId())){
                            idsUsedByOtherVessels.add(voyage.getMaritimeId());
                        }
                        return false;
                    }
                }
                ids.add(voyage.getMaritimeId());
            }
            return true;
        }

        public List<String> getIdErrors() {
            if(idsUsedByOtherVessels.size() ==  0){
                return idErrors;
            }

            List<String> result = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for(String id : idsUsedByOtherVessels){
                if(builder.length() == 0){
                    builder.append("Please assign unique Id values in Excel before uploading. The following id(s) are used in schedule(s) for other vessel(s): ");
                }else {
                    builder.append(", ");
                }
                builder.append(id);
            }
            result.add(builder.toString());
            result.addAll(idErrors);
            return result;
        }
    }

    private static class CachedPosition {
        private Position position;
        private boolean notFound;
    }

    static class DateValidator implements Function<Voyage, Voyage> {
        private final Date lastDeparture;
        private int departureErrors;
        private int arrivalErrors;

        public DateValidator(final Date lastDeparture) {
            this.lastDeparture = lastDeparture;
        }

        @Override
        public Voyage apply(Voyage voyage) {
            if (voyage.getMaritimeId() == null) {
                if (lastDeparture.getTime() > voyage.getArrival().getTime()) {
                    arrivalErrors++;
                    voyage.setArrival(null);
                }
                if (lastDeparture.getTime() > voyage.getDeparture().getTime()) {
                    departureErrors++;
                    voyage.setDeparture(null);
                }
            }
            return voyage;
        }

        public int getDepartureErrors() {
            return this.departureErrors;
        }

        public int getArrivalErrors() {
            return arrivalErrors;
        }
    }

    static class LocationFinder implements Function<Voyage, Voyage> {
        private final Map<String, CachedPosition> berthCache = new HashMap<String, CachedPosition>();
        private final GeographicDao geoDao;
        private int locationErrors;

        public LocationFinder(final GeographicDao geoDao) {
            this.geoDao = geoDao;
        }

        @Override
        public Voyage apply(Voyage voyage) {
            CachedPosition cp = berthCache.get(voyage.getLocation());
            if (cp == null) {
                List<Berth> berthList = geoDao.lookup(voyage.getLocation());

                if (berthList.size() == 0) {
                    // exact name/alis match gave nothing. Trying more loose query
                    berthList = geoDao.findBerths(voyage.getLocation());
                }

                cp = new CachedPosition();
                if (berthList.size() != 1) {
                    cp.notFound = true;
                } else {
                    cp.position = berthList.get(0).getPosition();
                }
                berthCache.put(voyage.getLocation(), cp);
            }
            Double lat = null, lon = null;
            if (cp.notFound) {
                locationErrors++;
            } else {
                voyage.setLatitude(cp.position.getLatitude());
                voyage.setLongitude(cp.position.getLongitude());
            }
            return voyage;
        }

        public int getLocationErrors() {
            return locationErrors;
        }
    }

}
