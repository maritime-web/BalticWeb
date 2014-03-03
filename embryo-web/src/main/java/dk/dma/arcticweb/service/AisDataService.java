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

import java.util.List;
import java.util.Map;

import dk.dma.arcticweb.service.MaxSpeedJob.MaxSpeedRecording;

public interface AisDataService {
    List<String[]> getVesselsInAisCircle();

    void setVesselsInAisCircle(List<String[]> vesselsInArcticCircle);

    List<String[]> getVesselsOnMap();

    void setVesselsOnMap(List<String[]> vesselsInArcticCircle);

    boolean isWithinAisCircle(double x, double y);

    Map<Long, MaxSpeedRecording> getMaxSpeeds();

    void setMaxSpeeds(Map<Long, MaxSpeedRecording> maxSpeeds);

}
