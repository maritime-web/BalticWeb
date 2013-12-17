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
package dk.dma.embryo.domain;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

public class IceObservation {
    private String source;
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime date;
    private String shapeFileName;
    private String region;
    private long size;

    public IceObservation(String source, String region, DateTime date, long size, String shapeFileName) {
        this.source = source;
        this.region = region;
        this.date = date;
        this.shapeFileName = shapeFileName;
        this.size = size;
    }

    public String getSource() {
        return source;
    }

    public DateTime getDate() {
        return date;
    }

    public String getShapeFileName() {
        return shapeFileName;
    }

    public String getRegion() {
        return region;
    }

    public long getSize() {
        return size;
    }

    public String toString() {
        return "IceObservation { source: " + source + " region: " + region + " date: " + date +
                " size: " + size + " shapeFileName: " + shapeFileName + "}";
    }
}
