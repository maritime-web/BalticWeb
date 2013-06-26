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
package dk.dma.arcticweb.site.pages.test;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;

import dk.dma.arcticweb.dao.RealmDao;
import dk.dma.arcticweb.dao.UserDao;
import dk.dma.embryo.domain.AuthorityRole;
import dk.dma.embryo.domain.Permission;
import dk.dma.embryo.domain.Role;
import dk.dma.embryo.domain.Sailor;
import dk.dma.embryo.domain.SecuredUser;
import dk.dma.embryo.domain.Ship2;
import dk.dma.embryo.domain.VoyageInformation2;

public class TestPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @EJB
    private RealmDao realmDao;

    @EJB
    private UserDao userDao;

    @Inject
    private transient Logger logger;

    @Inject
    private EntityManager em;

    @Inject
    UserTransaction tx;

    public TestPage() {
        init2();
    }

 
    public void init2() {

        try {
            logger.info("Deleting existing entries");
            tx.begin();
            em.createQuery("DELETE Voyage v").executeUpdate();
            em.createQuery("DELETE VoyageInformation2 v").executeUpdate();
            em.createQuery("DELETE Ship2 s where s.name = 'ORASILA'").executeUpdate();
            em.createQuery("DELETE SecuredUser u where u.userName = 'ora' or u.userName='dma'").executeUpdate();
            em.createQuery(
                    "DELETE Role r where r.logicalName = 'sailor' or r.logicalName='authority' or r.logicalName IS NULL")
                    .executeUpdate();
            em.createQuery("DELETE Permission p where p.logicalName = 'ais' or p.logicalName='yourShip'")
                    .executeUpdate();
            tx.commit();
        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException
                | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            // TODO Auto-generated catch block
            logger.error("Error deleting existing entries", e);
            throw new RuntimeException(e);
        }

        logger.info("AFTER DELETION");
        logExistingEntries();

        // Create ship and user
        Ship2 newShip = new Ship2();
        newShip.setName("ORASILA");
        newShip.setMmsi(220443000L);
        realmDao.saveEntity(newShip);

        Permission ais = new Permission("ais");
        Permission yourShip = new Permission("yourShip");

        realmDao.saveEntity(ais);
        realmDao.saveEntity(yourShip);

        Sailor sailorRole = new Sailor();
        sailorRole.setShip(newShip);
        sailorRole.add(ais);
        sailorRole.add(yourShip);

        realmDao.saveEntity(sailorRole);

        SecuredUser user = new SecuredUser("ora", "qwerty", "obo@dma.dk");
        user.addRole(sailorRole);

        realmDao.saveEntity(user);

        // Create auth and user
        AuthorityRole auth = new AuthorityRole();
        // auth.setName(new Text("en", "Danish Maritime Authority"));
        auth.add(ais);
        realmDao.saveEntity(auth);

        user = new SecuredUser("dma", "qwerty", "obo@dma.dk");
        user.addRole(auth);

        realmDao.saveEntity(user);

        logger.info("AFTER CREATION");
        logExistingEntries();

        // List<Stakeholder> stakeholders = stakeholderDao.getAll();
        // for (Stakeholder stakeholder : stakeholders) {
        // if (stakeholder instanceof Ship) {
        // Ship ship = (Ship) stakeholder;
        // logger.info("mmsi: {}", ship.getMmsi());
        // }
        // }
    }

    private void logExistingEntries() {
        try {
            tx.begin();
            logger.info("Permissions: {} ", realmDao.getAll(Permission.class));
            logger.info("Roles: {} ", realmDao.getAll(Role.class));
            logger.info("Users: {} ", realmDao.getAll(SecuredUser.class));
            logger.info("Ships: {} ", realmDao.getAll(Ship2.class));
            logger.info("VoyageInformations: {} ", realmDao.getAll(VoyageInformation2.class));
            tx.commit();
        } catch (NotSupportedException | SystemException | SecurityException | IllegalStateException
                | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            // TODO Auto-generated catch block
            logger.error("Error deleting existing entries", e);
            throw new RuntimeException(e);
        }

    }
}
