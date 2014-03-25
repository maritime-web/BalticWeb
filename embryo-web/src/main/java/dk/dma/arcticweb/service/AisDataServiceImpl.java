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

import javax.ejb.Singleton;
import javax.inject.Inject;

import dk.dma.arcticweb.service.MaxSpeedJob.MaxSpeedRecording;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.security.authorization.RolesAllowAll;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;

@Singleton
public class AisDataServiceImpl implements AisDataService {
    private List<String[]> vesselsInAisCircle = new ArrayList<>();

    private List<String[]> vesselsOnMap = new ArrayList<>();
    
    private Map<Long, MaxSpeedRecording> maxSpeeds =  new HashMap<>();
    
    @Inject
    @Property("embryo.aisCircle.latitude")
    private double aisCircleLatitude;

    @Inject
    @Property("embryo.aisCircle.longitude")
    private double aisCircleLongitude;

    @Inject
    @Property("embryo.aisCircle.radius")
    private double aisCircleRadius;

    @RolesAllowAll
    public boolean isWithinAisCircle(double x, double y) {
        return Position.create(y, x).distanceTo(Position.create(aisCircleLatitude, aisCircleLongitude), CoordinateSystem.GEODETIC) < aisCircleRadius;
    }

    @RolesAllowAll
    public List<String[]> getVesselsOnMap() {
        return new ArrayList<>(vesselsOnMap);
    }
    
    @RolesAllowAll
    public Map<Long, MaxSpeedRecording> getMaxSpeeds() {
        return new HashMap<>(maxSpeeds);
    }

    public void setVesselsInAisCircle(List<String[]> vesselsInAisCircle) {
        this.vesselsInAisCircle = vesselsInAisCircle;
    }

    public void setVesselsOnMap(List<String[]> vessels) {
        this.vesselsOnMap = vessels;
    }
    
    public void setMaxSpeeds(Map<Long, MaxSpeedRecording> maxSpeeds) {
        this.maxSpeeds = maxSpeeds;
    }
    public List<String[]> getVesselsInAisCircle() {
        return new ArrayList<>(vesselsInAisCircle);
    }

}
