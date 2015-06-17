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

import org.junit.Assert;
import org.junit.Test;

import static dk.dma.embryo.vessel.integration.AisTrackClient.AisTrack;
import static dk.dma.embryo.vessel.integration.AisTrackClient.Pos;
import static dk.dma.embryo.vessel.integration.AisTrackClient.Target;


/**
 * Created by Jesper Tejlgaard on 6/10/15.
 */
public class AisTrackClientTest {

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noTarget() {
        AisTrack track = new AisTrack();
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noVesselPosition() {
        AisTrack track = new AisTrack();
        track.setTarget(new Target());
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noVesselPositionPos() {
        AisTrack track = new AisTrack();
        track.setTarget(new Target());
        track.getTarget().setVesselPosition(new AisTrackClient.VesselPosition());
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noMmsi() {
        AisTrack track = new AisTrack();
        track.setTarget(new Target());
        track.getTarget().setVesselPosition(new AisTrackClient.VesselPosition());
        track.getTarget().getVesselPosition().setPos(new Pos());
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noLat() {
        AisTrack track = new AisTrack();
        track.setTarget(new Target());
        track.getTarget().setVesselPosition(new AisTrackClient.VesselPosition());
        track.getTarget().getVesselPosition().setPos(new Pos());
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }

    @Test
    public void testAisTrack_minimumCriteriaFulfilled_noLon() {
        AisTrack track = new AisTrack();
        track.setTarget(new Target());
        track.getTarget().setVesselPosition(new AisTrackClient.VesselPosition());
        track.getTarget().getVesselPosition().setPos(new Pos());
        track.getTarget().getVesselPosition().getPos().setLat(10.0);
        Assert.assertFalse(track.minimumCriteriaFulfilled());
    }
}
