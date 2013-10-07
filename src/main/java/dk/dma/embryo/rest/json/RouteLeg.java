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
package dk.dma.embryo.rest.json;

import dk.dma.enav.model.voyage.RouteLeg.Heading;

public class RouteLeg {

    /** Speed in knots. */
    private Double speed;

    /** Sail heading rhumb line or great circle */
    private Heading heading;

    public RouteLeg() {

    }

    public RouteLeg(Double speed, Heading heading) {
        super();
        this.speed = speed;
        this.heading = heading;
    }

    public Double getSpeed() {
        return speed;
    }

    public Heading getHeading() {
        return heading;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

}
