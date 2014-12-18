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
package dk.dma.embryo.vessel.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.BerthGst;

public class BerthMoverUtility {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("berthMover");
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @Test
    public void test1() {

        entityManager.getTransaction().begin();

        Query nQuery = entityManager.createNativeQuery("delete from berth_gst;");

        System.out.println("Foo:" + nQuery.executeUpdate());

        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();

        nQuery = entityManager
                .createNativeQuery("INSERT INTO berth_gst (id, alias, name, latitude, longitude) VALUES (1,NULL,'Ikerasassuaq Vejrstation (Prins Christian Sund Vejrstation)','60º03,5´N','043º10,5´W'),(2,NULL,'Aappilattoq','60º09,6´N','044º17,2´W'),(3,NULL,'Narsaq Kujalleq (Frederiksdal)','60º00,4´N','044º40,0´W'),(4,NULL,'Tasiusaq','60º11,8´N','044º49,5´W'),(5,NULL,'Nanortalik','60º08,3´N','045º14,5´W'),(6,NULL,'Alluitsup Paa (Sydprøven)','60º27,8´N','045º34,2´W'),(7,NULL,'Alluitsoq (Lichtenau)','60º30,6´N','045º32,5´W'),(8,NULL,'Ammassivik (Sletten)','60º35,8´N','045º23,7´W'),(9,NULL,'Qallimiut','60º42,5´N','045º22,0´W'),(10,NULL,'Saarloq','60º32,3´N','046º01,9´W'),(11,NULL,'Eqalugaarsuit','60º37,4´N','045º54,8´W'),(12,NULL,'Qaqortoq (Julianehåb)','60º43,1´N','046º02,4´W'),(13,NULL,'Upernaviarsuk','60º45,0´N','045º53,8´W'),(14,NULL,'Qassimiut','60º46,9´N','047º09,8´W'),(15,NULL,'Igaliku','60º59,6´N','045º25,4´W'),(16,NULL,'Narsaq','60º54,5´N','046º03,0´W'),(17,NULL,'Narsarsuaq','61º08,8´N','045º26,2´W'),(18,NULL,'Qassiarsuk','61º09,3´N','045º31,0´W'),(19,NULL,'Kangilinnguit (Grønnedal)','61º14,3´N','48º06,1´W'),(20,NULL,'Arsuk','61º10,5´N','048º27,2´W'),(21,NULL,'Paamiut (Frederikshåb)','61º59,8´N','049º40,8´W'),(22,NULL,'Qeqertarsuatsiaat (Fiskenæsset)','63º05,4´N','050º41,0´W'),(23,NULL,'Orsiivik (Polaroil)','63º41,1´N','051º30,6´W'),(24,NULL,'Nordafar','63º42,0´N','051º29,6´W'),(25,NULL,'Nuuk (Godthåb)','64º10,4´N','051º43,5´W'),(26,NULL,'Kapisillit','64º26,0´N','050º16,0´W'),(27,NULL,'Atammik','64º48,3´N','052º11,0´W'),(28,NULL,'Napasoq','65º02,7´N','052º23,1´W'),(29,NULL,'Maniitsoq (Sukkertoppen)','65º24,8´N','052º54,3´W'),(30,NULL,'Kangaamiut','65º49,6´N','053º20,9´W'),(31,NULL,'Kangerlussuup Umiarsualivia','66º58,2´N','050º57,2´W'),(32,NULL,'Itilleq','66º34,6´N','053º30,2´W'),(33,NULL,'Sarfannguit','66º54,0´N','052º52,0´W'),(34,NULL,'Sisimiut (Holsteinsborg)','66º56,5´N','053º40,5´W'),(35,NULL,'Attu','67º56,5´N','053º38,0´W'),(36,NULL,'Ikerasaarsuk','68º08,4´N','053º27,2´W'),(37,NULL,'Iginniarfik','68º09,0´N','053º10,8´W'),(38,NULL,'Niaqornaarsuk','68º14,0´N','052º52,0´W'),(39,NULL,'Kangaatsiaq','68º18,5´N','053º28,0´W'),(40,NULL,'Aasiaat (Egedesminde)','68º42,6´N','052º53,0´W'),(41,NULL,'Kitsissuarsuit (Hunde Ejland)','68º51,5´N','053º07,8´W'),(42,NULL,'Akunnaaq','68º44,7´N','052º20,2´W'),(43,NULL,'Qeqertarsuaq (Godhavn)','69º15,0´N','053º33,0´W'),(44,NULL,'Kangerluk (Diskofjord)','69º29,0´N','053º57,0´W'),(45,NULL,'Ikamiut','68º38,1´N','051º50,0´W'),(46,NULL,'Qasigiannguit (Christianshåb)','68º49,0´N','051º12,0´W'),(47,NULL,'Ilimanaq (Claushavn)','69º05,0´N','051º07,3´W'),(48,NULL,'Ilulissat (Jakobshavn)','69º13,5´N','051º06,0´W'),(49,NULL,'Oqaatsut (Rodebay)','69º20,7´N','051º00,7´W'),(50,NULL,'Qeqertaq','70º00,0´N','051º19,0´W'),(51,NULL,'Saqqaq','70º01,0´N','051º57,0´W'),(52,NULL,'Niaqornat','70º47,6´N','053º40,0´W'),(53,NULL,'Qaarsut','70º44,0´N','052º38,5´W'),(54,NULL,'Uummannaq','70º40,5´N','052º08,0´W'),(55,NULL,'Ikerasak','70º30,0´N','051º19,0´W'),(56,NULL,'Saattut','70º48,8´N','051º38,2´W'),(57,NULL,'Ukkusissat','71º03,0´N','051º53,8´W'),(58,NULL,'Illorsuit','71º14,3´N','053º30,5´W'),(59,NULL,'Nuugaatsiaq','71º32,5´N','053º13,0´W'),(60,NULL,'Upernavik Kujalleq (Søndre Upernavik)','72º09,2´N','055º32,0´W'),(61,NULL,'Kangersuatsiaq (Prøven)','72º22,7´N','055º33,5´W'),(62,NULL,'Upernavik','72º47,5´N','056º09,4´W'),(63,NULL,'Aappilattoq','72º53,0´N','055º36,6´W'),(64,NULL,'Naajaat','73º09,0´N','055º48,0´W'),(65,NULL,'Innaarsuit','73º12,0´N','056º02,0´W'),(66,NULL,'Tasiusaq','73º22,0´N','056º04,0´W'),(67,NULL,'Nutaarmiut','73º31,8´N','056º26,0´W'),(68,NULL,'Nuussuaq (Kraulshavn)','74º07,0´N','057º06,0´W'),(69,NULL,'Kullorsuaq','74º34,8´N','057º13,0´W'); ");

        System.out.println("Foo:" + nQuery.executeUpdate());

        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();

        nQuery = entityManager
                .createNativeQuery("INSERT INTO berth_gst (id, alias, name, latitude, longitude) VALUES (70,NULL,'Savissivik','76º01,0´N','065º06,0´W'),(71,NULL,'Pituffik (Thule Air Base)','76º32,6´N','068º52,5´W'),(72,NULL,'Moriusaq','76º45,0´N','069º53,0´W'),(73,NULL,'Qeqertarsuaq (Herbert Ø)','77º25,3´N','070º09,0´W'),(74,NULL,'Qaanaaq (Thule)','77º27,8´N','069º14,0´W'),(75,NULL,'Qeqertat','77º29,8´N','066º43,0´W'),(76,NULL,'Siorapaluk','77º47,0´N','070º37,0´W'),(77,NULL,'Danmarkshavn','76º46,0´N','018º45,0´W'),(78,NULL,'Daneborg','74º18,2´N','020º14,7´W'),(79,NULL,'Ella Ø','72º53,0´N','025º06,0´W'),(80,NULL,'Nyhavn','72º16,0´N','023º57,0´W'),(81,NULL,'Illoqqortoormiut (Scoresbysund)','70º28,0´N','021º58,0´W'),(82,NULL,'Ittaajimmiut (Kap Hope)','70º27,5´N','022º22,0´W'),(83,NULL,'Uunarteq (Kap Tobin)','70º24,5´N','021º58,0´W'),(84,NULL,'Nerlerit Inaat (Constable Pynt)','70º44,0´N','022º38,0´W'),(85,NULL,'Sermiligaaq','65º54,0´N','036º22,0´W'),(86,NULL,'Tiniteqilaaq','65º52,9´N','037º46,9´W'),(87,NULL,'Kuummiut','65º51,5´N','037º00,5´W'),(88,NULL,'Ikkatteq','65º37,8´N','037º57,2´W'),(89,NULL,'Tasiilaq','65º36,5´N','037º37,5´W'),(90,NULL,'Kulusuk (Kap Dan)','65º34,3´N','037º11,0´W'),(91,NULL,'Kulusuk Mittarfik (Kulusuk Flyveplads)','65º34,7´N','037º08,7´W'),(92,NULL,'Isortoq','65º32,4´N','038º58,5´W'),(93,NULL,'Saqqisikuik (Skjoldungen)','63º13,0´N','041º24,0´W'),(94,NULL,'Seqinnersuusaq','64º58,7´N','051º34,9´W'),(95,NULL,'Maarmorilik','71º07,6´N','051º16,5´W'),(96,NULL,'Zackenberg Forskningsstation','74º28,0´N','020º34,0´W'); ");

        System.out.println("Foo:" + nQuery.executeUpdate());

        entityManager.getTransaction().commit();

    }

    @Test
    public void test() {

        try {

            entityManager.getTransaction().begin();

            TypedQuery<BerthGst> query = entityManager.createQuery("select bgst from BerthGst bgst", BerthGst.class);
            List<BerthGst> berths = query.getResultList();
            entityManager.getTransaction().commit();

            for (BerthGst bgst : berths) {
                entityManager.getTransaction().begin();
                Berth berth = new Berth(bgst.getName(), bgst.getAlias(), bgst.getLatitude(), bgst.getLongitude());

                entityManager.persist(berth);

                entityManager.getTransaction().commit();

            }
        } catch (Throwable e) {
            System.out.println("Caught exception");
            
            e.printStackTrace();

        }
        
        Assert.assertTrue(true);

    }

    @Test
    public void test3() {

        try {
            entityManager.getTransaction().begin();
            TypedQuery<BerthGst> query = entityManager.createQuery("select bgst from BerthGst bgst", BerthGst.class);
            List<BerthGst> berths = query.getResultList();
            entityManager.getTransaction().commit();

            StringBuilder builder = new StringBuilder();
            
            boolean first = true;
            for (BerthGst bgst : berths) {
                if(!first){
//                    builder.append("")
                    first = false;
                }
                
                builder.append("list.add(").append(bgst.asBerthConstructor()).append(");\n");
                
            }

            System.out.println(builder.toString());
        
        } catch (Throwable e) {
            System.out.println("Caught exception");
            
            e.printStackTrace();

        }

        // Hvad sker der lige her?!?
//        Assert.assertTrue(false);

    }
}
