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
package dk.dma.embryo.user.persistence;

import dk.dma.embryo.user.model.AreasOfInterest;
import dk.dma.embryo.user.model.Role;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.model.ShoreRole;
import dk.dma.embryo.user.model.VesselOwnerRole;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RealmDaoImplTest {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    private RealmDao repository;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");

        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();

        Role sailor = new SailorRole();
        Role shore = new ShoreRole();
        Role role3 = new VesselOwnerRole();

        entityManager.persist(sailor);
        entityManager.persist(shore);
        entityManager.persist(role3);

        SecuredUser user1 = new SecuredUser("user1", "pw1", null);
        SecuredUser user2 = new SecuredUser("user2", "pw2", null);


        AreasOfInterest group1 = new AreasOfInterest("Group 1", "", true);
        AreasOfInterest group2 = new AreasOfInterest("Group 1", "", true);
        user1.setRole(sailor);
        user1.addSelectionGroup(group1);
        user1.addSelectionGroup(group2);

        entityManager.persist(user1);
        entityManager.persist(sailor);

        user2.setRole(shore);

        entityManager.persist(user2);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Before
    public void setup() {
        entityManager = factory.createEntityManager();
        repository = new RealmDaoImpl(entityManager);
        entityManager.clear();
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @Test
    public void testFindByUsername() {
        SecuredUser user = repository.findByUsername("user1");

        entityManager.clear();

        assertEquals("user1", user.getUserName());
        assertEquals("pw1", user.getHashedPassword());
    }


    @Test
    public void testGetByPrimaryKey() {
        SecuredUser user1 = repository.getByPrimaryKeyReturnAll(1L);

        // clear persistence context to test that roles and permissions have been selected eagerly
        entityManager.clear();

        assertEquals("user1", user1.getUserName());
        assertEquals("pw1", user1.getHashedPassword());
        assertEquals("Sailor", user1.getRole().getLogicalName());

        entityManager.clear();

        // asserting on user 2
        SecuredUser user2 = repository.getByPrimaryKeyReturnAll(2L);

        // clear persistence context to test that roles and permissions have been selected eagerly
        entityManager.clear();

        assertEquals("user2", user2.getUserName());
        assertEquals("pw2", user2.getHashedPassword());
        assertEquals("Shore", user2.getRole().getLogicalName());
    }

    @Test
    public void testFindByUsernameWithSelectionArea() {
        SecuredUser user = repository.findByUsername("user1");

        List<AreasOfInterest> selectionGroups = user.getAreasOfInterest();

        entityManager.clear();

        assertEquals("user1", user.getUserName());
        assertEquals("pw1", user.getHashedPassword());
        Assert.assertTrue(selectionGroups != null && !selectionGroups.isEmpty());
        Assert.assertTrue(selectionGroups.size() == 2);
    }

    @Test
    public void testFindByUsernameWithSelectionAreaToBeDeleted() {
        SecuredUser user = repository.findByUsername("user1");

        List<AreasOfInterest> selectionGroups = user.getAreasOfInterest();

        //entityManager.clear();

        user.setAreasOfInterest(null);
        SecuredUser updatedUser = repository.saveEntity(user);

        assertEquals("user1", updatedUser.getUserName());
        assertEquals("pw1", updatedUser.getHashedPassword());
        Assert.assertTrue(updatedUser.getAreasOfInterest() == null || updatedUser.getAreasOfInterest().isEmpty());
    }
}
