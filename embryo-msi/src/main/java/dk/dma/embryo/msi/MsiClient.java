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
package dk.dma.embryo.msi;

import dk.frv.msiedit.core.webservice.message.MsiDto;
import dk.frv.msiedit.core.webservice.message.PointDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface MsiClient {
    List<MsiItem> getActiveWarnings(List<String> regions);
    List<Region> getRegions();

    public enum Type {Point, Polygon, Polyline, Points, General}

    class Point {
        private PointDto pd;

        public Point(PointDto pd) {
            this.pd = pd;
        }

        public double getLongitude() {
            return pd.getLongitude();
        }

        public double getLatitude() {
            return pd.getLatitude();
        }

        public String toString() {
            return "(Latitude: " + getLatitude() + " Longitude: " + getLongitude() + ")";
        }
    }
    
    class Region {
        private String name;
        private String description;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }

    class MsiItem {
        private MsiDto md;

        public Type getType() {
            if (md.getPoints() == null || md.getPoints().getPoint().size() == 0) {
                return Type.General;
            }
            return Type.valueOf(md.getLocationType());
        }

        public Date getCreated() {
            return md.getCreated().toGregorianCalendar().getTime();
        }

        public String getENCtext() {
            return md.getEncText();
        }

        public List<Point> getPoints() {
            List<Point> result = new ArrayList<>();
            for (PointDto pd : md.getPoints().getPoint()) {
                result.add(new Point(pd));
            }
            return result;
        }

        public String getMainArea() {
            return md.getAreaEnglish();
        }

        public String getSubArea() {
            return md.getSubarea();
        }

        public String getNavtexNo() {
            return md.getNavtexNo();
        }

        public String getText() {
            return md.getNavWarning();
        }

        public Date getUpdated() {
            return md.getUpdated().toGregorianCalendar().getTime();
        }

        public MsiItem(MsiDto md) {
            this.md = md;
        }

        public String toString() {
            return getClass().getName() +
                    "\n- created: " + getCreated() +
                    "\n- Type: " + getType() +
                    "\n- ENCText: " + getENCtext() +
                    "\n- Points: " + getPoints() +
                    "\n- MainArea: " + getMainArea() +
                    "\n- SubArea: " + getSubArea() +
                    "\n- Text: " + getText() +
                    "\n- Updated: " + getUpdated();
        }
    }
}
