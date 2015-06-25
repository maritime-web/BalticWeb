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

import dk.dma.embryo.vessel.json.TrackPos;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.Date;

import static dk.dma.embryo.vessel.integration.AisStoreClient.TrackPosition;


/**
 * Created by Jesper Tejlgaard on 6/10/15.
 */
public class AisTrackStoreTest {

    @Test
    public void testToTrackPos_noValues() {
        // SETUP DATA
        TrackPosition track = new TrackPosition();
        // EXECUTE
        TrackPos result = track.toTrackPos();
        // EXPECTED
        TrackPos expected = new TrackPos();
        // ASSERT
        ReflectionAssert.assertReflectionEquals(expected, result);
    }

    @Test
    public void testToTrackPos() {
        Date now = DateTime.now(DateTimeZone.UTC).toDate();
        // SETUP DATA
        TrackPosition trackPosition = new TrackPosition();
        trackPosition.setSog(10.0);
        trackPosition.setCog(12.0);
        trackPosition.setLat(20.0);
        trackPosition.setLon(30.0);
        trackPosition.setAcc(90);
        trackPosition.setHdg(100);
        trackPosition.setSrcClk(now);
        trackPosition.setSrcCty("DK");
        trackPosition.setSrcId("123456789");
        trackPosition.setSrcReg("Foo");

        // EXECUTE
        TrackPos result = trackPosition.toTrackPos();

        // EXPECTED
        TrackPos expected = new TrackPos();
        expected.setLat(20.0);
        expected.setLon(30.0);
        expected.setSog(10.0);
        expected.setCog(12.0);
        expected.setTs(now);

        // ASSERT
        ReflectionAssert.assertReflectionEquals(expected, result);
    }

}
