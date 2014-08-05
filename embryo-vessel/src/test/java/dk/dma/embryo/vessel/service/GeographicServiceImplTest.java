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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.persistence.GeographicDaoImpl;

public class GeographicServiceImplTest {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    
    private GeographicService geoService;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");

        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();

        // Test data found on
        // http://gronlandskehavnelods.dk/#HID=78
        entityManager.persist(new Berth("Nuuk", "64 10.4N", "051 43.5W"));
        entityManager.persist(new Berth("Sisimiut", "Holsteinsborg", "66 56.5N", "053 40.5W"));
        entityManager.persist(new Berth("Danmarkshavn", "76 46.0N", "018 45.0W"));
        entityManager.persist(new Berth("Kangilinnguit", "Grønnedal", "61 14.3N", "48 06.1W"));
        entityManager.persist(new Berth("Aasiaat", "Egedesminde", "68 42.6N", "052 53.0W"));
        entityManager.persist(new Berth("Ilulissat", "Jakobshavn", "69 13.5N", "051 06.0W"));
        entityManager.persist(new Berth("Qeqertarsuaq", "Godhavn", "69 15.0N", "053 33.0W"));
        entityManager.persist(new Berth("Ammassivik", "Sletten", "60 35.8N", "045 23.7W"));
        entityManager.persist(new Berth("Ittaajimmiut", "Kap Hope", "70 27.5N", "022 22.0W"));
        entityManager.persist(new Berth("Kangersuatsiaq", "Prøven", "72 22.7N","055 33.5W"));

        entityManager.getTransaction().commit();
        entityManager.close();
    }
    
    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        geoService = new GeographicServiceImpl(new GeographicDaoImpl(entityManager));
        entityManager.clear();
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @Test
    public void testFindBerths_byFullName() {
        List<Berth> result = geoService.findBerths("Nuuk");
        
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        
        Assert.assertEquals("Nuuk", result.get(0).getName());
        Assert.assertNull(result.get(0).getAlias());
        Assert.assertEquals("64 10.400N", result.get(0).getPosition().asGeometryPosition().getLatitudeAsString());
        Assert.assertEquals("051 43.500W", result.get(0).getPosition().asGeometryPosition().getLongitudeAsString());
    }

    @Test
    public void testFindBerths_byEmptyQuery() {
        List<Berth> result = geoService.findBerths("");
        
        Assert.assertNotNull(result);
        Assert.assertEquals(10, result.size());
    }

}
