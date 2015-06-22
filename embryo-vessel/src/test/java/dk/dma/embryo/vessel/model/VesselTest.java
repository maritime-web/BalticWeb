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
package dk.dma.embryo.vessel.model;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Jesper Tejlgaard on 6/16/15.
 */
public class VesselTest {

    @Test
    public void testAsMap() {
        Vessel vessel1 = createArcticWebVessel(1L, "ET", "ETC", 11L);
        Vessel vessel2 = createArcticWebVessel(2L, "TO", "TOC", 22L);

        List<Vessel> vessels = Arrays.asList(vessel1, vessel2);

        Map<Long, Vessel> result = Vessel.asMap(vessels);

        Assert.assertTrue(result.containsKey(vessel1.getMmsi()));
        ReflectionAssert.assertReflectionEquals(vessel1, result.get(vessel1.getMmsi()));
        Assert.assertTrue(result.containsKey(vessel2.getMmsi()));
        ReflectionAssert.assertReflectionEquals(vessel2, result.get(vessel2.getMmsi()));
    }

    @Test
    public void testExtractMmsiNumbers() {
        // TEST DATA
        Vessel vessel1 = createArcticWebVessel(1L, "ET", "ETC", 11L);
        Vessel vessel2 = createArcticWebVessel(2L, "TO", "TOC", 22L);
        List<Vessel> vessels = Arrays.asList(vessel1, vessel2);

        // EXPECTATION
        List<Long> expected = Arrays.asList(1L, 2L);

        // ASSERT
        ReflectionAssert.assertReflectionEquals(expected, Vessel.extractMmsiNumbers(vessels));
    }

    @Test
    public void testIsUpToDate() {
        Vessel vessel1 = createArcticWebVessel(1L, "ET", "ETC", 11L);

        Assert.assertTrue(vessel1.isUpToDate("ET", "ETC", 11L));
        Assert.assertFalse(vessel1.isUpToDate("T", "ETC", 11L));
        Assert.assertFalse(vessel1.isUpToDate("ET", "EC", 11L));
        Assert.assertFalse(vessel1.isUpToDate("ET", "ETC", 21L));
    }

    @Test
    public void testIsUpToDate_AisDataIsNull() {
        Vessel vessel1 = new Vessel(1L);

        Assert.assertFalse(vessel1.isUpToDate("ET", "ETC", 11L));
        Assert.assertTrue(vessel1.isUpToDate(null, null, null));
    }


    @Test
    public void testMergeNonReferenceFields() {
        // TEST DATA

        DateTime oneDayAgo = DateTime.now().minusDays(1);
        DateTime twoDaysAg = oneDayAgo.minusDays(2);

        Vessel vessel1 = new Vessel();
        vessel1.setMmsi(1L);
        vessel1.setGrossTonnage(2000);
        vessel1.setCommCapabilities("Phone");
        vessel1.setPersons(21);
        vessel1.setIceClass("A1");
        vessel1.setMaxSpeed(BigDecimal.valueOf(12d));
        vessel1.setHelipad(false);
        vessel1.setAisData(new AisData("Olga", "OD45D", 123L));
        vessel1.setActiveVoyage(new Voyage("DreamIsland", "45 456.00N", "067 453.34W", twoDaysAg, oneDayAgo));

        Vessel vessel2 = new Vessel();
        vessel2.setMmsi(1L);
        vessel2.setGrossTonnage(3000);
        vessel2.setCommCapabilities("Email");
        vessel2.setPersons(25);
        vessel2.setIceClass("A2");
        vessel2.setMaxSpeed(BigDecimal.valueOf(10d));
        vessel2.setHelipad(true);
        vessel2.setAisData(new AisData("OlgaZ", "XD45D", 1234L));
        vessel2.setActiveVoyage(new Voyage("Funkystuff", "23 456.00N", "023 453.34W", oneDayAgo, DateTime.now()));

        // EXPECTATION
        Vessel expected = new Vessel();
        expected.setMmsi(1L);
        expected.setGrossTonnage(3000);
        expected.setCommCapabilities("Email");
        expected.setPersons(25);
        expected.setIceClass("A2");
        expected.setMaxSpeed(BigDecimal.valueOf(10d));
        expected.setHelipad(true);
        expected.setAisData(new AisData("OlgaZ", "XD45D", 1234L));
        expected.setActiveVoyage(new Voyage("DreamIsland", "45 456.00N", "067 453.34W", twoDaysAg, oneDayAgo));
        expected.getActiveVoyage().setEnavId(vessel1.getActiveVoyage().getEnavId());

        // EXECUTE
        Vessel result = vessel1.mergeNonReferenceFields(vessel2);

        // ASSERT
        Assert.assertTrue(vessel1 == result);
        ReflectionAssert.assertReflectionEquals(expected, result);
    }


    public static Vessel createArcticWebVessel(Long mmsi, String name, String callSign, Long imo) {
        Vessel vessel = new Vessel();
        vessel.setMmsi(mmsi);
        vessel.setGrossTonnage(randomInteger());
        vessel.setCommCapabilities(randomString());
        vessel.setPersons(randomInteger());
        vessel.setIceClass(randomString());
        vessel.setHelipad(false);
        vessel.setAisData(new AisData(name, callSign, imo));
        return vessel;
    }

    public static int randomInteger() {
        return new Random().nextInt();
    }


    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
