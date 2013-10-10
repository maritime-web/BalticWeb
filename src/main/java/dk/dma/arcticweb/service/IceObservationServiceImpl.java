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

import dk.dma.configuration.Property;
import dk.dma.embryo.domain.IceObservation;

import javax.inject.Inject;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IceObservationServiceImpl implements IceObservationService {
    @Inject
    @Property(value = "embryo.iceMaps.localDmiDirectory", substituteSystemProperties = true)
    private String localDmiDirectory;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shp.xml", ".shx");

    private boolean isIceObservationFullyDownloaded(String name) {
        for (String suffix : requiredFilesInIceObservation) {
            if (!new File(localDmiDirectory + "/" + name + suffix).exists()) {
                return false;
            }
        }
        return true;
    }

    private Set<String> downloadedIceObservations() {
        Set<String> result = new HashSet<>();
        File[] files = new File(localDmiDirectory).listFiles();
        if (files != null) {
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().indexOf("."));
                if (isIceObservationFullyDownloaded(name)) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public List<IceObservation> listAvailableIceObservations() {
        List<IceObservation> iceObservations = new ArrayList<>();

        try {
            for (String name : downloadedIceObservations()) {
                Date date = new SimpleDateFormat("yyyyMMddHHmm").parse(name.substring(0, 12));
                String region = name.substring(13);

                switch (region) {
                    case "CapeFarewell_RIC":
                        region = "Cape Farewell";
                        break;
                    case "CentralWest_RIC":
                        region = "Central West";
                        break;
                    case "Greenland_WA":
                        region = "Greenland WA";
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
                }

                long size = new File(localDmiDirectory + "/" + name + ".shp").length();

                if ((System.currentTimeMillis() - date.getTime() < 3600 * 1000L * 24 * 30) && (!region.equals("Greenland WA"))) {
                    iceObservations.add(new IceObservation("DMI", region, date, size, name));
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return iceObservations;
    }
}
