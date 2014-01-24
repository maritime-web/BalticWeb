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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dk.dma.arcticweb.dao.ShapeFileMeasurementDao;
import dk.dma.embryo.domain.IceObservation;
import dk.dma.embryo.domain.ShapeFileMeasurement;
import dk.dma.embryo.security.AuthorizationChecker;
import dk.dma.embryo.security.authorization.RolesAllowAll;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Interceptors(value = AuthorizationChecker.class)
public class IceObservationServiceImpl implements IceObservationService {

    @Inject
    ShapeFileMeasurementDao shapeFileMeasurementDao;

    private static final Map<String, String> sources = new HashMap<>();
    static {
        sources.put("dmi.", "DMI");
        sources.put("aari.", "AARI");
    }

    @RolesAllowAll
    public List<IceObservation> listAvailableIceObservations() {
        List<IceObservation> iceObservations = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZone(DateTimeZone.UTC);

        for (ShapeFileMeasurement sfm : shapeFileMeasurementDao.getAll(ShapeFileMeasurement.class)) {
            Date date = formatter.parseDateTime(sfm.getFileName().substring(0, 12)).toDate();
            String region = sfm.getFileName().substring(13);

            switch (region) {
            case "CapeFarewell_RIC":
                region = "Cape Farewell";
                break;
            case "CentralWest_RIC":
                region = "Central West";
                break;
            case "Greenland_WA":
                region = "Greenland Overview";
                break;
            case "NorthEast_RIC":
                region = "North East";
                break;
            case "NorthWest_RIC":
                region = "North West";
                break;
            case "Qaanaaq_RIC":
                region = "Qaanaaq";
                break;
            case "SouthEast_RIC":
                region = "South East";
                break;
            case "SouthWest_RIC":
                region = "South West";
                break;
            case "aari_arc":
                region = "All Arctic";
                break;
            }

            if (System.currentTimeMillis() - date.getTime() < 3600 * 1000L * 24 * 30) {
                iceObservations.add(new IceObservation(sources.get(sfm.getPrefix()), region, date, sfm.getFileSize(),
                        sfm.getPrefix() + sfm.getFileName()));
            }
        }

        return iceObservations;
    }
}
