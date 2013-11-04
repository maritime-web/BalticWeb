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
package dk.dma.embryo.rest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dk.dma.arcticweb.service.ScheduleService;
import dk.dma.embryo.domain.Schedule;
import dk.dma.embryo.domain.Voyage;
import dk.dma.embryo.rest.json.VoyageShort;
import dk.dma.embryo.rest.util.DateTimeConverter;
import dk.dma.embryo.rest.util.TypeaheadDatum;

@Path("/voyage")
public class VoyageRestService {

    @Inject
    private ScheduleService scheduleService;
    
    @Inject
    private Logger logger;

    public VoyageRestService() {
    }

//    @GET
//    @Path("/{mmsi}/current")
//    @Produces("application/json")
//    public dk.dma.embryo.rest.json.VoyagePlan getCurrentVoyagePlan(@PathParam("mmsi") Long mmsi) {
//        logger.trace("getCurrentVoyagePlan({})", mmsi);
//
//        Schedule schedule = scheduleService.getSchedule(mmsi);
//        dk.dma.embryo.rest.json.VoyagePlan result = null;
//        if (schedule != null) {
//            result = schedule.toJsonModel();
//        }
//
//        logger.debug("getCurrentVoyagePlan({}) : {}", mmsi, result);
//        return result;
//    }

    // TODO remove double. Also in method above
//    @GET
//    @Path("/{mmsi}/current")
//    @Produces("application/json")
//    public dk.dma.embryo.rest.json.VoyagePlan getSchedule(@PathParam("mmsi") Long mmsi) {
//        logger.trace("getCurrentVoyagePlan({})", mmsi);
//
//        Schedule schedule = scheduleService.getSchedule(mmsi);
//        dk.dma.embryo.rest.json.VoyagePlan result = null;
//        if (schedule != null) {
//            result = schedule.toJsonModel();
//        }
//
//        logger.debug("getCurrentVoyagePlan({}) : {}", mmsi, schedule);
//        return result;
//    }

    
    @GET
    @Path("/active/{mmsi}")
    @Produces("application/json")
    public dk.dma.embryo.rest.json.Voyage getActive(@PathParam("mmsi") Long mmsi) {
        logger.trace("getActive({})", mmsi);

        Voyage voyage = scheduleService.getActiveVoyage(""+mmsi);

        dk.dma.embryo.rest.json.Voyage result = null;

        if (voyage != null) {
            result = voyage.toJsonModel();
        }

        logger.debug("getActive({}) : {}", mmsi, result);
        return result;
    }

    @GET
    @Path("/typeahead/{mmsi}")
    @Produces("application/json")
    public List<VoyageDatum> getVoyageTypeahead(@PathParam("mmsi") Long mmsi) {
        logger.debug("getVoyages({})", mmsi);

        List<Voyage> voyages = scheduleService.getVoyages(mmsi);

        List<VoyageDatum> transformed = Lists.transform(voyages,
                new VoyageTransformerFunction(DateTimeConverter.getDateTimeConverter()));

        logger.debug("getVoyages({}) : {}", mmsi, transformed);
        return transformed;
    }

    @GET
    @Path("/short/{mmsi}")
    @Produces("application/json")
    public List<VoyageShort> getVoyagesShort(@PathParam("mmsi") Long mmsi) {
        logger.debug("getVoyagesShort({})", mmsi);

        List<Voyage> voyages = scheduleService.getVoyages(mmsi);

        List<VoyageShort> transformed = Lists.transform(voyages, new VoyageShortTransformerFunction());

        logger.debug("getVoyagesShort({}) : {}", mmsi, transformed);
        return transformed;
    }

    public static class VoyageDatum extends TypeaheadDatum {
        private String id;

        public VoyageDatum(String value, String[] tokens, String id) {
            super(value, tokens);
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "VoyageDatum [id=" + id + ", getValue()=" + getValue() + ", getTokens()="
                    + Arrays.toString(getTokens()) + "]";
        }

    }

    public static final class VoyageTransformerFunction implements Function<Voyage, VoyageDatum> {
        private final DateTimeConverter converter;

        public VoyageTransformerFunction(DateTimeConverter converter) {
            this.converter = converter;
        }

        private String value(final Voyage input) {
            String departure = converter.toString(input.getDeparture(), null);
            return input.getBerthName() + (departure == null ? "" : " (" + departure + ")");
        }

        private String[] tokens(final Voyage input) {
            return new String[] { input.getBerthName(), input.getEnavId() };
        }

        @Override
        public VoyageDatum apply(final Voyage input) {
            return new VoyageDatum(value(input), tokens(input), input.getEnavId());
        }
    }

    public static final class VoyageShortTransformerFunction implements Function<Voyage, VoyageShort> {
        @Override
        public VoyageShort apply(final Voyage input) {
            Date arrival = input.getArrival() == null ? null : input.getArrival().toDate();
            Date departure = input.getDeparture() == null ? null : input.getDeparture().toDate();

            return new VoyageShort(input.getEnavId(), input.getBerthName(), arrival, departure);
        }
    }

}
