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
package dk.dma.embryo.vessel.json.client;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

public interface AisViewService {
    
    /*
    @GET
    @Path("/vessel_list")
    VesselListResult vesselList(@QueryParam("requestId") long requestId);
*/
    @GET
    @Path("vessel_target_details")
    Map<String, Object> vesselTargetDetails(@QueryParam("mmsi") long vesselId, @QueryParam("past_track") int pastTrack);

    /*
    public static class VesselListResult {
        private long requestId;
        private long vesselsInWorld;
        private VesselList vesselList;

        public String toString() {
            return AisJsonClientFactory.asJson(this);
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
    */
}
