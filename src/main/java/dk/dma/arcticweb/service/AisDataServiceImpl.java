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
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AisDataServiceImpl implements AisDataService {
    private List<String[]> vesselsInAisCircle = new ArrayList<>();

    @Inject
    @Property("embryo.aisCircle.latitude")
    private double aisCircleLatitude;

    @Inject
    @Property("embryo.aisCircle.longitude")
    private double aisCircleLongitude;

    @Inject
    @Property("embryo.aisCircle.radius")
    private double aisCircleRadius;

    public List<String[]> getVesselsInAisCircle() {
        return vesselsInAisCircle;
    }

    public void setVesselsInAisCircle(List<String[]> vesselsInAisCircle) {
        this.vesselsInAisCircle = vesselsInAisCircle;
    }

    public boolean isWithinAisCircle(double x, double y) {
        return Position.create(y, x).distanceTo(Position.create(aisCircleLatitude, aisCircleLongitude), CoordinateSystem.GEODETIC) < aisCircleRadius;
    }
}
