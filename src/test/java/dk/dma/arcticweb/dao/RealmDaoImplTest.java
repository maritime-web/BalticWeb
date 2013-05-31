/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.dao;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertPropertyLenientEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dk.dma.arcticweb.domain.authorization.Role;
import dk.dma.arcticweb.domain.authorization.SecuredUser;

public class RealmDaoImplTest {

    private static EntityManagerFactory factory;
    private EntityManager entityManager;
    private RealmDao repository;

    @BeforeClass
    public static void setupForAll() {
        factory = Persistence.createEntityManagerFactory("componentTest");
        
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        
        Role role1 = new Role("role1");
        Role role2 = new Role("role2");
        Role role3 = new Role("role3");

        entityManager.persist(role1);
        entityManager.persist(role2);
        entityManager.persist(role3);

        SecuredUser user1 = new SecuredUser("user1", "pw1");
        SecuredUser user2 = new SecuredUser("user2", "pw2");
        
        user1.addRole(role1);
        user1.addRole(role2);
        entityManager.persist(user1);
        
        user2.addRole(role2);
        user2.addRole(role3);
        
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
        assertEquals("pw1", user.getPassword());
    }


    @Test
    public void testGetByPrimaryKey() {
        SecuredUser user = repository.getByPrimaryKeyReturnAll(1L);

        assertEquals("user1", user.getUserName());
        assertEquals("pw1", user.getPassword());
        
        // clear persistence context to test that roles have been selected eagerly
        entityManager.clear();
        
        assertPropertyLenientEquals("logicalName", asList("role1", "role2"), user.getRoles());
    }

}
