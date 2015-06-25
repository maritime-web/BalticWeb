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
package dk.dma.embryo.vessel.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dma.embryo.vessel.json.TrackPos;
import dk.dma.enav.model.geometry.Position;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jesper Tejlgaard
 *         <p/>
 *         Client to AISTrack server. See https://github.com/dma-ais/AisTrack
 */
@Path("/ais-store-rest")
public interface AisStoreClient {

    String LOOK_BACK_PT24H = "PT24H";
    String LOOK_BACK_PT12H = "PT12H";
    String LOOK_BACK_PT120H = "PT120H";


    @GET
    @Path("/pastTrack/{mmsi}")
    List<TrackPosition> pastTrack(@PathParam("mmsi") Long mmsi, @QueryParam("sourceFilter") String sourceFilters, @QueryParam("duration") String duration);

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class TrackPosition {

        @JsonProperty("src.id")
        private String srcId;
        @JsonProperty("src.clk")
        private Date srcClk;
        @JsonProperty("src.cty")
        private String srcCty;
        @JsonProperty("src.reg")
        private String srcReg;

        private Double lat;
        private Double lon;
        private Double sog;
        private Double cog;
        private Integer hdg;
        private Integer acc;

        // //////////////////////////////////////////////////////////////////////
        // Utility methods
        // //////////////////////////////////////////////////////////////////////
        public TrackPos toTrackPos() {
            TrackPos tp = new TrackPos();
            tp.setLat(getLat());
            tp.setLon(getLon());
            tp.setCog(getCog());
            tp.setSog(getSog());
            tp.setTs(getSrcClk());
            return tp;
        }

        public static List<TrackPosition> downSample(List<TrackPosition> list, int minPastTrackDist) {
            List<TrackPosition> downSampled = new ArrayList<>(list.size());
            if (list.size() == 0) {
                return downSampled;
            }
            downSampled.add(list.get(0));
            int i = 0;
            int n;
            while (i < list.size()) {
                TrackPosition pos = list.get(i);
                n = i + 1;
                while (n < list.size()) {
                    TrackPosition next = list.get(n);
                    if (pos.distance(next) > minPastTrackDist) {
                        downSampled.add(next);
                        break;
                    }
                    n++;
                }
                i = n;
            }
            return downSampled;
        }

        public double distance(TrackPosition pos2) {
            TrackPosition pos1 = this;
            return Position.create(pos1.getLat(), pos1.getLon()).rhumbLineDistanceTo(Position.create(pos2.getLat(), pos2.getLon()));
        }

        // //////////////////////////////////////////////////////////////////////
        // Object methods
        // //////////////////////////////////////////////////////////////////////
        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }

        // //////////////////////////////////////////////////////////////////////
        // Property methods
        // //////////////////////////////////////////////////////////////////////
        public String getSrcId() {
            return srcId;
        }

        public void setSrcId(String srcId) {
            this.srcId = srcId;
        }

        public Date getSrcClk() {
            return srcClk;
        }

        public void setSrcClk(Date srcClk) {
            this.srcClk = srcClk;
        }

        public String getSrcCty() {
            return srcCty;
        }

        public void setSrcCty(String srcCty) {
            this.srcCty = srcCty;
        }

        public String getSrcReg() {
            return srcReg;
        }

        public void setSrcReg(String srcReg) {
            this.srcReg = srcReg;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }

        public void setLon(Double lon) {
            this.lon = lon;
        }

        public Double getSog() {
            return sog;
        }

        public void setSog(Double sog) {
            this.sog = sog;
        }

        public Double getCog() {
            return cog;
        }

        public void setCog(Double cog) {
            this.cog = cog;
        }

        public Integer getHdg() {
            return hdg;
        }

        public void setHdg(Integer hdg) {
            this.hdg = hdg;
        }

        public Integer getAcc() {
            return acc;
        }

        public void setAcc(Integer acc) {
            this.acc = acc;
        }
    }
}
