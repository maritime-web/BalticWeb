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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MsiClientMock /* implements MsiClient */ {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public List<MsiClient.MsiItem> getActiveWarnings() {
        try {
            List<MsiClient.MsiItem> result = new ArrayList<>();

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-10"), "Shoal", 72.7516667, -056.2500000, "Greenland West", "Upernavik",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 014/13. " +
                            "WEST COAST OF GREENLAND, OFF UPERNAVIK CHART 1710 " +
                            "IN POSITION 7245,1 N - 05615,0W (WGS84) A 17 METER SHOAL " +
                            "HAS BEEN REPORTED.", DATE_FORMAT.parse("2013-10-10"), "GL-001-01")
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-07"), "Inoperative Beacon", 63.6978333, -051.6053333, "Greenland West", "Kangerluarsoruseq",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 016/13. " +
                            "WEST COAST OF GREENLAND, KANGERLUARSORUSEQ. " +
                            "RACON-BEACON ON SAATTUT IN POSITION " +
                            "63-41.87N 051-36.32W IS REPORTED INOPERATIVE. " +
                            "ADMIRALITY LIST OF LIGHTS NO. L 5500", DATE_FORMAT.parse("2013-10-07"), "GL-001-02")
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-02"), "Shoal", 72.0283333, -056.0416667, "Greenland West", "Svartenhuk",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 017/13. " +
                            "WEST COAST OF GREENLAND, SVARTENHUK PENINSULA " +
                            "AN 8 METER SHOAL HAS BEEN REPORTED IN POS 72 01.7N 056 02.5W " +
                            "GREENLANDIC CHART NO 1600", DATE_FORMAT.parse("2013-10-02"), "GL-001-03")
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-09-28"), "Lost Trawl", 68.1900000, -053.9583333, "Greenland West", "Simiutarsuup",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 018/13. " +
                            "WEST COAST OF GREENLAND, SIMIUTARSUUP " +
                            "A TRAWL(OLD) HAS BEEN REPORTED FLOATING IN POS 68 11,4N " +
                            "053 57,5w GREENLANDIC CHART NO 1416", DATE_FORMAT.parse("2013-09-28"), "GL-001-04")
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-09-30"), "Lost Dinghy", 64.142664, -51.615843, "Greenland West", "Nuuk Fiord",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 019/13. " +
                            "WEST COAST OF GREENLAND, NUUK FIORD. " +
                            "FISHINGVESSEL PITA HAVE LOST A DINGHY YESTERDAY 30 SEP " +
                            "2230 LMT NE OF NUUK AIRPORT.THE DINGHY IS A RYDS 535 WITH " +
                            "AN SUZUKI 115 HP, NO PERSON UNBOARD.", DATE_FORMAT.parse("2013-09-30"), "GL-001-05")
            );

            return result;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public class MsiItemMock extends MsiClient.MsiItem {
        private Date created;
        private String encText;
        private double latitude;
        private double longitude;
        private String mainArea;
        private String subArea;
        private String text;
        private Date updated;
        private String navtexNo;

        public MsiItemMock(Date created, String encText, double latitude, double longitude, String mainArea,
                           String subArea, String text, Date updated, String navtexNo) {
            super(null);
            this.created = created;
            this.encText = encText;
            this.latitude = latitude;
            this.longitude = longitude;
            this.mainArea = mainArea;
            this.subArea = subArea;
            this.text = text;
            this.updated = updated;
            this.navtexNo = navtexNo;
        }

        public Date getCreated() {
            return created;
        }

        public String getENCtext() {
            return encText;
        }

        public List<MsiClient.Point> getPoints() {
            List<MsiClient.Point> result = new ArrayList<>();
            result.add(new MsiClient.Point(null) {
                public double getLatitude() {
                    return latitude;
                }
                public double getLongitude() {
                    return longitude;
                }
            });
            return result;
        }

        public MsiClient.Type getType() {
            return MsiClient.Type.Point;
        }

        public String getMainArea() {
            return mainArea;
        }

        public String getSubArea() {
            return subArea;
        }

        public String getText() {
            return text;
        }

        public Date getUpdated() {
            return updated;
        }

        public String getNavtexNo() {
            return navtexNo;
        }
    }
}
