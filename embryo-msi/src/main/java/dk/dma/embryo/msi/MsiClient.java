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
package dk.dma.embryo.msi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.frv.msiedit.core.webservice.message.MsiDto;
import dk.frv.msiedit.core.webservice.message.PointDto;

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
        
        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null)           ? 0 : name.hashCode());
            result = prime * result + ((description == null)    ? 0 : description.hashCode());
            
            return result;
        }
        
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
