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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MsiClientMock implements MsiClient {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<MsiItem> getActiveWarnings() {
        try {
            List<MsiItem> result = new ArrayList<>();

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-10"), "Shoal", 72.7516667, -056.2500000, "Greenland West", "Upernavik",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 014/13. " +
                            "WEST COAST OF GREENLAND, OFF UPERNAVIK CHART 1710 " +
                            "IN POSITION 7245,1 N - 05615,0W (WGS84) A 17 METER SHOAL " +
                            "HAS BEEN REPORTED.", DATE_FORMAT.parse("2013-10-10"))
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-07"), "Inoperative Beacon", 63.6978333, -051.6053333, "Greenland West", "Kangerluarsoruseq",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 016/13. " +
                            "WEST COAST OF GREENLAND, KANGERLUARSORUSEQ. " +
                            "RACON-BEACON ON SAATTUT IN POSITION " +
                            "63-41.87N 051-36.32W IS REPORTED INOPERATIVE. " +
                            "ADMIRALITY LIST OF LIGHTS NO. L 5500", DATE_FORMAT.parse("2013-10-07"))
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-10-02"), "Shoal", 72.0283333, -056.0416667, "Greenland West", "Svartenhuk",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 017/13. " +
                            "WEST COAST OF GREENLAND, SVARTENHUK PENINSULA " +
                            "AN 8 METER SHOAL HAS BEEN REPORTED IN POS 72 01.7N 056 02.5W " +
                            "GREENLANDIC CHART NO 1600", DATE_FORMAT.parse("2013-10-02"))
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-09-28"), "Lost Trawl", 68.1900000, -053.9583333, "Greenland West", "Simiutarsuup",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 018/13. " +
                            "WEST COAST OF GREENLAND, SIMIUTARSUUP " +
                            "A TRAWL(OLD) HAS BEEN REPORTED FLOATING IN POS 68 11,4N " +
                            "053 57,5w GREENLANDIC CHART NO 1416", DATE_FORMAT.parse("2013-09-28"))
            );

            result.add(new MsiItemMock(DATE_FORMAT.parse("2013-09-30"), "Lost Dinghy", 64.142664, -51.615843, "Greenland West", "Nuuk Fiord",
                    "TEST - NAVIGATIONAL WARNING JOINT ARCTIC COMMAND NO. 019/13. " +
                            "WEST COAST OF GREENLAND, NUUK FIORD. " +
                            "FISHINGVESSEL PITA HAVE LOST A DINGHY YESTERDAY 30 SEP " +
                            "2230 LMT NE OF NUUK AIRPORT.THE DINGHY IS A RYDS 535 WITH " +
                            "AN SUZUKI 115 HP, NO PERSON UNBOARD.", DATE_FORMAT.parse("2013-09-30"))
            );

            return result;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public class MsiItemMock extends MsiItem {
        private Date created;
        private String encText;
        private double latitude;
        private double longitude;
        private String mainArea;
        private String subArea;
        private String text;
        private Date updated;

        public MsiItemMock(Date created, String encText, double latitude, double longitude, String mainArea,
                           String subArea, String text, Date updated) {
            super(null);
            this.created = created;
            this.encText = encText;
            this.latitude = latitude;
            this.longitude = longitude;
            this.mainArea = mainArea;
            this.subArea = subArea;
            this.text = text;
            this.updated = updated;
        }

        public Date getCreated() {
            return created;
        }

        public String getENCtext() {
            return encText;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
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
    }
}
