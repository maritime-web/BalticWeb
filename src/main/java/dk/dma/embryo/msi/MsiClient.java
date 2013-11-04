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
import dk.frv.msiedit.core.webservice.message.MsiDtoLight;

import java.util.Date;
import java.util.List;

public interface MsiClient {
    List<MsiItem> getActiveWarnings();

    class MsiItem {
        private MsiDto mdl;

        public Date getCreated() {
            return mdl.getCreated().toGregorianCalendar().getTime();
        }

        public String getENCtext() {
            return mdl.getEncText();
        }

        public double getLatitude() {
            return mdl.getPoints().getPoint().get(0).getLatitude();
        }

        public double getLongitude() {
            return mdl.getPoints().getPoint().get(0).getLongitude();
        }

        public String getMainArea() {
            return mdl.getAreaEnglish();
        }

        public String getSubArea() {
            return mdl.getSubarea();
        }

        public String getText() {
            return mdl.getNavWarning();
        }

        public Date getUpdated() {
            return mdl.getUpdated().toGregorianCalendar().getTime();
        }

        public MsiItem(MsiDto mdl) {
            this.mdl = mdl;
        }

        public String toString() {
            return getClass().getName() +
                    "\n- created: " + getCreated() +
                    "\n- ENCText: " + getENCtext() +
                    "\n- Latitude: " + getLatitude() +
                    "\n- Longitude: " + getLongitude() +
                    "\n- MainArea: " + getMainArea() +
                    "\n- SubArea: " + getSubArea() +
                    "\n- Text: " + getText() +
                    "\n- Updated: " + getUpdated();
        }
    }
}
