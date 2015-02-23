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
        private double longitude;
        private double latitude;
        
        public Point(PointDto pointDto) {
            this.longitude = pointDto.getLongitude();
            this.latitude = pointDto.getLatitude();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;

            long temp;
            temp = Double.doubleToLongBits(latitude);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(longitude);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            
            return result;
        }

        public double getLongitude() {
            return longitude;
        }
        public double getLatitude() {
            return latitude;
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
        //private MsiDto md;
        
        private Type type;
        private Date created;
        private String eNCText;
        private String mainArea;
        private String subArea;
        private String navtexNo;
        private String text;
        private Date updated;
        
        private List<Point> points;
        
        public MsiItem(MsiDto msiDto) {
            //this.md = md;
            
            this.type = getType(msiDto);
            this.created = msiDto.getCreated().toGregorianCalendar().getTime();
            this.eNCText = msiDto.getEncText();
            this.mainArea = msiDto.getAreaEnglish();
            this.subArea = msiDto.getSubarea();
            this.navtexNo = msiDto.getNavtexNo();
            this.text = msiDto.getNavWarning();
            this.updated = msiDto.getUpdated().toGregorianCalendar().getTime();
            
            this.points = getPoints(msiDto);
        }

        private Type getType(MsiDto msiDto) {
            if (msiDto.getPoints() == null || msiDto.getPoints().getPoint().size() == 0) {
                return Type.General;
            }
            return Type.valueOf(msiDto.getLocationType());
        }
        
        private List<Point> getPoints(MsiDto msiDto) {
            List<Point> result = new ArrayList<>();
            for (PointDto pd : msiDto.getPoints().getPoint()) {
                result.add(new Point(pd));
            }
            return result;
        }

        public Type getType() {
            return type;
        }
        public Date getCreated() {
            return created;
        }
        public String getENCtext() {
            return eNCText;
        }
        public String getMainArea() {
            return mainArea;
        }
        public String getSubArea() {
            return subArea;
        }
        public String getNavtexNo() {
            return navtexNo;
        }
        public String getText() {
            return text;
        }
        public Date getUpdated() {
            return updated;
        }
        public List<Point> getPoints() {
            return points;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            
            result = prime * result + ((created == null) ? 0 : created.hashCode());
            result = prime * result + ((eNCText == null) ? 0 : eNCText.hashCode());
            result = prime * result + ((mainArea == null) ? 0 : mainArea.hashCode());
            result = prime * result + ((navtexNo == null) ? 0 : navtexNo.hashCode());
            result = prime * result + ((points == null) ? 0 : points.hashCode());
            result = prime * result + ((subArea == null) ? 0 : subArea.hashCode());
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((updated == null) ? 0 : updated.hashCode());
            
            return result;
        }
    }
}
