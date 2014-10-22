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
package dk.dma.embryo.dataformats.inshore;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 * @author Jesper Tejlgaard
 */
public class InshoreIceReportParserTest {

    @Test
    public void test() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/2014-07-02-for-test.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
        expected.addHeader("www.dmi.dk");
        expected.setOverview("På indenskærs isrekognoscering onsdag den 02. juli 2014 mellem Skovfjord, Bredefjord, Julianehåbsfjord, Mågeløbene, Knækket og Narsarsuaq blev følgende observeret:");

        expected.addNotification(11, "I Torssukatak: Var der Tåge.");
        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der enkelte isfjelde og skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        expected.addFooter("Næste isrekognoscering forventes mandag den. 07. juli  2014");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk");
//        expected.addFooter("Iscentralen Narsarsuaq, onsdag den 02. juli  2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    @Test
    public void testWithLineBreakInOverview() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/2014-07-10_for-test.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
        expected.addHeader("www.dmi.dk");

        expected.setOverview("På indenskærs isrekognoscering torsdag den 10. juli 2014 mellem Prins Chr. Sund, Frederiksdal, Nanortalik, Julianehåbs Fjord, Hollænder Løbet, Mågeløbene, Knækket, Bredefjord, Narsaq Sund og Narsarsuaq blev følgende observeret:");

        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der ved mundingen enkelte isfjelde og spredte skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        expected.addFooter("Næste isrekognoscering forventes onsdag den 16. juli 2014.");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk");
//        expected.addFooter("Iscentralen Narsarsuaq, torssdag den 10. juli 2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    //@Test
    public void testWithMissingHeader() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/missingHeader.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.setOverview("På indenskærs isrekognoscering onsdag den 02. juli 2014 mellem Skovfjord, Bredefjord, Julianehåbsfjord, Mågeløbene, Knækket og Narsarsuaq blev følgende observeret:");

        expected.addNotification(11, "I Torssukatak: Var der Tåge.");
        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der enkelte isfjelde og skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        expected.addFooter("Næste isrekognoscering forventes mandag den. 07. juli  2014");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk");
//        expected.addFooter("Iscentralen Narsarsuaq, onsdag den 02. juli  2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    //@Test
    public void testWithMissingHeaderAndOverview() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/missingHeaderAndOverview.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();

        expected.addNotification(11, "I Torssukatak: Var der Tåge.");
        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der enkelte isfjelde og skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        expected.addFooter("Næste isrekognoscering forventes mandag den. 07. juli  2014");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk");
//        expected.addFooter("Iscentralen Narsarsuaq, onsdag den 02. juli  2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }
    
    //@Test
    public void testWithMissingOverview() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/missingOverview.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
//        expected.addHeader("www.dmi.dk");

        expected.addNotification(11, "I Torssukatak: Var der Tåge.");
        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der enkelte isfjelde og skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        expected.addFooter("Næste isrekognoscering forventes mandag den. 07. juli  2014");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk");
//        expected.addFooter("Iscentralen Narsarsuaq, onsdag den 02. juli  2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }


    @Test
    public void testWithMissingFooter() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/missingFooter.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
        expected.addHeader("www.dmi.dk");
        expected.setOverview("På indenskærs isrekognoscering onsdag den 02. juli 2014 mellem Skovfjord, Bredefjord, Julianehåbsfjord, Mågeløbene, Knækket og Narsarsuaq blev følgende observeret:");

        expected.addNotification(11, "I Torssukatak: Var der Tåge.");
        expected.addNotification(12, "I Knækket: Var der isfrit.");
        expected.addNotification(13, "I Ikerasagssuaq: Var der enkelte isfjelde og skosser.");
        expected.addNotification(14, "I Sermilik: Var der enkelte isfjelde og skosser.");
        expected.addNotification(15, "I Akugdleq: Var der enkelte isfjelde og skosser.");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    @Test
    public void testWithMissingObservations() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/missingObservations.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
        expected.addHeader("www.dmi.dk");
        expected.setOverview("På indenskærs isrekognoscering onsdag den 02. juli 2014 mellem Skovfjord, Bredefjord, Julianehåbsfjord, Mågeløbene, Knækket og Narsarsuaq blev følgende observeret: Næste isrekognoscering forventes mandag den. 07. juli 2014 For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk Iscentralen Narsarsuaq, onsdag den 02. juli 2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    @Test
    public void testWithObservationLineBreak() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/linebreak.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("DMI");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");
        expected.addHeader("www.dmi.dk");

        expected.setOverview("På indenskærs isrekognoscering fredag den 1. august 2014 mellem Prins Christian Sund, Frederiksdal, Båkeruten, Nanortalik, Sydprøven, Sardloq, Julianehåbs Fjord, Skovfjord, Narsaq Sund, Bredefjord og Narsarsuaq blev følgende observeret:");

        expected.addNotification(87, "I Prins Christian Sund ved det smalle sted: Var der enkelte isfjelde og skosser. Der var et mindre bælte med spredte skosser der kunne passeres.");
        expected.addNotification(88, "I Prins Christian Sund østlige del: Var der enkelte isfjelde og skosser. Der var områder med spredte skosser ud for bræerne.");
        expected.addNotification(89, "I Prins Christian Sund Ø-lig ved mundingen: Var der spredte isfjelde der kunne passeres.");

        expected.addFooter("Næste isrekognoscering forventes onsdag den 6. august 2014.");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk.");
//        expected.addFooter("Iscentralen Narsarsuaq, onsdag den 02. juli  2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

    @Test
    public void testFooterWithoutDots() throws IOException {

        InputStream is = getClass().getResourceAsStream("/inshore-ice-reports/2014-10-20.txt");
        InshoreIceReportParser parser = new InshoreIceReportParser(is);

        InshoreIceReport expected = new InshoreIceReport();
        expected.addHeader("www.dmi.dk");
        expected.addHeader("Iscentralen Ismelding");
        expected.addHeader("Narsarsuaq");

        expected.setOverview("Billeder fra Isrekognosceringer kan modtages i DropBox. Skriv venligst til Iscentralen icepatrol@dmi.dk for invitation. På indenskærs isrekognoscering mandag den 20. oktober 2014 mellem Prins Christian Sund, Nanortalik, Sydprøven, Qaqortoq, Narsaq og Narsarsuaq blev følgende observeret:");

        expected.addNotification(23, "I Bredefjord: 2-3/10 bræis i den inderste del, aftagende mod mundingen.");
        expected.addNotification(24, "I ruten Narssaq Sund - Tasiussaq: Spredte isfjelde og 6/10 bræis.");
        expected.addNotification(31, "I Tunuaraq: Spredte isfjelde og 1-2/10 bræis.");
        expected.addNotification(32, "I Narssaq Sund: Spredte isfjelde og 1-2/10 bræis samt områder med nyis.");

        expected.addFooter("Næste isrekognoscering forventes onsdag den 22-10-2014");
        expected.addFooter("For yderligere information kontakt Iscentralen på telefon +299 66 52 44 eller icepatrol@dmi.dk.");
//        expected.addFooter("Iscentralen Narsarsuaq, torssdag den 10. juli 2014");

        InshoreIceReport notifications = parser.parse();

        ReflectionAssert.assertReflectionEquals(expected, notifications);
    }

}
