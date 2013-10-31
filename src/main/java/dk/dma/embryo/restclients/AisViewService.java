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
package dk.dma.embryo.restclients;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Map;

public interface AisViewService {
    @GET
    @Path("/vessel_list")
    VesselListResult vesselList(@QueryParam("requestId") long requestId);

    @GET
    @Path("vessel_target_details")
    Map vesselTargetDetails(@QueryParam("mmsi") long vesselId, @QueryParam("past_track") int pastTrack);

    public static class VesselListResult {
        private long requestId;
        private long vesselsInWorld;
        private VesselList vesselList;

        public String toString() {
            return RestClientFactory.asJson(this);
        }

        public long getRequestId() {
            return requestId;
        }

        public void setRequestId(long requestId) {
            this.requestId = requestId;
        }

        public long getVesselsInWorld() {
            return vesselsInWorld;
        }

        public void setVesselsInWorld(long vesselsInWorld) {
            this.vesselsInWorld = vesselsInWorld;
        }

        public VesselList getVesselList() {
            return vesselList;
        }

        public void setVesselList(VesselList vesselList) {
            this.vesselList = vesselList;
        }
    }

    public static class VesselList {
        private Map<String, String[]> vessels;
        private long currentTime;
        private long inWorldCount;
        private long vesselCount;

        public Map<String, String[]> getVessels() {
            return vessels;
        }

        public void setVessels(Map<String, String[]> vessels) {
            this.vessels = vessels;
        }

        public long getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }

        public long getInWorldCount() {
            return inWorldCount;
        }

        public void setInWorldCount(long inWorldCount) {
            this.inWorldCount = inWorldCount;
        }

        public long getVesselCount() {
            return vesselCount;
        }

        public void setVesselCount(long vesselCount) {
            this.vesselCount = vesselCount;
        }
    }
}
