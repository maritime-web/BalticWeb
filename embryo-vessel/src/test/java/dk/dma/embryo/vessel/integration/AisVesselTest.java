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

import dk.dma.embryo.vessel.json.VesselOverview;
import dk.dma.embryo.vessel.model.AisData;
import dk.dma.embryo.vessel.model.Vessel;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AisVesselTest {

    @Test
    public void testToVesselOverview_LatLonIsNull() {
        // SETUP INPUT DATA
        AisVessel aisVessel = createAisVessel(1L, "ET", "ETC");
        aisVessel.setLon(null);
        aisVessel.setLat(null);

        // EXECUTE
        VesselOverview result = aisVessel.toVesselOverview();

        // EXPECTED RESULT
        VesselOverview expected = new VesselOverview();
        expected.setMmsi(1L);
        expected.setCallSign("ETC");
        expected.setName("ET");
        expected.setAngle(aisVessel.getCog());
        expected.setX(null);
        expected.setY(null);
        expected.setMoored(false);
        expected.setInAW(false);
        expected.setType("1");

        // ASSERT
        ReflectionAssert.assertReflectionEquals(expected, result);
    }

    @Test
    public void testAddMissingVessels() {
        // SETUP INPUT DATA
        AisVessel aisVessel1 = createAisVessel(1L, "ET", "ETC");
        AisVessel aisVessel2 = createAisVessel(2L, "TO", "TOC");

        List<AisVessel> aisVessels = Arrays.asList(aisVessel1, aisVessel2);

        Vessel vessel1 = createArcticWebVessel(11L, "ETET", "ETETC", 11L);
        Vessel vessel2 = createArcticWebVessel(22L, "TOTO", "TOTOC", 33L);

        List<Vessel> arcticWebVessels = Arrays.asList(vessel1, vessel2);

        // EXPECTED RESULT
        List<AisVessel> expected = Arrays.asList(toAisVessel(vessel1), toAisVessel(vessel2), aisVessel1, aisVessel2);

        // EXECUTE
        List<AisVessel> result = AisVessel.addMissingVessels(aisVessels, arcticWebVessels);

        // ASSERT
        ReflectionAssert.assertReflectionEquals(expected, result);
    }

    public static AisVessel createAisVessel(Long mmsi, String name, String callSign) {
        AisVessel aisVessel = new AisVessel();
        aisVessel.setMmsi(mmsi);
        aisVessel.setName(name);
        aisVessel.setCallsign(callSign);
        aisVessel.setCog(randomDouble());
        aisVessel.setDraught(randomDouble());
        aisVessel.setHeading(randomDouble());
        aisVessel.setLat(randomDouble());
        aisVessel.setLon(randomDouble());
        aisVessel.setLength(randomDouble());
        aisVessel.setMaxSpeed(randomDouble());
        aisVessel.setRot(randomDouble());
        aisVessel.setSog(randomDouble());
        aisVessel.setWidth(randomDouble());

        aisVessel.setCountry(randomString());
        aisVessel.setDestination(randomString());
        aisVessel.setLastReport(new Date());
        aisVessel.setSourceCountry(randomString());
        aisVessel.setTargetType(randomString());
        aisVessel.setVesselType(randomString());
        aisVessel.setSourceType(randomString());
        aisVessel.setNavStatus(randomString());
        aisVessel.setSourceRegion(randomString());
        return aisVessel;
    }

    public static AisVessel toAisVessel(Vessel vessel) {
        AisVessel aisVessel = new AisVessel();
        aisVessel.setMmsi(vessel.getMmsi());
        aisVessel.setCallsign(vessel.getAisData().getCallsign());
        aisVessel.setImoNo(vessel.getAisData().getImoNo());
        aisVessel.setName(vessel.getAisData().getName());
        return aisVessel;
    }


    public static Vessel createArcticWebVessel(Long mmsi, String name, String callSign, Long imo) {
        Vessel vessel = new Vessel();
        vessel.setMmsi(mmsi);
        vessel.setAisData(new AisData(name, callSign, imo));
        return vessel;
    }

    public static double randomDouble() {
        double start = 0;
        double end = 50;
        double random = new Random().nextDouble();
        double result = start + (random * (end - start));
        return result;
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }


}
